package simple.orm.repo.impl.meta;

import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import simple.orm.jdbc.query.Query;
import simple.orm.loader.QueryParser;
import simple.orm.repo.RepositoryBuilderException;
import simple.orm.repo.anno.ExtractParam;
import simple.orm.repo.anno.InjectParam;
import simple.orm.repo.anno.QuerySource;
import simple.orm.repo.anno.SimpleOrmRepo;
import simple.orm.repo.anno.SimpleQuery;
import simple.orm.util.Mutable;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static io.vavr.API.*;
import static simple.orm.jdbc.query.QueryType.*;
import static simple.orm.repo.anno.ParameterStrategy.*;
import static simple.orm.util.StringUtils.nullify;

/**
 * Utility class to collect metadata from repository interface annotations.
 */
public class MetadataCollector {

    public RepositoryMeta collectMetadata(Class<?> repoInterface) {
        if (repoInterface == null) {
            throw new NullPointerException("repoInterface is null");
        }
        if (!repoInterface.isInterface()) {
            throw new NullPointerException("repoInterface is not an interface");
        }
        // top level annotation
        final SimpleOrmRepo annRepo = repoInterface.getDeclaredAnnotation(SimpleOrmRepo.class);
        if (annRepo == null) {
            throw new RepositoryBuilderException(repoInterface.getName() +
                    " must be annotated with " + SimpleOrmRepo.class.getSimpleName());
        }
        // sift through methods and validate their annotations
        final Mutable<Seq<Method>> repoMethods = Mutable.of(List.empty());
        for (Method m : repoInterface.getMethods()) {
            if (ObjectMethod.match(m) != null) {
                continue;
            }
            validate(m);
            repoMethods.apply(list -> list.append(m));
        }
        // collect metadata
        return new RepositoryMeta(
                annRepo.type(),
                repoMethods.get().map(this::extractMeta),
                annRepo.timeout()
        );
    }

    private void validate(Method method) {
        if (method.getReturnType() != Query.class) {
            throw new RepositoryBuilderException(method.getName() + " should return " + Query.class.getName());
        }
        if (method.getParameterCount() != 0) {
            throw new RepositoryBuilderException(method.getName() + " should have no parameters");
        }
        final SimpleQuery annSimpleQuery = method.getAnnotation(SimpleQuery.class);
        final QuerySource annQuerySource = method.getAnnotation(QuerySource.class);
        final InjectParam[] annInjectParam = method.getAnnotationsByType(InjectParam.class);
        final ExtractParam[] annExtractParam = method.getAnnotationsByType(ExtractParam.class);
        // SimpleQuery and QuerySource
        if (annSimpleQuery == null) {
            throw new RepositoryBuilderException(method.getName() +
                    " must be annotated with " + SimpleQuery.class.getSimpleName());
        }
        if (annQuerySource == null) {
            throw new RepositoryBuilderException(method.getName() +
                    " must be annotated with " + QuerySource.class.getSimpleName());
        }
        boolean hasSQL = !annQuerySource.query().isBlank();
        boolean hasURI = !annQuerySource.uri().isBlank();
        if (!hasSQL && !hasURI) {
            throw new RepositoryBuilderException(method.getName() +
                    " must have either query SQL or query URI specified");
        }
        if (hasSQL && hasURI) {
            throw new RepositoryBuilderException(method.getName() +
                    " must have either query SQL or query URI specified, not both");
        }
        // parameters present (or not)
        boolean hasInjectionParams = annInjectParam.length > 0;
        boolean hasExtractionParams = annExtractParam.length > 0;
        switch (annSimpleQuery.parameters()) {
            case PROVIDED -> {
                if (annSimpleQuery.type() == SELECT && !hasExtractionParams) {
                    throw new RepositoryBuilderException(method.getName() +
                            " must be annotated with at least one " + ExtractParam.class.getSimpleName() +
                            " cause query type is " + annSimpleQuery.type() +
                            " and parameters strategy is " + PROVIDED);
                }
                if ((annSimpleQuery.type() == DDL || annSimpleQuery.type() == DML) && hasExtractionParams) {
                    throw new RepositoryBuilderException(method.getName() +
                            " should have no " + ExtractParam.class.getSimpleName() +
                            " cause query type is " + annSimpleQuery.type());
                }
            }
            case PARSE_QUERY -> {
                if (hasInjectionParams || hasExtractionParams) {
                    throw new RepositoryBuilderException(method.getName() +
                            " should have no " + InjectParam.class.getSimpleName() +
                            " and " + ExtractParam.class.getSimpleName() +
                            " cause parameters strategy is " + PARSE_QUERY);
                }
            }
        }
        // injection parameters
        if (hasInjectionParams) {
            final Seq<InjectParam> params = Array.of(annInjectParam);
            // validate indexing
            boolean noIndexes = params.find(p -> p.index() != -1).isEmpty();
            boolean allIndexesSpecified = params.find(p -> p.index() == -1).isEmpty();
            if (!noIndexes && !allIndexesSpecified) {
                throw new RepositoryBuilderException(method.getName() +
                        " all " + InjectParam.class.getSimpleName() +
                        " must be either properly indexed, either have no indexes");
            }
            if (allIndexesSpecified) {
                boolean brokenIndexing = params.sortBy(InjectParam::index)
                        .zipWithIndex()
                        .find(t2 -> t2._1.index() - 1 != t2._2)
                        .isDefined();
                if (brokenIndexing) {
                    throw new RepositoryBuilderException(method.getName() +
                            " " + InjectParam.class.getSimpleName() +
                            " indexing is broken (must start with 1 and increment by 1)");
                }
            }
            // check prop names specified if NamedInjector is used
            if (annSimpleQuery.sourceClass() != Seq.class) {
                if (params.find(p -> p.prop().isBlank()).isDefined()) {
                    throw new RepositoryBuilderException(method.getName() +
                            " must have property name specified for each " + InjectParam.class.getSimpleName() +
                            " (cause " + SimpleQuery.class.getSimpleName() + " has sourceClass for NamedInjector)");
                }
            } else if (params.find(p -> !p.prop().isBlank()).isDefined()) {
                throw new RepositoryBuilderException(method.getName() +
                        " must have no property names specified for each " + InjectParam.class.getSimpleName() +
                        " (cause " + SimpleQuery.class.getSimpleName() + " has no sourceClass, hence will use IndexedInjector)");
            }
        }
        // extraction parameters
        if (hasExtractionParams) {
            final Seq<ExtractParam> params = Array.of(annExtractParam);
            // validate labelling
            boolean noLabels = params.find(p -> !p.label().isBlank()).isEmpty();
            boolean allLabelsSpecified = params.find(p -> p.label().isBlank()).isEmpty();
            if (!noLabels && !allLabelsSpecified) {
                throw new RepositoryBuilderException(method.getName() +
                        " all " + ExtractParam.class.getSimpleName() +
                        " must either have label specified, either have none");
            }
            // validate indexing
            boolean noIndexes = params.find(p -> p.index() != -1).isEmpty();
            boolean allIndexesSpecified = params.find(p -> p.index() == -1).isEmpty();
            if (noLabels && !noIndexes && !allIndexesSpecified) {
                throw new RepositoryBuilderException(method.getName() +
                        " all " + ExtractParam.class.getSimpleName() +
                        " must be either properly indexed, either have no indexes");
            }
            if (noLabels && allIndexesSpecified) {
                boolean brokenIndexing = params.sortBy(ExtractParam::index)
                        .zipWithIndex()
                        .find(t2 -> t2._1.index() - 1 != t2._2)
                        .isDefined();
                if (brokenIndexing) {
                    throw new RepositoryBuilderException(method.getName() +
                            " " + ExtractParam.class.getSimpleName() +
                            " indexing is broken (must start with 1 and increment by 1)");
                }
            }
            // check prop names specified if NamedExtractor is used
            if (annSimpleQuery.targetClass() != Seq.class) {
                if (params.find(p -> p.prop().isBlank()).isDefined()) {
                    throw new RepositoryBuilderException(method.getName() +
                            " must have property name specified for each " + ExtractParam.class.getSimpleName() +
                            " (cause " + SimpleQuery.class.getSimpleName() + " has targetClass for NamedExtractor)");
                }
            } else if (params.find(p -> !p.prop().isBlank()).isDefined()) {
                throw new RepositoryBuilderException(method.getName() +
                        " must have no property names specified for each " + ExtractParam.class.getSimpleName() +
                        " (cause " + SimpleQuery.class.getSimpleName() + " has no targetClass, hence will use NamedExtractor)");
            }
        }
    }

    private QueryMethodMeta extractMeta(Method method) {
        final SimpleQuery annSimpleQuery = method.getAnnotation(SimpleQuery.class);
        final QuerySource annQuerySource = method.getAnnotation(QuerySource.class);
        final InjectParam[] annInjectParam = method.getAnnotationsByType(InjectParam.class);
        final ExtractParam[] annExtractParam = method.getAnnotationsByType(ExtractParam.class);
        return new QueryMethodMeta(
                method.getName(),
                annSimpleQuery.type(),
                annQuerySource.query().isBlank() ? null : annQuerySource.query(),
                annQuerySource.uri().isBlank() ? null : annQuerySource.uri(),
                annQuerySource.uriCharset(),
                annSimpleQuery.parameters(),
                annSimpleQuery.sourceClass() == Seq.class ? null : annSimpleQuery.sourceClass(),
                annSimpleQuery.targetClass() == Seq.class ? null : annSimpleQuery.targetClass(),
                reindexOrSort(annInjectParam.length == 0 ?
                        Array.empty() :
                        Array.of(annInjectParam).map(this::toQueryParam)
                ),
                reindexOrSort(annExtractParam.length == 0 ?
                        Array.empty() :
                        Array.of(annExtractParam).map(this::toQueryParam)),
                annSimpleQuery.timeout()
        );
    }

    private QueryParser.QueryParam toQueryParam(InjectParam param) {
        return new QueryParser.QueryParam(
                QueryParser.ParamType.INJECTION,
                param.index(),
                null,
                false,
                nullify(param.prop()),
                nullify(param.mapper()),
                nullify(param.tag()),
                nullify(param.jdbc()),
                getJavaClassName(param.java())
        );
    }

    private QueryParser.QueryParam toQueryParam(ExtractParam param) {
        return new QueryParser.QueryParam(
                QueryParser.ParamType.EXTRACTION,
                param.index(),
                nullify(param.label()),
                false,
                nullify(param.prop()),
                nullify(param.mapper()),
                nullify(param.tag()),
                nullify(param.jdbc()),
                getJavaClassName(param.java())
        );
    }

    private Traversable<QueryParser.QueryParam> reindexOrSort(Array<QueryParser.QueryParam> params) {
        if (params.isEmpty()) {
            return params;
        }
        // reindex params if no indexes specified
        if (params.get(0).indexWithinType() == -1) {
            return params.zipWithIndex((p, i) -> p.reindex(i + 1));
        } else {
            // or sort them if indexes were provided
            return params.sortBy(QueryParser.QueryParam::indexWithinType);
        }
    }

    private static String getJavaClassName(Class<?> aClass) {
        // unfortunately have to use QueryParser.QueryParam with string to pass java class information
        // works badly for primitive types (Class.forName can't load them)
        // looks like in Java22 they have Class#forPrimitiveName
        // but that's so half-assed... like everything they do though...
        if (aClass == Object.class) {
            return null;
        }
        if (aClass.isPrimitive()) {
            return Match(aClass).of(
                    Case($(same(Boolean.TYPE)), Boolean.class.getName()),
                    Case($(same(Byte.TYPE)), Byte.class.getName()),
                    Case($(same(Short.TYPE)), Short.class.getName()),
                    Case($(same(Character.TYPE)), Character.class.getName()),
                    Case($(same(Integer.TYPE)), Integer.class.getName()),
                    Case($(same(Long.TYPE)), Long.class.getName()),
                    Case($(same(Float.TYPE)), Float.class.getName()),
                    Case($(same(Double.TYPE)), Double.class.getName()),
                    Case($(same(Void.TYPE)), Void.class.getName()), // mmmm...anyway
                    Case($(), () -> {
                        throw new IllegalStateException("should be unreachable for " + aClass);
                    })
            );
        }
        return aClass.getName();
    }

    private static Predicate<Class<?>> same(Class<?> type) {
        return aClass -> type == aClass;
    }

}
