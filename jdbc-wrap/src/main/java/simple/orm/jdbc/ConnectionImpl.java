package simple.orm.jdbc;

import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.exc.NamedParamsNotSupportedException;
import simple.orm.jdbc.query.Query;

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
    public <T> T execute(Query<T> query) {
        // todo
        return null;
    }

    @Override
    public <T> T execute(Query<T> query, Object... params) {
        // todo
        return null;
    }

    @Override
    public <T> T execute(Query<T> query, Traversable<Object> params) {
        // todo
        return null;
    }

    @Override
    public <T> T execute(Query<T> query, Map<String, Object> params) throws NamedParamsNotSupportedException {
        // todo
        return null;
    }
}
