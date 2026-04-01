package simple.orm.loader.impl.builder;

import io.vavr.collection.Traversable;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.loader.RuntimeClassNotFoundException;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.mapping.builder.IndexIndexedExtractorBuilder;
import simple.orm.mapping.builder.IndexNamedExtractorBuilder;
import simple.orm.mapping.builder.IndexedInjectorBuilder;
import simple.orm.mapping.builder.LabelIndexedExtractorBuilder;
import simple.orm.mapping.builder.LabelNamedExtractorBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.NamedInjectorBuilder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.builder.SimpleIndexedExtractorBuilder;
import simple.orm.mapping.builder.SimpleNamedExtractorBuilder;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;

import static simple.orm.util.StringUtils.*;

/**
 * Collections of utility methods to build injectors/extractors from {@link QueryParameter}s list.
 * <br>
 * It is expected that all required validations were performed beforehand.
 */
public class QueryInjectorExtractorBuilder {

    private static QueryInjectorExtractorBuilder INSTANCE = null;

    public static QueryInjectorExtractorBuilder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QueryInjectorExtractorBuilder();
        }
        return INSTANCE;
    }

    private QueryInjectorExtractorBuilder() {
    }

    public IndexedInjector buildIndexedInjector(TypesCollection typesCollection,
                                                MappersFinder mappersFinder,
                                                Traversable<QueryParameter> params) {
        IndexedInjectorBuilder builder = IndexedInjectorBuilder.builder(mappersFinder);
        params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
        return builder.build();
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 IndexedInjectorBuilder builder,
                                 QueryParameter p,
                                 int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(p.mapper());
            } else {
                builder.param(
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("injection parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    public IndexedExtractor buildIndexedExtractor(TypesCollection typesCollection,
                                                  MappersFinder mappersFinder,
                                                  Traversable<QueryParameter> params) {
        if (!empty(params.head().label())) {
            LabelIndexedExtractorBuilder builder = LabelIndexedExtractorBuilder.builder(mappersFinder);
            params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
            return builder.build();
        } else if (params.head().index() != null) {
            IndexIndexedExtractorBuilder builder = IndexIndexedExtractorBuilder.builder(mappersFinder);
            params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
            return builder.build();
        } else {
            SimpleIndexedExtractorBuilder builder = SimpleIndexedExtractorBuilder.builder(mappersFinder);
            params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
            return builder.build();
        }
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 LabelIndexedExtractorBuilder builder,
                                 QueryParameter p,
                                 int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(p.label(), p.mapper());
            } else {
                builder.param(
                        p.label(),
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 IndexIndexedExtractorBuilder builder,
                                 QueryParameter p,
                                 int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(p.index(), p.mapper());
            } else {
                builder.param(
                        p.index(),
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 SimpleIndexedExtractorBuilder builder,
                                 QueryParameter p,
                                 int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(p.mapper());
            } else {
                builder.param(
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    public <T> NamedInjector<T> buildNamedInjector(TypesCollection typesCollection,
                                                   MappersFinder mappersFinder,
                                                   ReflectionsFinder reflectionsFinder,
                                                   Class<T> sourceClass,
                                                   Traversable<QueryParameter> params) {
        NamedInjectorBuilder<T> builder = NamedInjectorBuilder.builder(mappersFinder, reflectionsFinder, sourceClass);
        params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
        return builder.build();
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     NamedInjectorBuilder<T> builder,
                                     QueryParameter p,
                                     int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(
                        p.propName(),
                        p.mapper()
                );
            } else {
                builder.param(
                        p.propName(),
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("injection parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    public <T> NamedExtractor<T> buildNamedExtractor(TypesCollection typesCollection,
                                                     MappersFinder mappersFinder,
                                                     ReflectionsFinder reflectionsFinder,
                                                     Class<T> targetClass,
                                                     Traversable<QueryParameter> params) {
        if (!empty(params.head().label())) {
            LabelNamedExtractorBuilder<T> builder = LabelNamedExtractorBuilder.builder(mappersFinder, reflectionsFinder, targetClass);
            params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
            return builder.build();
        } else if (params.head().index() != null) {
            IndexNamedExtractorBuilder<T> builder = IndexNamedExtractorBuilder.builder(mappersFinder, reflectionsFinder, targetClass);
            params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
            return builder.build();
        } else {
            SimpleNamedExtractorBuilder<T> builder = SimpleNamedExtractorBuilder.builder(mappersFinder, reflectionsFinder, targetClass);
            params.forEachWithIndex((p, i) -> appendToBuilder(typesCollection, builder, p, i));
            return builder.build();
        }
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     LabelNamedExtractorBuilder<T> builder,
                                     QueryParameter p,
                                     int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(
                        p.label(),
                        empty(p.propName()) ? p.label() : p.propName(),
                        p.mapper()
                );
            } else {
                builder.param(
                        p.label(),
                        empty(p.propName()) ? p.label() : p.propName(),
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     IndexNamedExtractorBuilder<T> builder,
                                     QueryParameter p,
                                     int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(
                        p.index(),
                        empty(p.propName()) ? p.label() : p.propName(),
                        p.mapper()
                );
            } else {
                builder.param(
                        p.index(),
                        empty(p.propName()) ? p.label() : p.propName(),
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     SimpleNamedExtractorBuilder<T> builder,
                                     QueryParameter p,
                                     int paramIndex) {
        try {
            if (p.mapper() != null) {
                builder.param(
                        empty(p.propName()) ? p.label() : p.propName(),
                        p.mapper()
                );
            } else {
                builder.param(
                        empty(p.propName()) ? p.label() : p.propName(),
                        p.mapperName(),
                        p.tag(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p)
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + (paramIndex + 1) +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    private ParameterJdbcType<?> jdbcTypeFrom(QueryParameter p, TypesCollection typesCollection) {
        return p.jdbcType() != null
                ? p.jdbcType()
                : empty(p.jdbcTypeName())
                ? null
                : typesCollection.findType(p.jdbcTypeName())
                ;
    }

    private Class<?> javaTypeFrom(QueryParameter p) throws ClassNotFoundException {
        return p.javaType() != null
                ? p.javaType()
                : empty(p.javaTypeClassName())
                ? null
                : Class.forName(p.javaTypeClassName())
                ;
    }

}
