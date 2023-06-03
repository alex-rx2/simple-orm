package simple.orm.jdbc;

import io.vavr.collection.HashSet;
import io.vavr.control.Option;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Simple {@link DatabaseAccessPoint} implementation.
 */
public class DatabaseAccessPointImpl implements DatabaseAccessPoint {

    private final String connectionUrl;
    private final Properties connectionProperties;
    private final Driver jdbcDriver;

    // cache of opened connections
    private HashSet<Connection> connections = HashSet.empty();
    // flag of being closed
    private boolean closed = false;

    DatabaseAccessPointImpl(Class<? extends Driver> driverClass, String connectionUrl, Properties connectionProperties) {
        this.connectionUrl = connectionUrl;
        this.connectionProperties = connectionProperties;
        // driver should be registered in DriverManager
        Driver driver = findDriver(DriverManager.getDrivers(), driverClass);
        if (driver == null) {
            // weird but try to instantiate it
            try {
                driver = driverClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("failed to instantiate driver class", e);
            }
        }
        this.jdbcDriver = driver;
    }

    private Driver findDriver(Enumeration<Driver> drivers, Class<? extends Driver> driverClass) {
        while (drivers.hasMoreElements()) {
            Driver d = drivers.nextElement();
            if (d.getClass() == driverClass) return d;
        }
        return null;
    }

    @Override
    public Class<? extends Driver> getDriverClass() {
        return jdbcDriver.getClass();
    }

    @Override
    public String getConnectionURL() {
        return connectionUrl;
    }

    @Override
    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public Connection connect() {
        synchronized (this) {
            if (closed) {
                throw new DatabaseClosedException("database is closed");
            }
        }
        java.sql.Connection jdbcConnection;
        try {
            jdbcConnection = jdbcDriver.connect(connectionUrl, connectionProperties);
        } catch (SQLException e) {
            throw new JdbcException("failed to establish connection", e);
        }
        ConnectionImpl connection = new ConnectionImpl(this, jdbcConnection);
        addConnection(connection);
        return connection;
    }

    private void addConnection(Connection conn) {
        synchronized (this) {
            if (closed) {
                throw tryToClose(conn, Option.none())
                        .flatMap(exc -> {
                            DatabaseClosedException dcExc = new DatabaseClosedException("database is closed");
                            dcExc.addSuppressed(exc);
                            return Option.of(dcExc);
                        })
                        .getOrElse(() -> new DatabaseClosedException("database is closed"));
            }
            connections = connections.add(conn);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            closed = true;
        }
        // close all connections
        final JdbcException closeException =
                connections
                        .foldLeft(
                                Option.<JdbcException>none(),
                                (exceptionOpt, conn) -> tryToClose(conn, exceptionOpt)
                        )
                        .getOrNull();
        // clear connections
        synchronized (this) {
            connections = HashSet.empty();
        }
        // throw exception if present
        if (closeException != null) {
            throw closeException;
        }
    }

    void removeConnection(Connection conn) {
        synchronized (this) {
            connections = connections.remove(conn);
        }
    }

    private Option<JdbcException> tryToClose(Connection conn, Option<JdbcException> exceptionOpt) {
        try {
            conn.close();
            return exceptionOpt;
        } catch (Exception e) {
            return exceptionOpt
                    .map(exc -> combineExceptions(exc, e))
                    .orElse(() -> Option.of(new JdbcException("jdbc connection close error", e)));
        }
    }

    private static JdbcException combineExceptions(JdbcException exc, Exception suppressed) {
        exc.addSuppressed(suppressed);
        return exc;
    }
}
