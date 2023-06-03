package simple.orm.jdbc;

/**
 * Simple {@link Connection} implementation.
 */
class ConnectionImpl implements Connection {

    private final DatabaseAccessPointImpl database;

    private final java.sql.Connection jdbcConnection;

    ConnectionImpl(DatabaseAccessPointImpl database, java.sql.Connection connection) {
        this.database = database;
        this.jdbcConnection = connection;
    }

    @Override
    public DatabaseAccessPoint getDatabase() {
        return database;
    }

    @Override
    public <T> T execute(Query query) {
        // todo
        return null;
    }

    @Override
    public void close() throws Exception {
        try {
            jdbcConnection.close();
        } finally {
            database.removeConnection(this);
        }
    }
}
