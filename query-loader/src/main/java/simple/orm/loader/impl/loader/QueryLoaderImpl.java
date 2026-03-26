package simple.orm.loader.impl.loader;

import io.vavr.collection.Traversable;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryFactory;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryLoader;
import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;
import simple.orm.loader.builder.ParameterType;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.loader.impl.builder.QueryBuilderImpl;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.TypesCollection;

import java.util.function.Supplier;

/**
 * Implementation of {@link QueryLoader}.
 */
public class QueryLoaderImpl implements QueryLoader {

    private final QueryParser parser;
    private final QueryBuilder builder;

    public QueryLoaderImpl(QueryFactory qFactory,
                           QueryParser parser,
                           TypesCollection typesCollection,
                           Supplier<MappersFinder> mappersFinderSupplier,
                           Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        this(parser,
                new QueryBuilderImpl(
                        qFactory,
                        typesCollection,
                        mappersFinderSupplier,
                        reflectionsFinderSupplier
                )
        );
    }

    protected QueryLoaderImpl(QueryParser parser,
                              QueryBuilder builder) {
        if (parser == null) {
            throw new NullPointerException("parser is null");
        }
        if (builder == null) {
            throw new NullPointerException("builder is null");
        }
        this.parser = parser;
        this.builder = builder;
    }

    @Override
    public <P, R> Query<P, R> loadQuery(QueryType type,
                                        QuerySource source,
                                        InjectionStrategy<P> injectionStrategy,
                                        ExtractionStrategy<R> extractionStrategy,
                                        int queryTimeoutSeconds) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (source == null) {
            throw new NullPointerException("source is null");
        }
        if (injectionStrategy == null) {
            throw new NullPointerException("injectionStrategy is null");
        }
        if (extractionStrategy == null) {
            throw new NullPointerException("extractionStrategy is null");
        }
        // parse
        final QueryParser.ParsedQuery parsedQuery = parser.parse(source);
        // build query
        final String sql = parsedQuery.querySQL();
        final Traversable<QueryParameter> params = parsedQuery.parsedParams()
                .map(qp -> new QueryParameter(
                        ParameterType.of(qp.type()),
                        qp.indexWithinType(),
                        qp.label(),
                        qp.labelGuessed(),
                        qp.propName(),
                        null,
                        qp.mapperName(),
                        qp.tag(),
                        null,
                        qp.jdbcTypeName(),
                        null,
                        qp.javaClassName()
                ));
        return builder.buildQuery(type, sql, injectionStrategy, extractionStrategy, params, queryTimeoutSeconds);
    }

}
