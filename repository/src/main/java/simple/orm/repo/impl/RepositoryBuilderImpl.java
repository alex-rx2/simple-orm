package simple.orm.repo.impl;

import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;
import simple.orm.loader.builder.ParameterType;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.repo.ImplementationStyle;
import simple.orm.repo.RepositoryBuilder;
import simple.orm.repo.SQLLoader;
import simple.orm.repo.anno.ParameterStrategy;
import simple.orm.repo.impl.meta.MetadataCollector;
import simple.orm.repo.impl.meta.QueryMethodMeta;
import simple.orm.repo.impl.meta.RepositoryMeta;
import simple.orm.repo.impl.proxy.ProxyHandlerBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import static io.vavr.API.*;
import static simple.orm.util.StringUtils.empty;

/**
 * Default implementation of {@link RepositoryBuilder}.
 */
public class RepositoryBuilderImpl implements RepositoryBuilder {

    private final SQLLoader sqlLoader;
    private final QueryParser queryParser;
    private final QueryBuilder queryBuilder;
    private final MetadataCollector metadataCollector;
    private final ProxyHandlerBuilder handlerBuilder;

    public RepositoryBuilderImpl(SQLLoader sqlLoader,
                                 QueryParser queryParser,
                                 QueryBuilder queryBuilder) {
        this(sqlLoader, queryParser, queryBuilder, new MetadataCollector(), new ProxyHandlerBuilder());
    }

    protected RepositoryBuilderImpl(SQLLoader sqlLoader,
                                    QueryParser queryParser,
                                    QueryBuilder queryBuilder,
                                    MetadataCollector metadataCollector,
                                    ProxyHandlerBuilder handlerBuilder) {
        if (sqlLoader == null) {
            throw new NullPointerException("sqlLoader is null");
        }
        if (queryParser == null) {
            throw new NullPointerException("queryParser is null");
        }
        if (queryBuilder == null) {
            throw new NullPointerException("queryBuilder is null");
        }
        if (metadataCollector == null) {
            throw new NullPointerException("metadataCollector is null");
        }
        if (handlerBuilder == null) {
            throw new NullPointerException("handlersBuilder is null");
        }
        this.sqlLoader = sqlLoader;
        this.queryParser = queryParser;
        this.queryBuilder = queryBuilder;
        this.metadataCollector = metadataCollector;
        this.handlerBuilder = handlerBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T buildRepository(Class<T> repoInterface, ImplementationStyle style) {
        if (style == null) {
            throw new NullPointerException("style is null");
        }
        // collect meta
        final RepositoryMeta meta = metadataCollector.collectMetadata(repoInterface);
        // build repository
        return switch (style) {
            case JAVA_PROXY -> {
                // build queries, invocations handler and return proxy
                final Map<String, Query<?, ?>> queries = HashMap.ofEntries(
                        meta.queryMethods().map(m -> Tuple.of(m.methodName(), buildQuery(meta, m)))
                );
                final InvocationHandler invocationHandler = handlerBuilder.build(repoInterface, queries);
                yield (T) Proxy.newProxyInstance(
                        repoInterface.getClassLoader(),
                        new Class<?>[]{repoInterface},
                        invocationHandler
                );
            }
            case JAVA_PROXY_LAZY -> {
                // build suppliers, invocations handler and return proxy
                final Map<String, Supplier<Query<?, ?>>> suppliers = HashMap.ofEntries(
                        meta.queryMethods().map(m -> Tuple.of(m.methodName(), () -> buildQuery(meta, m)))
                );
                final InvocationHandler invocationHandler = handlerBuilder.buildLazy(repoInterface, suppliers);
                yield (T) Proxy.newProxyInstance(
                        repoInterface.getClassLoader(),
                        new Class<?>[]{repoInterface},
                        invocationHandler
                );
            }
        };
    }

    private Query<?, ?> buildQuery(RepositoryMeta repo, QueryMethodMeta method) {
        if (method.querySQL() == null) {
            method = method.replaceSQL(sqlLoader.loadFromURI(method.queryURI(), method.queryURICharset()));
        }
        if (method.paramStrat() == ParameterStrategy.PARSE_QUERY) {
            method = parseParamsIntoMeta(method);
        }
        return queryBuilder.buildQuery(
                method.type(),
                method.querySQL(),
                injectionStrategy(method),
                extractionStrategy(method),
                List.<QueryParameter>empty()
                        .appendAll(method.injectParams())
                        .appendAll(method.extractParams()),
                method.queryTimeout() < 0 ? repo.timeout() : method.queryTimeout()
        );
    }

    private QueryMethodMeta parseParamsIntoMeta(QueryMethodMeta qm) {
        QueryParser.ParsedQuery parsed = queryParser.parse(QuerySource.of(qm.querySQL()));
        Traversable<QueryParameter> queryParameters = toQueryParameters(parsed.parsedParams(), qm.targetClass());
        return qm.replaceSQL(parsed.querySQL())
                .replaceParams(
                        queryParameters.filter(qp -> qp.type() == ParameterType.INJECTION),
                        queryParameters.filter(qp -> qp.type() == ParameterType.EXTRACTION)
                );
    }

    private InjectionStrategy<?> injectionStrategy(QueryMethodMeta meta) {
        return Match(meta).of(
                Case($(m -> m.type() == QueryType.DDL), m -> InjectionStrategy.none()),
                Case($(m -> m.injectParams().isEmpty()), m -> InjectionStrategy.none()),
                Case($(m -> m.sourceClass() == null), m -> InjectionStrategy.indexed()),
                Case($(), m -> InjectionStrategy.named(m.sourceClass()))
        );
    }

    private ExtractionStrategy<?> extractionStrategy(QueryMethodMeta meta) {
        return Match(meta).of(
                Case($(m -> m.type() == QueryType.DDL), m -> ExtractionStrategy.noneDdl()),
                Case($(m -> m.type() == QueryType.DML), m -> ExtractionStrategy.noneDml()),
                Case($(m -> m.targetClass() == null), m -> ExtractionStrategy.indexed()),
                Case($(), m -> ExtractionStrategy.named(m.targetClass()))
        );
    }

    private Traversable<QueryParameter> toQueryParameters(Traversable<QueryParser.QueryParam> qParams, Class<?> targetClass) {
        // TODO move all this to QueryParser? (tweaks around labels and propNames)
        boolean extractionStartNamed = targetClass != null;
        final boolean dropGuessedLabels = !extractionStartNamed
                || qParams.find(qp -> qp.type() == QueryParser.ParamType.EXTRACTION && empty(qp.label())).isDefined();
        return qParams.map(qParam ->
                new QueryParameter(
                        ParameterType.of(qParam.type()),
                        qParam.indexWithinType(),
                        qParam.labelGuessed() && dropGuessedLabels ? null : qParam.label(),
                        qParam.propName(),
                        null,
                        qParam.mapperName(),
                        qParam.tag(),
                        null,
                        qParam.jdbcTypeName(),
                        null,
                        qParam.javaClassName()
                ));
    }

}
