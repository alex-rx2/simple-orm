package simple.orm.loader.builder;

import io.vavr.collection.Traversable;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryFactory;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryLoader;
import simple.orm.loader.QueryParser;
import simple.orm.loader.RuntimeClassNotFoundException;
import simple.orm.loader.impl.builder.QueryBuilderImpl;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;

import java.util.function.Supplier;

/**
 * An interface for helper class, that builds actual {@link Query} from provided (collected) information.
 * <br>
 * Though {@link QueryLoader} is actually just {@link QueryParser} plus this {@link QueryBuilder}, this interface is
 * supposed only as helper utility for {@link QueryLoader} or other functionality that creates {@link Query}
 * from provided (collected) information. Hence, this interface is <i>hidden</i> in separate package.
 */
public interface QueryBuilder {

    /**
     * Builds a {@link Query} from provided query information.
     *
     * @param type                type of query.
     * @param sql                 query SQL.
     * @param injectionStrategy   parameter injection strategy.
     * @param extractionStrategy  result extraction strategy.
     * @param params              query parameters.
     * @param queryTimeoutSeconds query timeout in seconds (0 - no timeout, -1 - take default timeout from connection).
     * @param <P>                 type of object source of parameters
     *                            (<code>Seq&lt;Object></code> for indexed, <code>Void</code> for query without parameters).
     * @param <R>                 type of result
     *                            (<code>Seq&lt;Object></code> for indexed, <code>Void</code> for query without {@link Result}).
     * @return new query.
     * @throws RuntimeClassNotFoundException wrap around {@link ClassNotFoundException}
     *                                       if query parameter references a class, that failed to be found.
     */
    <P, R> Query<P, R> buildQuery(QueryType type,
                                  String sql,
                                  InjectionStrategy<P> injectionStrategy,
                                  ExtractionStrategy<R> extractionStrategy,
                                  Traversable<QueryParser.QueryParam> params,
                                  int queryTimeoutSeconds);

    /**
     * Factory method creating default {@link QueryBuilder} implementation.
     *
     * @param typesCollection           collection of {@link ParameterJdbcType} to use.
     * @param mappersFinderSupplier     supplier of {@link MappersFinder} to be used in each injector/extractor.
     * @param reflectionsFinderSupplier supplier of {@link ReflectionsFinder} to be used in each injector/extractor.
     * @return new {@link QueryBuilder}.
     */
    static QueryBuilder of(TypesCollection typesCollection,
                           Supplier<MappersFinder> mappersFinderSupplier,
                           Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        return new QueryBuilderImpl(
                QueryFactory.instance(),
                typesCollection,
                mappersFinderSupplier,
                reflectionsFinderSupplier
        );
    }

}
