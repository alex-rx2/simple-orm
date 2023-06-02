package simple.orm.jdbc;

import io.vavr.collection.HashSet;
import io.vavr.control.Option;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Simple {@link Database} implementation.
 *
 * TODO merge with configuration?
 */
public class DatabaseImpl implements Database {

    private final Configuration configuration;
    private final Driver jdbcDriver;

    // cache of opened connections
    private HashSet<Connection> connections = HashSet.empty();
    // flag of being closed
    private boolean closed = false;

    DatabaseImpl(Configuration configuration) {
        this.configuration = configuration;
        // driver should be registered in DriverManager
        Driver driver = findDriver(DriverManager.getDrivers(), configuration.getDriverClass());
        if (driver == null) {
            // weird but try to instantiate it
            try {
                driver = configuration.getDriverClass().newInstance();
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
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Connection connect() {
        synchronized (this) {
            if (closed) {
                throw new RuntimeException("database is closed");
            }
        }
        java.sql.Connection jdbcConnection;
        try {
            jdbcConnection = jdbcDriver.connect(configuration.getConnectionURL(), configuration.getConnectionProperties());
        } catch (SQLException e) {
            throw new RuntimeException("failed to establish connection", e);
        }
        ConnectionImpl connection = new ConnectionImpl(this, jdbcConnection);
        addConnection(connection);
        return connection;
    }

    private void addConnection(Connection conn) {
        synchronized (this) {
            if (closed) {
                throw tryToClose(conn, Option.none())
                        .flatMap(exc -> Option.of(new RuntimeException("database is closed", exc)))
                        .getOrElse(() -> new RuntimeException("database is closed"));
            }
            connections = connections.add(conn);
        }
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            closed = true;
        }
        // close all connections
        final Exception closeException =
                connections
                        .foldLeft(
                                Option.<Exception>none(),
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

    private Option<Exception> tryToClose(Connection conn, Option<Exception> exceptionOpt) {
        try {
            conn.close();
            return exceptionOpt;
        } catch (Exception e) {
            return exceptionOpt
                    .map(exc -> combineExceptions(exc, e))
                    .orElse(() -> Option.of(e));
        }
    }

    private static Exception combineExceptions(Exception exc, Exception suppressed) {
        exc.addSuppressed(suppressed);
        return exc;
    }
}
