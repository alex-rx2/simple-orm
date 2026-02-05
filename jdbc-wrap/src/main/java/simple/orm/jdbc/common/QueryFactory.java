package simple.orm.jdbc.common;

import io.vavr.Tuple2;
import simple.orm.jdbc.impl.query.BaseQueryImpl;
import simple.orm.jdbc.impl.query.IndexedNamedQueryImpl;
import simple.orm.jdbc.impl.query.IndexedQueryImpl;
import simple.orm.jdbc.impl.query.NamedIndexedQueryImpl;
import simple.orm.jdbc.impl.query.NamedQueryImpl;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.IndexedNamedQuery;
import simple.orm.jdbc.query.IndexedQuery;
import simple.orm.jdbc.query.NamedIndexedQuery;
import simple.orm.jdbc.query.NamedParametersMap;
import simple.orm.jdbc.query.NamedQuery;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

/**
 * Simple factory to create queries.
 * <br>
 * For default factory {@link #defaultFactory()} named parameters in SQL query
 * are strings of format <nobr><code>:[a-zA-Z0-9_]+</code></nobr> that are not inside comments or SQL string literals.
 * Said parameters are collected and replaced inside query with <code>?</code>.
 */
public class QueryFactory {

    /**
     * Interface for utility class that extracts named parameters from SQL query replacing them with ? in the query.
     */
    public interface NamedParametersProcessor {
        /**
         * This method extracts named parameters from SQL query, replacing them with ? in the query.
         *
         * @param sql SQL query.
         * @return {@link Tuple2} of processed query and extracted named parameters.
         */
        Tuple2<String, NamedParametersMap> process(String sql);
    }

    private static final QueryFactory defaultFactory = new QueryFactory(new DefaultNPProcessor());

    public static QueryFactory defaultFactory() {
        return defaultFactory;
    }

    private final NamedParametersProcessor namedParametersProcessor;

    public QueryFactory(NamedParametersProcessor extractor) {
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        this.namedParametersProcessor = extractor;
    }

    /**
     * Creates SQL DDL query.
     */
    public Query ddlQuery(String sql) {
        return ddlQuery(sql, -1);
    }

    /**
     * Creates SQL DDL query.
     */
    public Query ddlQuery(
            String sql,
            int queryTimeoutSeconds
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        return new BaseQueryImpl(QueryType.EXECUTE_UPDATE, sql, queryTimeoutSeconds);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as a sequence of java object (injected by index).
     */
    public IndexedQuery iudQuery(String sql, IndexedInjector injector) {
        return iudQuery(sql, -1, injector);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as a sequence of java object (injected by index).
     */
    public IndexedQuery iudQuery(
            String sql,
            int queryTimeoutSeconds,
            IndexedInjector injector
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        return new IndexedQueryImpl(QueryType.EXECUTE_QUERY, sql, queryTimeoutSeconds, injector, null);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as properties of java object.
     */
    public <I> NamedQuery<I, Void> iudQuery(String sql, NamedInjector<I> injector) {
        return iudQuery(sql, -1, injector);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as properties of java object.
     */
    public <I> NamedQuery<I, Void> iudQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<I> injector
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        Tuple2<String, NamedParametersMap> processed = namedParametersProcessor.process(sql);
        return new NamedQueryImpl<>(QueryType.EXECUTE_QUERY, processed._1, queryTimeoutSeconds, injector, null, processed._2);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query has no parameters.
     */
    public Query iudQueryWithoutParameters(String sql) {
        return iudQueryWithoutParameters(sql, -1);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query has no parameters.
     */
    public Query iudQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        return new IndexedQueryImpl(QueryType.EXECUTE_QUERY, sql, queryTimeoutSeconds, null, null);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public IndexedQuery selectQuery(String sql, IndexedInjector injector, IndexedExtractor extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public IndexedQuery selectQuery(
            String sql,
            int queryTimeoutSeconds,
            IndexedInjector injector,
            IndexedExtractor extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        return new IndexedQueryImpl(QueryType.EXECUTE_QUERY, sql, queryTimeoutSeconds, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <I, O> NamedQuery<I, O> selectQuery(String sql, NamedInjector<I> injector, NamedExtractor<O> extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <I, O> NamedQuery<I, O> selectQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<I> injector,
            NamedExtractor<O> extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        Tuple2<String, NamedParametersMap> processed = namedParametersProcessor.process(sql);
        return new NamedQueryImpl<>(QueryType.EXECUTE_QUERY, processed._1, queryTimeoutSeconds, injector, extractor, processed._2);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <O> IndexedNamedQuery<O> selectQuery(String sql, IndexedInjector injector, NamedExtractor<O> extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <O> IndexedNamedQuery<O> selectQuery(
            String sql,
            int queryTimeoutSeconds,
            IndexedInjector injector,
            NamedExtractor<O> extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        return new IndexedNamedQueryImpl<>(QueryType.EXECUTE_QUERY, sql, queryTimeoutSeconds, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public <I> NamedIndexedQuery<I> selectQuery(String sql, NamedInjector<I> injector, IndexedExtractor extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public <I> NamedIndexedQuery<I> selectQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<I> injector,
            IndexedExtractor extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        Tuple2<String, NamedParametersMap> processed = namedParametersProcessor.process(sql);
        return new NamedIndexedQueryImpl<>(QueryType.EXECUTE_QUERY, processed._1, queryTimeoutSeconds, injector, extractor, processed._2);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public IndexedQuery selectQueryWithoutParameters(String sql, IndexedExtractor extractor) {
        return selectQueryWithoutParameters(sql, -1, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public IndexedQuery selectQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds,
            IndexedExtractor extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        return new IndexedQueryImpl(QueryType.EXECUTE_QUERY, sql, queryTimeoutSeconds, null, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <O> NamedQuery<Void, O> selectQueryWithoutParameters(String sql, NamedExtractor<O> extractor) {
        return selectQueryWithoutParameters(sql, -1, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <O> NamedQuery<Void, O> selectQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds,
            NamedExtractor<O> extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        Tuple2<String, NamedParametersMap> processed = namedParametersProcessor.process(sql);
        return new NamedQueryImpl<>(QueryType.EXECUTE_QUERY, processed._1, queryTimeoutSeconds, null, extractor, processed._2);
    }

}
