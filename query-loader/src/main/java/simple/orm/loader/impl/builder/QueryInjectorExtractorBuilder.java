package simple.orm.loader.impl.builder;

import io.vavr.collection.Traversable;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.loader.RuntimeClassNotFoundException;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.mapping.builder.IndexedExtractorBuilder;
import simple.orm.mapping.builder.IndexedInjectorBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.NamedExtractorBuilder;
import simple.orm.mapping.builder.NamedInjectorBuilder;
import simple.orm.mapping.builder.ReflectionsFinder;
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
        params.forEach(p -> appendToBuilder(typesCollection, builder, p));
        return builder.build();
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 IndexedInjectorBuilder builder,
                                 QueryParameter p) {
        try {
            if (p.mapper() != null) {
                builder.param(p.mapper());
            } else {
                builder.param(
                        p.mapperName(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p),
                        p.tag()
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("injection parameter #" + p.indexWithinType() +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    public IndexedExtractor buildIndexedExtractor(TypesCollection typesCollection,
                                                  MappersFinder mappersFinder,
                                                  Traversable<QueryParameter> params) {
        IndexedExtractorBuilder builder = IndexedExtractorBuilder.builder(mappersFinder);
        params.forEach(p -> appendToBuilder(typesCollection, builder, p));
        return builder.build();
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 IndexedExtractorBuilder builder,
                                 QueryParameter p) {
        try {
            if (p.mapper() != null) {
                builder.param(p.mapper());
            } else {
                builder.param(
                        p.mapperName(),
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p),
                        p.tag()
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + p.indexWithinType() +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    public <T> NamedInjector<T> buildNamedInjector(TypesCollection typesCollection,
                                                   MappersFinder mappersFinder,
                                                   ReflectionsFinder reflectionsFinder,
                                                   Class<T> sourceClass,
                                                   Traversable<QueryParameter> params) {
        NamedInjectorBuilder<T> builder = NamedInjectorBuilder.builder(mappersFinder, reflectionsFinder, sourceClass);
        params.forEach(p -> appendToBuilder(typesCollection, builder, p));
        return builder.build();
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     NamedInjectorBuilder<T> builder,
                                     QueryParameter p) {
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
                        jdbcTypeFrom(p, typesCollection),
                        javaTypeFrom(p),
                        p.tag()
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("injection parameter #" + p.indexWithinType() +
                    " referenced class " + qnn(p.javaTypeClassName()) + " not found", e);
        }
    }

    public <T> NamedExtractor<T> buildNamedExtractor(TypesCollection typesCollection,
                                                     MappersFinder mappersFinder,
                                                     ReflectionsFinder reflectionsFinder,
                                                     Class<T> targetClass,
                                                     Traversable<QueryParameter> params) {
        NamedExtractorBuilder<T> builder = NamedExtractorBuilder.builder(mappersFinder, reflectionsFinder, targetClass);
        params.forEach(p -> appendToBuilder(typesCollection, builder, p));
        return builder.build();
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     NamedExtractorBuilder<T> builder,
                                     QueryParameter p) {
        try {
            if (!empty(p.label())) {
                if (p.mapper() != null) {
                    builder.param(
                            empty(p.propName()) ? p.label() : p.propName(),
                            p.label(),
                            p.mapper()
                    );
                } else {
                    builder.param(
                            empty(p.propName()) ? p.label() : p.propName(),
                            p.label(),
                            p.mapperName(),
                            jdbcTypeFrom(p, typesCollection),
                            javaTypeFrom(p),
                            p.tag()
                    );
                }
            } else {
                if (p.mapper() != null) {
                    builder.param(
                            p.propName(),
                            p.mapper()
                    );
                } else {
                    builder.param(
                            p.propName(),
                            p.mapperName(),
                            jdbcTypeFrom(p, typesCollection),
                            javaTypeFrom(p),
                            p.tag()
                    );
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + p.indexWithinType() +
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
