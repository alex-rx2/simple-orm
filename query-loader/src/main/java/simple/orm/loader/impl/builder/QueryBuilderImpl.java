package simple.orm.loader.impl.builder;

import io.vavr.Tuple;
import io.vavr.collection.Traversable;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryFactory;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryParser;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.TypesCollection;

import java.util.function.Supplier;

import static io.vavr.API.*;
import static simple.orm.jdbc.query.QueryType.*;
import static simple.orm.loader.StrategyType.*;
import static simple.orm.loader.builder.ParameterType.*;

/**
 * Default {@link QueryBuilder} implementation.
 */
public class QueryBuilderImpl implements QueryBuilder {

    private final QueryFactory qFactory;
    private final QueryValidator validator;
    private final QueryInjectorExtractorBuilder ieBuilder;

    private final TypesCollection typesCollection;
    private final Supplier<MappersFinder> mappersFinderSupplier;
    private final Supplier<ReflectionsFinder> reflectionsFinderSupplier;

    public QueryBuilderImpl(QueryFactory qFactory,
                            TypesCollection typesCollection,
                            Supplier<MappersFinder> mappersFinderSupplier,
                            Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        this(qFactory,
                QueryValidator.getInstance(),
                QueryInjectorExtractorBuilder.getInstance(),
                typesCollection,
                mappersFinderSupplier,
                reflectionsFinderSupplier);
    }

    protected QueryBuilderImpl(QueryFactory qFactory,
                               QueryValidator validator,
                               QueryInjectorExtractorBuilder ieBuilder,
                               TypesCollection typesCollection,
                               Supplier<MappersFinder> mappersFinderSupplier,
                               Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        if (qFactory == null) {
            throw new NullPointerException("qFactory is null");
        }
        if (validator == null) {
            throw new NullPointerException("validator is null");
        }
        if (ieBuilder == null) {
            throw new NullPointerException("ieBuilder is null");
        }
        if (typesCollection == null) {
            throw new NullPointerException("typesCollection is null");
        }
        if (mappersFinderSupplier == null) {
            throw new NullPointerException("mappersFinderSupplier is null");
        }
        if (reflectionsFinderSupplier == null) {
            throw new NullPointerException("reflectionsFinderSupplier is null");
        }
        this.qFactory = qFactory;
        this.validator = validator;
        this.ieBuilder = ieBuilder;
        this.typesCollection = typesCollection;
        this.mappersFinderSupplier = mappersFinderSupplier;
        this.reflectionsFinderSupplier = reflectionsFinderSupplier;
    }

    @Override
    @Deprecated
    public <P, R> Query<P, R> buildQueryOld(QueryType type,
                                            String sql,
                                            InjectionStrategy<P> injectionStrategy,
                                            ExtractionStrategy<R> extractionStrategy,
                                            Traversable<QueryParser.QueryParam> params,
                                            int queryTimeoutSeconds) {
        return buildQuery(
                type,
                sql,
                injectionStrategy,
                extractionStrategy,
                params.map(p -> QueryParameter.of(p, p.type() == QueryParser.ParamType.INJECTION || extractionStrategy.targetClass == null)),
                queryTimeoutSeconds
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P, R> Query<P, R> buildQuery(QueryType type,
                                         String sql,
                                         InjectionStrategy<P> injectionStrategy,
                                         ExtractionStrategy<R> extractionStrategy,
                                         Traversable<QueryParameter> params,
                                         int queryTimeoutSeconds) {
        // TODO
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (injectionStrategy == null) {
            throw new NullPointerException("injectionStrategy is null");
        }
        if (extractionStrategy == null) {
            throw new NullPointerException("extractionStrategy is null");
        }
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        // create query
        return Match(type).of(
                Case($(DDL), () -> {
                    validator.validateDDL(injectionStrategy, extractionStrategy, params);
                    return (Query<P, R>) createDDL(sql, queryTimeoutSeconds);
                }),
                Case($(DML), () -> {
                    validator.validateDML(injectionStrategy, extractionStrategy, params);
                    return (Query<P, R>) createDML(sql, params, injectionStrategy, queryTimeoutSeconds);
                }),
                Case($(SELECT), () -> {
                    validator.validateSelect(injectionStrategy, extractionStrategy, params);
                    return (Query<P, R>) createSelect(sql, params, injectionStrategy, extractionStrategy, queryTimeoutSeconds);
                })
        );
    }

    private Query<Void, Void> createDDL(String sql,
                                        int queryTimeoutSeconds) {
        return qFactory.ddlQuery(sql, queryTimeoutSeconds);
    }

    private Query<?, ?> createDML(String sql,
                                  Traversable<QueryParameter> params,
                                  InjectionStrategy<?> istrat,
                                  int queryTimeoutSeconds) {
        final Traversable<QueryParameter> iparams = params.filter(p -> p.type() == INJECTION);
        return Match(istrat.type).of(
                Case($(NONE),
                        () -> qFactory.iudQueryWithoutParameters(
                                sql,
                                queryTimeoutSeconds
                        )),
                Case($(INDEXED),
                        () -> qFactory.iudQuery(
                                sql,
                                queryTimeoutSeconds,
                                createIInjector(iparams)
                        )),
                Case($(NAMED),
                        () -> qFactory.iudQuery(
                                sql,
                                queryTimeoutSeconds,
                                createNInjector(istrat.sourceClass, iparams)
                        ))
        );
    }

    private Query<?, ?> createSelect(String sql,
                                     Traversable<QueryParameter> params,
                                     InjectionStrategy<?> istrat,
                                     ExtractionStrategy<?> estrat,
                                     int queryTimeoutSeconds) {
        final Traversable<QueryParameter> iparams = params.filter(p -> p.type() == INJECTION);
        final Traversable<QueryParameter> eparams = params.filter(p -> p.type() == EXTRACTION);
        return Match(Tuple.of(istrat.type, estrat.type)).of(
                Case($(Tuple.of(NONE, INDEXED)),
                        () -> qFactory.selectQueryWithoutParameters(
                                sql,
                                queryTimeoutSeconds,
                                createIExtractor(eparams)
                        )),
                Case($(Tuple.of(NONE, NAMED)),
                        () -> qFactory.selectQueryWithoutParameters(
                                sql,
                                queryTimeoutSeconds,
                                createNExtractor(estrat.targetClass, eparams)
                        )),
                Case($(Tuple.of(INDEXED, INDEXED)),
                        () -> qFactory.selectQuery(
                                sql,
                                queryTimeoutSeconds,
                                createIInjector(iparams),
                                createIExtractor(eparams)
                        )),
                Case($(Tuple.of(INDEXED, NAMED)),
                        () -> qFactory.selectQuery(
                                sql,
                                queryTimeoutSeconds,
                                createIInjector(iparams),
                                createNExtractor(estrat.targetClass, eparams)
                        )),
                Case($(Tuple.of(NAMED, INDEXED)),
                        () -> qFactory.selectQuery(
                                sql,
                                queryTimeoutSeconds,
                                createNInjector(istrat.sourceClass, iparams),
                                createIExtractor(eparams)
                        )),
                Case($(Tuple.of(NAMED, NAMED)),
                        () -> qFactory.selectQuery(
                                sql,
                                queryTimeoutSeconds,
                                createNInjector(istrat.sourceClass, iparams),
                                createNExtractor(estrat.targetClass, eparams)
                        ))
        );
    }

    private IndexedInjector createIInjector(Traversable<QueryParameter> params) {
        return ieBuilder.buildIndexedInjector(
                typesCollection,
                mappersFinderSupplier.get(),
                params
        );
    }

    private <P> NamedInjector<P> createNInjector(Class<P> sourceClass, Traversable<QueryParameter> params) {
        return ieBuilder.buildNamedInjector(
                typesCollection,
                mappersFinderSupplier.get(),
                reflectionsFinderSupplier.get(),
                sourceClass,
                params
        );
    }

    private IndexedExtractor createIExtractor(Traversable<QueryParameter> params) {
        return ieBuilder.buildIndexedExtractor(
                typesCollection,
                mappersFinderSupplier.get(),
                params
        );
    }

    private <R> NamedExtractor<R> createNExtractor(Class<R> targetClass, Traversable<QueryParameter> params) {
        return ieBuilder.buildNamedExtractor(
                typesCollection,
                mappersFinderSupplier.get(),
                reflectionsFinderSupplier.get(),
                targetClass,
                params
        );
    }

}
