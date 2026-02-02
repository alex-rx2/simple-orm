package simple.orm.jdbc;

import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import simple.orm.jdbc.exc.DatabaseClosedException;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.map.out.IndexedExtractor;
import simple.orm.jdbc.map.out.NamedExtractor;

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

    protected static class DefaultConnectionFactory implements ConnectionFactory {
        @Override
        public Connection connection(DatabaseAccessPoint dap, java.sql.Connection jdbcConn, ResultFactory resultFactory) {
            if (!(dap instanceof DatabaseAccessPointImpl)) {
                throw new IllegalArgumentException("DefaultConnectionFactory is designed to work only with DatabaseAccessPointImpl");
            }
            return new ConnectionImpl((DatabaseAccessPointImpl) dap, jdbcConn, resultFactory);
        }
    }

    protected static class DefaultResultFactory implements ResultFactory {
        @Override
        public Result<Seq<Object>> indexed(Connection conn, ResultSet rs, IndexedExtractor extractor) {
            return ResultImpl.indexed(rs, extractor);
        }

        @Override
        public <T> Result<T> named(Connection conn, ResultSet rs, NamedExtractor<T> extractor) {
            return ResultImpl.named(rs, extractor);
        }
    }

    protected final String connectionUrl;
    protected final Properties connectionProperties;
    protected final Driver jdbcDriver;
    protected final ConnectionFactory connectionFactory;
    protected final ResultFactory resultFactory;

    // cache of opened connections
    protected HashSet<Connection> connections = HashSet.empty();
    // flag of being closed
    protected boolean closed = false;

    protected DatabaseAccessPointImpl(Class<? extends Driver> driverClass,
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
                driver = driverClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("failed to instantiate driver class", e);
            }
        }
        this.jdbcDriver = driver;
        this.connectionFactory = connectionFactory;
        this.resultFactory = resultFactory;
    }

    protected Driver findDriver(Enumeration<Driver> drivers, Class<? extends Driver> driverClass) {
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
    public Connection connect() {
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
        Connection connection = connectionFactory.connection(this, jdbcConnection, resultFactory);
        addConnection(connection);
        return connection;
    }

    protected void addConnection(Connection conn) {
        synchronized (this) {
            if (closed) {
                throw closeConnection(conn, Option.none())
                        .flatMap(exc -> {
                            DatabaseClosedException dcExc = new DatabaseClosedException("database access point is closed");
                            dcExc.addSuppressed(exc);
                            return Option.of(dcExc);
                        })
                        .getOrElse(() -> new DatabaseClosedException("database access point is closed"));
            }
            connections = connections.add(conn);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
        }
        // close all connections
        final JdbcException closeException =
                connections
                        .foldLeft(
                                Option.<JdbcException>none(),
                                (exceptionOpt, conn) -> closeConnection(conn, exceptionOpt)
                        )
                        .getOrNull();
        // clear connections
        synchronized (this) {
            connections = HashSet.empty();
            closed = true;
        }
        // throw exception if present
        if (closeException != null) {
            throw closeException;
        }
    }

    protected void removeConnection(Connection conn) {
        synchronized (this) {
            connections = connections.remove(conn);
        }
    }

    protected Option<JdbcException> closeConnection(Connection conn, Option<JdbcException> exceptionOpt) {
        try {
            conn.close();
            return exceptionOpt;
        } catch (Exception e) {
            return exceptionOpt
                    .map(exc -> combineExceptions(exc, e))
                    .orElse(() -> Option.of(new JdbcException("jdbc connection close error", e)));
        }
    }

    protected static JdbcException combineExceptions(JdbcException exc, Exception suppressed) {
        exc.addSuppressed(suppressed);
        return exc;
    }
}
