package simple.orm.jdbc.impl;

import io.vavr.collection.Seq;
import simple.orm.jdbc.Connection;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.query.HasIndexedExtractor;
import simple.orm.jdbc.query.HasIndexedInjector;
import simple.orm.jdbc.query.HasNamedExtractor;
import simple.orm.jdbc.query.HasNamedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple {@link Connection} implementation.
 */
public class ConnectionImpl implements Connection {

    private final DatabaseAccessPointImpl database;
    private final java.sql.Connection jdbcConnection;
    private final DatabaseAccessPoint.ResultFactory resultFactory;
    private final int defaultTimeout;

    private Statement currentStatement;
    private String lastPreparedQuery;
    private ResultSet currentResultSet;

    public ConnectionImpl(DatabaseAccessPointImpl database,
                          java.sql.Connection connection,
                          DatabaseAccessPoint.ResultFactory resultFactory,
                          int defaultTimeout) {
        this.database = database;
        this.jdbcConnection = connection;
        this.resultFactory = resultFactory;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public DatabaseAccessPoint getDatabase() {
        return database;
    }

    @Override
    public java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }

    @Override
    public void releaseResources() {
        try {
            if (currentResultSet != null) {
                currentResultSet.close();
                currentResultSet = null;
            }
            if (currentStatement != null) {
                currentStatement.close();
                currentStatement = null;
                lastPreparedQuery = null;
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public void close() {
        // close connection
        try {
            jdbcConnection.close();
            currentResultSet = null;
            currentStatement = null;
            lastPreparedQuery = null;
        } catch (SQLException sqlException) {
            throw new JdbcException(sqlException);
        } finally {
            database.removeConnection(this);
        }
    }

    @Override
    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    private Statement obtainSimpleStatement(int timeout) {
        try {
            if (currentResultSet != null) {
                currentResultSet.close();
                currentResultSet = null;
            }
            if (currentStatement instanceof PreparedStatement) {
                currentStatement.close();
                currentStatement = null;
                lastPreparedQuery = null;
            }
            if (currentStatement == null) {
                currentStatement = jdbcConnection.createStatement();
            }
            currentStatement.setQueryTimeout(timeout);
            return currentStatement;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    private PreparedStatement obtainPreparedStatement(String sql, int timeout) {
        try {
            if (currentResultSet != null) {
                currentResultSet.close();
                currentResultSet = null;
            }
            // use == not equals for speed and cause in most cases query will be exactly same not other equal string
            // same query, statement can be reused, clear parameters
            if (currentStatement instanceof PreparedStatement && lastPreparedQuery == sql) {
                ((PreparedStatement) currentStatement).clearParameters();
            } else if (currentStatement != null) {
                currentStatement.close();
                currentStatement = null;
                lastPreparedQuery = null;
            }
            if (currentStatement == null) {
                currentStatement = jdbcConnection.prepareStatement(sql);
                lastPreparedQuery = sql;
            }
            currentStatement.setQueryTimeout(timeout);
            return (PreparedStatement) currentStatement;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public void executeDDLQuery(Query<Void, Void> query) {
        validate(query, QueryType.DDL, true, false, true);
        try {
            obtainSimpleStatement(getTimeoutFor(query)).executeUpdate(query.getSQLQuery());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <P> int executeDMLQuery(Query<P, Integer> query, Object... params) {
        validate(query, QueryType.DML, false, false, true);
        try {
            Statement stmt = obtainStatementForQuery(query);
            injectParameters(query, stmt, params);
            return executeUpdate(query, stmt);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <P, R> Result<R> executeSelect(Query<P, R> query, Object... params) {
        validate(query, QueryType.SELECT, false, true, false);
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        try {
            Statement stmt = obtainStatementForQuery(query);
            injectParameters(query, stmt, params);
            ResultSet rs = executeQuery(query, stmt);
            currentResultSet = rs;
            return extractResult(query, rs);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <QP, QR, ER> ER executeAnyQuery(Query<QP, QR> query, Object... params) {
        return switch (query.getType()) {
            case DDL -> {
                if (params.length > 0) {
                    throw new IllegalArgumentException("no params expected for DDL query");
                }
                executeDDLQuery((Query<Void, Void>) query);
                yield null;
            }
            case DML -> {
                int result = executeDMLQuery((Query<?, Integer>) query, params);
                yield (ER) (Integer) result;
            }
            case SELECT -> (ER) executeSelect(query, params);
        };
    }

    private <P, R> Statement obtainStatementForQuery(Query<P, R> query) {
        if (query instanceof HasIndexedInjector || query instanceof HasNamedInjector<?>) {
            return obtainPreparedStatement(query.getSQLQuery(), getTimeoutFor(query));
        } else {
            return obtainSimpleStatement(getTimeoutFor(query));
        }
    }

    @SuppressWarnings("unchecked")
    private <P, R> void injectParameters(Query<P, R> query, Statement stmt, Object[] params) {
        if (stmt instanceof PreparedStatement pstmt) {
            if (query instanceof HasIndexedInjector) {
                if (params.length == 1 && params[0] instanceof Seq<?>) {
                    ((HasIndexedInjector) query).getInjector().injectParameters(pstmt, (Seq<Object>) params[0]);
                } else {
                    ((HasIndexedInjector) query).getInjector().injectParameters(pstmt, params);
                }
            } else {
                HasNamedInjector<Object> hasNamedInjector = (HasNamedInjector<Object>) query;
                if (params.length != 1) {
                    throw new IllegalArgumentException("params should contain exactly one object for NamedInjector");
                }
                hasNamedInjector.getInjector().injectParameters(pstmt, params[0]);
            }
        } else {
            if (params.length > 0) {
                throw new IllegalArgumentException("no params expected for a query without injectors");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <R, P> Result<R> extractResult(Query<P, R> query, ResultSet rs) {
        if (query instanceof HasIndexedExtractor) {
            return (Result<R>) resultFactory.indexed(this, rs, ((HasIndexedExtractor) query).getExtractor());
        } else {
            return resultFactory.named(this, rs, ((HasNamedExtractor<R>) query).getExtractor());
        }
    }

    private <P, R> int executeUpdate(Query<P, R> query, Statement stmt) throws SQLException {
        if (stmt instanceof PreparedStatement pstmt) {
            return pstmt.executeUpdate();
        } else {
            return stmt.executeUpdate(query.getSQLQuery());
        }
    }

    private static <P, R> ResultSet executeQuery(Query<P, R> query, Statement stmt) throws SQLException {
        if (stmt instanceof PreparedStatement) {
            return ((PreparedStatement) stmt).executeQuery();
        } else {
            return stmt.executeQuery(query.getSQLQuery());
        }
    }

    private void validate(Query<?, ?> query,
                          QueryType type,
                          boolean requireNoInjector,
                          boolean requireAnyExtractor,
                          boolean requireNoExtractor
    ) {
        if (query.getType() != type) {
            throw new IllegalArgumentException("wrong query type: " + query.getType());
        }
        if (requireNoInjector && (query instanceof HasIndexedInjector || query instanceof HasNamedInjector<?>)) {
            throw new IllegalArgumentException("query should have no injector");
        }
        if (requireAnyExtractor && !(query instanceof HasIndexedExtractor || query instanceof HasNamedExtractor<?>)) {
            throw new IllegalArgumentException("query has no extractor");
        }
        if (requireNoExtractor && (query instanceof HasIndexedExtractor || query instanceof HasNamedExtractor<?>)) {
            throw new IllegalArgumentException("query should have no extractor");
        }
    }

    private int getTimeoutFor(Query<?, ?> query) {
        int queryTimeout = query.getQueryTimeout();
        return queryTimeout < 0 ? defaultTimeout : queryTimeout;
    }

}
