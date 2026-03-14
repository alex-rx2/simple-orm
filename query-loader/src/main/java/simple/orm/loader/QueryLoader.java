package simple.orm.loader;

import simple.orm.jdbc.Result;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryFactory;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.impl.loader.QueryLoaderImpl;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Supplier;

/**
 * Interface for query loader.
 * <br>
 * Query loader constructs {@link Query} from provided SQL.
 */
public interface QueryLoader {

    /**
     * Loads query SQL, parses out parameter/result info according to provided strategy and constructs a {@link Query}.
     * <br>
     * Constructed query has no timeout.
     *
     * @param type               type of query.
     * @param source             source of query SQL.
     * @param injectionStrategy  parameter injection strategy.
     * @param extractionStrategy result extraction strategy.
     * @param <P>                type of object source of parameters
     *                           (<code>Seq&lt;Object></code> for indexed, <code>Void</code> for query without parameters).
     * @param <R>                type of result
     *                           (<code>Seq&lt;Object></code> for indexed, <code>Void</code> for query without {@link Result}).
     * @return new query.
     * @throws RuntimeIOException            wrap around possible {@link IOException} working with source {@link Reader}.
     * @throws RuntimeClassNotFoundException wrap around {@link ClassNotFoundException}
     *                                       if query parameter references a class, that failed to be found.
     */
    default <P, R> Query<P, R> loadQuery(QueryType type,
                                         QuerySource source,
                                         InjectionStrategy<P> injectionStrategy,
                                         ExtractionStrategy<R> extractionStrategy) {
        return loadQuery(type, source, injectionStrategy, extractionStrategy, 0);
    }

    /**
     * Loads query SQL, parses out parameter/result info according to provided strategy and constructs a {@link Query}.
     *
     * @param type                type of query.
     * @param source              source of query SQL.
     * @param injectionStrategy   parameter injection strategy.
     * @param extractionStrategy  result extraction strategy.
     * @param queryTimeoutSeconds query timeout in seconds (0 - no timeout).
     * @param <P>                 type of object source of parameters
     *                            (<code>Seq&lt;Object></code> for indexed, <code>Void</code> for query without parameters).
     * @param <R>                 type of result
     *                            (<code>Seq&lt;Object></code> for indexed, <code>Void</code> for query without {@link Result}).
     * @return new query.
     * @throws RuntimeIOException            wrap around possible {@link IOException} working with source {@link Reader}.
     * @throws RuntimeClassNotFoundException wrap around {@link ClassNotFoundException}
     *                                       if query parameter references a class, that failed to be found.
     */
    <P, R> Query<P, R> loadQuery(QueryType type,
                                 QuerySource source,
                                 InjectionStrategy<P> injectionStrategy,
                                 ExtractionStrategy<R> extractionStrategy,
                                 int queryTimeoutSeconds);

    /**
     * Factory method creating default {@link QueryLoader} implementation.
     *
     * @param typesCollection   collection of {@link ParameterJdbcType} to use.
     * @param mappersCollection collection of {@link TypeMapper} to use.
     * @return new {@link QueryLoader}.
     */
    static QueryLoader of(TypesCollection typesCollection,
                          MappersCollection mappersCollection) {
        return of(
                QueryParser.defaultParser(),
                typesCollection,
                mappersCollection
        );
    }

    /**
     * Factory method creating default {@link QueryLoader} implementation.
     *
     * @param parser            a {@link QueryParser} to extract parameter info from {@link QuerySource}.
     * @param typesCollection   collection of {@link ParameterJdbcType} to use.
     * @param mappersCollection collection of {@link TypeMapper} to use.
     * @return new {@link QueryLoader}.
     */
    static QueryLoader of(QueryParser parser,
                          TypesCollection typesCollection,
                          MappersCollection mappersCollection) {
        final ReflectionsFinder reflectionsFinder = ReflectionsFinder.defaultFinder();
        return of(
                parser,
                typesCollection,
                () -> MappersFinder.defaultFinder(mappersCollection),
                () -> reflectionsFinder
        );
    }

    /**
     * Factory method creating default {@link QueryLoader} implementation.
     *
     * @param parser                    a {@link QueryParser} to extract parameter info from {@link QuerySource}.
     * @param typesCollection           collection of {@link ParameterJdbcType} to use.
     * @param mappersFinderSupplier     supplier of {@link MappersFinder} to be used in each injector/extractor.
     * @param reflectionsFinderSupplier supplier of {@link ReflectionsFinder} to be used in each injector/extractor.
     * @return new {@link QueryLoader}.
     */
    static QueryLoader of(QueryParser parser,
                          TypesCollection typesCollection,
                          Supplier<MappersFinder> mappersFinderSupplier,
                          Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        return new QueryLoaderImpl(
                QueryFactory.instance(),
                parser,
                typesCollection,
                mappersFinderSupplier,
                reflectionsFinderSupplier
        );
    }

}
