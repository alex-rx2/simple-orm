package simple.orm.repo;

import simple.orm.jdbc.query.Query;
import simple.orm.loader.QueryParser;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.repo.impl.RepositoryBuilderImpl;

import java.util.function.Supplier;

/**
 * Interface for repository builder.
 * <br>
 * Creates implementation of repository from its interface.
 */
public interface RepositoryBuilder {

    /**
     * Builds a repository implementation class and returns its instance.
     * <br>
     * Implementation is build using {@link ImplementationStyle#JAVA_PROXY} style.
     *
     * @param repoInterface interface of repository.
     * @param <T>           repository interface.
     * @return repository implementation.
     * @throws RepositoryBuilderException failed to create repository.
     */
    default <T> T buildRepository(Class<T> repoInterface) {
        return buildRepository(repoInterface, ImplementationStyle.JAVA_PROXY);
    }

    /**
     * Builds a repository implementation class and returns its instance.
     *
     * @param repoInterface interface of repository.
     * @param style         the style of repository implementation.
     * @param <T>           repository interface.
     * @return repository implementation.
     * @throws RepositoryBuilderException failed to create repository.
     */
    <T> T buildRepository(Class<T> repoInterface, ImplementationStyle style);

    /**
     * Factory method for default {@link RepositoryBuilder} implementation.
     *
     * @param typesCollection   collection of {@link ParameterJdbcType} to use.
     * @param mappersCollection collection of {@link TypeMapper} to use.
     * @return default implementation.
     */
    static RepositoryBuilder of(
            TypesCollection typesCollection,
            MappersCollection mappersCollection) {
        final ReflectionsFinder reflectionsFinder = ReflectionsFinder.defaultFinder();
        return of(
                typesCollection,
                () -> MappersFinder.defaultFinder(mappersCollection),
                () -> reflectionsFinder
        );
    }

    /**
     * Factory method for default {@link RepositoryBuilder} implementation.
     *
     * @param typesCollection           collection of {@link ParameterJdbcType} to use.
     * @param mappersFinderSupplier     supplier of {@link MappersFinder} to be used in each injector/extractor.
     * @param reflectionsFinderSupplier supplier of {@link ReflectionsFinder} to be used in each injector/extractor.
     * @return default implementation.
     */
    static RepositoryBuilder of(TypesCollection typesCollection,
                                Supplier<MappersFinder> mappersFinderSupplier,
                                Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        return of(QueryParser.defaultParser(), typesCollection, mappersFinderSupplier, reflectionsFinderSupplier);
    }

    /**
     * Factory method for default {@link RepositoryBuilder} implementation.
     *
     * @param parser                    parser to use if parameters information should be extracted from query SQL.
     * @param typesCollection           collection of {@link ParameterJdbcType} to use.
     * @param mappersFinderSupplier     supplier of {@link MappersFinder} to be used in each injector/extractor.
     * @param reflectionsFinderSupplier supplier of {@link ReflectionsFinder} to be used in each injector/extractor.
     * @return default implementation.
     */
    static RepositoryBuilder of(QueryParser parser,
                                TypesCollection typesCollection,
                                Supplier<MappersFinder> mappersFinderSupplier,
                                Supplier<ReflectionsFinder> reflectionsFinderSupplier) {
        return of(
                SQLLoader.defaultLoader(),
                parser,
                QueryBuilder.of(typesCollection, mappersFinderSupplier, reflectionsFinderSupplier)
        );
    }

    /**
     * Factory method for default {@link RepositoryBuilder} implementation.
     *
     * @param sqlLoader    utility class to load query SQL from provided URIs.
     * @param queryParser  parser to use if parameters information should be extracted from query SQL.
     * @param queryBuilder builder to use for {@link Query} construction.
     * @return default implementation.
     */
    static RepositoryBuilder of(SQLLoader sqlLoader,
                                QueryParser queryParser,
                                QueryBuilder queryBuilder) {
        return new RepositoryBuilderImpl(sqlLoader, queryParser, queryBuilder);
    }

}
