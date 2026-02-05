package simple.orm.jdbc.impl;

import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import simple.orm.jdbc.Connection;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.DatabaseClosedException;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.util.Mutable;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Simple {@link DatabaseAccessPoint} implementation.
 */
public class DatabaseAccessPointImpl implements DatabaseAccessPoint {

    public static class DefaultConnectionFactory implements ConnectionFactory {
        @Override
        public Connection connection(DatabaseAccessPoint dap, java.sql.Connection jdbcConn, ResultFactory resultFactory, int defaultTimeout) {
            if (!(dap instanceof DatabaseAccessPointImpl)) {
                throw new IllegalArgumentException("DefaultConnectionFactory is designed to work only with DatabaseAccessPointImpl");
            }
            return new ConnectionImpl((DatabaseAccessPointImpl) dap, jdbcConn, resultFactory, defaultTimeout);
        }
    }

    public static class DefaultResultFactory implements ResultFactory {
        @Override
        public Result<Seq<Object>> indexed(Connection conn, ResultSet rs, IndexedExtractor extractor) {
            return ResultImpl.indexed(rs, extractor);
        }

        @Override
        public <T> Result<T> named(Connection conn, ResultSet rs, NamedExtractor<T> extractor) {
            return ResultImpl.named(rs, extractor);
        }
    }

    private final String connectionUrl;
    private final Properties connectionProperties;
    private final Driver jdbcDriver;
    private final ConnectionFactory connectionFactory;
    private final ResultFactory resultFactory;

    // cache of opened connections
    private final Mutable<HashSet<Connection>> connections = Mutable.of(HashSet.empty());
    // flag of being closed
    private boolean closed = false;

    public DatabaseAccessPointImpl(Class<? extends Driver> driverClass,
                                   String connectionUrl,
                                   Properties connectionProperties,
                                   ConnectionFactory connectionFactory,
                                   ResultFactory resultFactory) {
        this.connectionUrl = connectionUrl;
        this.connectionProperties = connectionProperties;
        // driver should be registered in DriverManager
        Driver driver = findDriver(DriverManager.getDrivers(), driverClass);
        if (driver == null) {
            // weird but try to instantiate it
            try {
                driver = driverClass.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException |
                     InstantiationException |
                     IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException("failed to instantiate driver class", e);
            }
        }
        this.jdbcDriver = driver;
        this.connectionFactory = connectionFactory;
        this.resultFactory = resultFactory;
    }

    private Driver findDriver(Enumeration<Driver> drivers, Class<? extends Driver> driverClass) {
        while (drivers.hasMoreElements()) {
            Driver d = drivers.nextElement();
            if (d.getClass() == driverClass)
                return d;
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
    public Connection connect() throws DatabaseClosedException {
        return connect(0);
    }

    @Override
    public Connection connect(int defaultTimeout) {
        if (defaultTimeout < 0) {
            throw new IllegalArgumentException("defaultTimeout is negative");
        }
        synchronized (this) {
            if (closed) {
                throw new DatabaseClosedException("database access point is closed");
            }
        }
        java.sql.Connection jdbcConnection;
        try {
            jdbcConnection = jdbcDriver.connect(connectionUrl, connectionProperties);
        } catch (SQLException e) {
            throw new JdbcException("failed to establish connection", e);
        }
        Connection connection = connectionFactory.connection(this, jdbcConnection, resultFactory, defaultTimeout);
        addConnection(connection);
        return connection;
    }

    private void addConnection(Connection conn) {
        synchronized (this) {
            if (closed) {
                try {
                    conn.close();
                    throw new DatabaseClosedException("database access point is closed");
                } catch (Exception e) {
                    DatabaseClosedException exc = new DatabaseClosedException("database access point is closed");
                    exc.addSuppressed(e);
                    throw exc;
                }
            }
            connections.apply(conns -> conns.add(conn));
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            // close all connections
            Option<JdbcException> exceptions = connections.get()
                    .toList()
                    .<Option<JdbcException>>map(conn -> {
                        try {
                            conn.close();
                            return Option.none();
                        } catch (Exception e) {
                            return Option.of(new JdbcException("jdbc connection close error", e));
                        }
                    })
                    .filter(Option::isDefined)
                    .map(Option::get)
                    .reduceLeftOption((exc1, exc2) -> {
                        exc1.addSuppressed(exc2);
                        return exc1;
                    });
            // clear connections
            connections.set(HashSet.empty());
            // set closed
            closed = true;
            // throw exception if present
            if (exceptions.isDefined()) {
                throw exceptions.get();
            }
        }
    }

    void removeConnection(Connection conn) {
        synchronized (this) {
            connections.apply(conns -> conns.remove(conn));
        }
    }

}
