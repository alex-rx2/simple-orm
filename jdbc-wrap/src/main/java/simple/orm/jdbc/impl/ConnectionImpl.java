package simple.orm.jdbc.impl;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.Connection;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.query.IndexedNamedQuery;
import simple.orm.jdbc.query.IndexedQuery;
import simple.orm.jdbc.query.NamedIndexedQuery;
import simple.orm.jdbc.query.NamedQuery;
import simple.orm.jdbc.query.Query;

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

    private Statement currentStatement;
    private String lastPreparedQuery;
    private ResultSet currentResultSet;

    public ConnectionImpl(DatabaseAccessPointImpl database,
                             java.sql.Connection connection,
                             DatabaseAccessPoint.ResultFactory resultFactory) {
        this.database = database;
        this.jdbcConnection = connection;
        this.resultFactory = resultFactory;
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
            if (currentStatement instanceof PreparedStatement) {
                if (lastPreparedQuery == sql) {
                    // use == not equals for speed and cause in most cases query will be exactly same not other equal string
                    // same query, statement can be reused, clear parameters
                    ((PreparedStatement) currentStatement).clearParameters();
                } else {
                    currentStatement.close();
                    currentStatement = null;
                    lastPreparedQuery = null;
                }
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
    public void executeDDLUpdate(Query query) {
        try {
            obtainSimpleStatement(query.getQueryTimeout()).executeUpdate(query.getSQLQuery());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public int executeDMLUpdate(Query query) {
        try {
            return obtainSimpleStatement(query.getQueryTimeout()).executeUpdate(query.getSQLQuery());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public int executeDMLUpdate(IndexedQuery query, Object... params) {
        return executeDMLUpdate(query, List.of(params));
    }

    @Override
    public int executeDMLUpdate(IndexedQuery query, Seq<Object> params) {
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        if (query.getInjector() == null) {
            throw new NullPointerException("query.injector is null");
        }
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <I> int executeDMLUpdate(NamedQuery<I, Void> query, I input) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        if (query.getInjector() == null) {
            throw new NullPointerException("query.injector is null");
        }
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, input, query.getParametersMap());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query, Object... params) {
        return executeSelect(query, List.of(params));
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query, Seq<Object> params) {
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        if (query.getInjector() == null) {
            throw new NullPointerException("query.injector is null");
        }
        if (query.getExtractor() == null) {
            throw new NullPointerException("query.extractor is null");
        }
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, params);
            return resultFactory.indexed(this, stmt.executeQuery(), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query) {
        if (query.getExtractor() == null) {
            throw new NullPointerException("query.extractor is null");
        }
        try {
            Statement stmt = obtainSimpleStatement(query.getQueryTimeout());
            return resultFactory.indexed(this, stmt.executeQuery(query.getSQLQuery()), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <I, O> Result<O> executeSelect(NamedQuery<I, O> query, I input) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        if (query.getInjector() == null) {
            throw new NullPointerException("query.injector is null");
        }
        if (query.getExtractor() == null) {
            throw new NullPointerException("query.extractor is null");
        }
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, input, query.getParametersMap());
            return resultFactory.named(this, stmt.executeQuery(), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <O> Result<O> executeSelect(NamedQuery<Void, O> query) {
        if (query.getExtractor() == null) {
            throw new NullPointerException("query.extractor is null");
        }
        try {
            Statement stmt = obtainSimpleStatement(query.getQueryTimeout());
            return resultFactory.named(this, stmt.executeQuery(query.getSQLQuery()), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <O> Result<O> executeSelect(IndexedNamedQuery<O> query, Object... params) {
        return executeSelect(query, List.of(params));
    }

    @Override
    public <O> Result<O> executeSelect(IndexedNamedQuery<O> query, Seq<Object> params) {
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        if (query.getInjector() == null) {
            throw new NullPointerException("query.injector is null");
        }
        if (query.getExtractor() == null) {
            throw new NullPointerException("query.extractor is null");
        }
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, params);
            return resultFactory.named(this, stmt.executeQuery(), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <I> Result<Seq<Object>> executeSelect(NamedIndexedQuery<I> query, I input) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        if (query.getInjector() == null) {
            throw new NullPointerException("query.injector is null");
        }
        if (query.getExtractor() == null) {
            throw new NullPointerException("query.extractor is null");
        }
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, input, query.getParametersMap());
            return resultFactory.indexed(this, stmt.executeQuery(), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
