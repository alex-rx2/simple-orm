package simple.orm.jdbc.query;

import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import simple.orm.jdbc.impl.query.BasicQuery;
import simple.orm.jdbc.impl.query.IndexedIndexedQuery;
import simple.orm.jdbc.impl.query.IndexedNamedQuery;
import simple.orm.jdbc.impl.query.IndexedParamsNoResultSetQuery;
import simple.orm.jdbc.impl.query.NamedIndexedQuery;
import simple.orm.jdbc.impl.query.NamedNamedQuery;
import simple.orm.jdbc.impl.query.NamedParamsNoResultSetQuery;
import simple.orm.jdbc.impl.query.NoParamsIndexedQuery;
import simple.orm.jdbc.impl.query.NoParamsNamedQuery;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;

/**
 * Simple factory to create queries.
 * <br>
 * For default factory {@link #defaultFactory()} named parameters in SQL query
 * are strings of format <nobr><code>:[a-zA-Z0-9_]+</code></nobr> that are not inside comments or SQL string literals.
 * Said parameters are collected and replaced inside query with <code>?</code>.
 */
@Deprecated
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
    public Query<Void, Void> ddlQuery(String sql) {
        return ddlQuery(sql, -1);
    }

    /**
     * Creates SQL DDL query.
     */
    public Query<Void, Void> ddlQuery(
            String sql,
            int queryTimeoutSeconds
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        return new BasicQuery<>(QueryType.DDL, sql, queryTimeoutSeconds);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as a sequence of java object (injected by index).
     */
    public Query<Seq<Object>, Integer> iudQuery(String sql, IndexedInjector injector) {
        return iudQuery(sql, -1, injector);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as a sequence of java object (injected by index).
     */
    public Query<Seq<Object>, Integer> iudQuery(
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
        return new IndexedParamsNoResultSetQuery<>(QueryType.DML, sql, queryTimeoutSeconds, injector);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as properties of java object.
     */
    public <P> Query<P, Integer> iudQuery(String sql, NamedInjector<P> injector) {
        return iudQuery(sql, -1, injector);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query parameters are represented as properties of java object.
     */
    public <P> Query<P, Integer> iudQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<P> injector
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        Tuple2<String, NamedParametersMap> processed = namedParametersProcessor.process(sql);
        return new NamedParamsNoResultSetQuery<>(QueryType.DML, processed._1, queryTimeoutSeconds, injector, processed._2);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query has no parameters.
     */
    public Query<Void, Integer> iudQueryWithoutParameters(String sql) {
        return iudQueryWithoutParameters(sql, -1);
    }

    /**
     * Creates SQL INSERT/UPDATE/DELETE query.
     * Query has no parameters.
     */
    public Query<Void, Integer> iudQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        return new BasicQuery<>(QueryType.DML, sql, queryTimeoutSeconds);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public Query<Seq<Object>, Seq<Object>> selectQuery(String sql, IndexedInjector injector, IndexedExtractor extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public Query<Seq<Object>, Seq<Object>> selectQuery(
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
        return new IndexedIndexedQuery(QueryType.SELECT, sql, queryTimeoutSeconds, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <P, R> Query<P, R> selectQuery(String sql, NamedInjector<P> injector, NamedExtractor<R> extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <P, R> Query<P, R> selectQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<P> injector,
            NamedExtractor<R> extractor
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
        return new NamedNamedQuery<>(QueryType.SELECT, processed._1, queryTimeoutSeconds, injector, processed._2, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <R> Query<Seq<Object>, R> selectQuery(String sql, IndexedInjector injector, NamedExtractor<R> extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as a sequence of java object (injected by index).
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <R> Query<Seq<Object>, R> selectQuery(
            String sql,
            int queryTimeoutSeconds,
            IndexedInjector injector,
            NamedExtractor<R> extractor
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
        return new IndexedNamedQuery<>(QueryType.SELECT, sql, queryTimeoutSeconds, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public <P> Query<P, Seq<Object>> selectQuery(String sql, NamedInjector<P> injector, IndexedExtractor extractor) {
        return selectQuery(sql, -1, injector, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query parameters are represented as properties of java object.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public <P> Query<P, Seq<Object>> selectQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<P> injector,
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
        return new NamedIndexedQuery<>(QueryType.SELECT, processed._1, queryTimeoutSeconds, injector, processed._2, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public Query<Void, Seq<Object>> selectQueryWithoutParameters(String sql, IndexedExtractor extractor) {
        return selectQueryWithoutParameters(sql, -1, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as a sequence of objects (extracted by index).
     */
    public Query<Void, Seq<Object>> selectQueryWithoutParameters(
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
        return new NoParamsIndexedQuery(QueryType.SELECT, sql, queryTimeoutSeconds, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <R> Query<Void, R> selectQueryWithoutParameters(String sql, NamedExtractor<R> extractor) {
        return selectQueryWithoutParameters(sql, -1, extractor);
    }

    /**
     * Creates SQL SELECT.
     * Query has no parameters.
     * Each ResultSet row is represented as java object (properties of said object).
     */
    public <R> Query<Void, R> selectQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds,
            NamedExtractor<R> extractor
    ) {
        if (sql == null) {
            throw new NullPointerException("sql is null");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        return new NoParamsNamedQuery<>(QueryType.SELECT, sql, queryTimeoutSeconds, extractor);
    }

}
