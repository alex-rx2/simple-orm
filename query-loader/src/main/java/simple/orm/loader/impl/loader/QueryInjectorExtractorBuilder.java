package simple.orm.loader.impl.loader;

import io.vavr.collection.Traversable;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.loader.QueryParser;
import simple.orm.loader.RuntimeClassNotFoundException;
import simple.orm.mapping.builder.IndexedExtractorBuilder;
import simple.orm.mapping.builder.IndexedInjectorBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.NamedExtractorBuilder;
import simple.orm.mapping.builder.NamedInjectorBuilder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.TypesCollection;

import static simple.orm.util.StringUtils.qnn;

/**
 * Collections of utility methods to build injectors/extractors from {@link QueryParser.QueryParam}s list.
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
                                                Traversable<QueryParser.QueryParam> params) {
        IndexedInjectorBuilder builder = IndexedInjectorBuilder.builder(mappersFinder);
        params.forEach(qp -> appendToBuilder(typesCollection, builder, qp));
        return builder.build();
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 IndexedInjectorBuilder builder,
                                 QueryParser.QueryParam qp) {
        try {
            builder.param(
                    qp.mapperName(),
                    empty(qp.jdbcTypeName()) ? null : typesCollection.findType(qp.jdbcTypeName()),
                    empty(qp.javaClassName()) ? null : Class.forName(qp.javaClassName()),
                    qp.tag()
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("injection parameter #" + qp.indexWithinType() +
                    " referenced class " + qnn(qp.javaClassName()) + " not found", e);
        }
    }

    public IndexedExtractor buildIndexedExtractor(TypesCollection typesCollection,
                                                  MappersFinder mappersFinder,
                                                  Traversable<QueryParser.QueryParam> params) {
        IndexedExtractorBuilder builder = IndexedExtractorBuilder.builder(mappersFinder);
        params.forEach(qp -> appendToBuilder(typesCollection, builder, qp));
        return builder.build();
    }

    private void appendToBuilder(TypesCollection typesCollection,
                                 IndexedExtractorBuilder builder,
                                 QueryParser.QueryParam qp) {
        try {
            builder.param(
                    qp.mapperName(),
                    empty(qp.jdbcTypeName()) ? null : typesCollection.findType(qp.jdbcTypeName()),
                    empty(qp.javaClassName()) ? null : Class.forName(qp.javaClassName()),
                    qp.tag()
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + qp.indexWithinType() +
                    " referenced class " + qnn(qp.javaClassName()) + " not found", e);
        }
    }

    public <T> NamedInjector<T> buildNamedInjector(TypesCollection typesCollection,
                                                   MappersFinder mappersFinder,
                                                   ReflectionsFinder reflectionsFinder,
                                                   Class<T> sourceClass,
                                                   Traversable<QueryParser.QueryParam> params) {
        NamedInjectorBuilder<T> builder = NamedInjectorBuilder.builder(mappersFinder, reflectionsFinder, sourceClass);
        params.forEach(qp -> appendToBuilder(typesCollection, builder, qp));
        return builder.build();
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     NamedInjectorBuilder<T> builder,
                                     QueryParser.QueryParam qp) {
        try {
            builder.param(
                    qp.propName(),
                    qp.mapperName(),
                    empty(qp.jdbcTypeName()) ? null : typesCollection.findType(qp.jdbcTypeName()),
                    empty(qp.javaClassName()) ? null : Class.forName(qp.javaClassName()),
                    qp.tag()
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("injection parameter #" + qp.indexWithinType() +
                    " referenced class " + qnn(qp.javaClassName()) + " not found", e);
        }
    }

    public <T> NamedExtractor<T> buildNamedExtractor(TypesCollection typesCollection,
                                                     MappersFinder mappersFinder,
                                                     ReflectionsFinder reflectionsFinder,
                                                     Class<T> targetClass,
                                                     Traversable<QueryParser.QueryParam> params) {
        NamedExtractorBuilder<T> builder = NamedExtractorBuilder.builder(mappersFinder, reflectionsFinder, targetClass);
        params.forEach(qp -> appendToBuilder(typesCollection, builder, qp));
        return builder.build();
    }

    private <T> void appendToBuilder(TypesCollection typesCollection,
                                     NamedExtractorBuilder<T> builder,
                                     QueryParser.QueryParam qp) {
        try {
            if (!empty(qp.label())) {
                builder.param(
                        empty(qp.propName()) ? qp.label() : qp.propName(),
                        qp.label(),
                        qp.mapperName(),
                        empty(qp.jdbcTypeName()) ? null : typesCollection.findType(qp.jdbcTypeName()),
                        empty(qp.javaClassName()) ? null : Class.forName(qp.javaClassName()),
                        qp.tag()
                );
            } else {
                builder.param(
                        qp.propName(),
                        qp.mapperName(),
                        empty(qp.jdbcTypeName()) ? null : typesCollection.findType(qp.jdbcTypeName()),
                        empty(qp.javaClassName()) ? null : Class.forName(qp.javaClassName()),
                        qp.tag()
                );
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeClassNotFoundException("extraction parameter #" + qp.indexWithinType() +
                    " referenced class " + qnn(qp.javaClassName()) + " not found", e);
        }
    }

    private boolean empty(String s) {
        return s == null || s.isBlank();
    }

}
