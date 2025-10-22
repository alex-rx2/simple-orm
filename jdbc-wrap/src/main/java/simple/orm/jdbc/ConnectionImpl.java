package simple.orm.jdbc;

import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.query.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple {@link Connection} implementation.
 */
public class ConnectionImpl implements Connection {

    protected final DatabaseAccessPointImpl database;
    protected final java.sql.Connection jdbcConnection;
    protected final DatabaseAccessPoint.ResultFactory resultFactory;

    protected Statement currentStatement;
    protected String lastPreparedQuery;
    protected ResultSet currentResultSet;

    protected ConnectionImpl(DatabaseAccessPointImpl database,
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

    protected Statement obtainSimpleStatement(int timeout) {
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

    protected PreparedStatement obtainPreparedStatement(String sql, int timeout) {
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
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public int executeDMLUpdate(IndexedQuery query, Seq<Object> params) {
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
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, params);
            return resultFactory.indexed(this, stmt.executeQuery(), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query, Seq<Object> params) {
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
        try {
            Statement stmt = obtainSimpleStatement(query.getQueryTimeout());
            return resultFactory.indexed(this, stmt.executeQuery(query.getSQLQuery()), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <I, O> Result<O> executeSelect(NamedQuery<I, O> query, I input) {
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
        try {
            Statement stmt = obtainSimpleStatement(query.getQueryTimeout());
            return resultFactory.named(this, stmt.executeQuery(query.getSQLQuery()), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public <O> Result<O> executeSelect(IndexedNamedQuery<O> query, Seq<Object> params) {
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
        try {
            PreparedStatement stmt = obtainPreparedStatement(query.getSQLQuery(), query.getQueryTimeout());
            query.getInjector().injectParameters(stmt, input, query.getParametersMap());
            return resultFactory.indexed(this, stmt.executeQuery(), query.getExtractor());
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
