package simple.orm.jdbc;

import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.query.*;

import java.sql.SQLException;

/**
 * Simple {@link Connection} implementation.
 */
public class ConnectionImpl implements Connection {

    final DatabaseAccessPointImpl database;

    final java.sql.Connection jdbcConnection;

    ConnectionImpl(DatabaseAccessPointImpl database, java.sql.Connection connection) {
        this.database = database;
        this.jdbcConnection = connection;
    }

    @Override
    public DatabaseAccessPoint getDatabase() {
        return database;
    }

    @Override
    public void close() {
        try {
            jdbcConnection.close();
        } catch (SQLException sqlException) {
            throw new JdbcException(sqlException);
        } finally {
            database.removeConnection(this);
        }
    }

    @Override
    public void executeDDLUpdate(Query query) {
        // todo
    }

    @Override
    public int executeDMLUpdate(Query query) {
        // todo
        return 0;
    }

    @Override
    public int executeDMLUpdate(IndexedQuery query, Object... params) {
        // todo
        return 0;
    }

    @Override
    public int executeDMLUpdate(IndexedQuery query, Seq<Object> params) {
        // todo
        return 0;
    }

    @Override
    public <I> int executeDMLUpdate(NamedQuery<I, Void> query, I input) {
        // todo
        return 0;
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query, Object... params) {
        // todo
        return null;
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query, Seq<Object> params) {
        // todo
        return null;
    }

    @Override
    public Result<Seq<Object>> executeSelect(IndexedQuery query) {
        // todo
        return null;
    }

    @Override
    public <I, O> Result<O> executeSelect(NamedQuery<I, O> query, I input) {
        // todo
        return null;
    }

    @Override
    public <O> Result<O> executeSelect(NamedQuery<Void, O> query) {
        // todo
        return null;
    }

    @Override
    public <O> Result<O> executeSelect(IndexedNamedQuery<O> query, Seq<Object> params) {
        // todo
        return null;
    }

    @Override
    public <I> Result<Seq<Object>> executeSelect(NamedIndexedQuery<I> query, I input) {
        // todo
        return null;
    }

}
