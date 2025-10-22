package simple.orm.jdbc;

import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.DatabaseClosedException;
import simple.orm.jdbc.map.out.IndexedExtractor;
import simple.orm.jdbc.map.out.NamedExtractor;

import java.sql.Driver;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Abstraction of configured database access point.
 * <br>
 * Opens and manages connections.
 */
public interface DatabaseAccessPoint extends AutoCloseable {

    /**
     * Factory of {@link Connection} implementations.
     */
    interface ConnectionFactory {
        Connection connection(DatabaseAccessPoint dap, java.sql.Connection jdbcConn, ResultFactory resultFactory);
    }

    /**
     * Factory of {@link Result} implementations.
     */
    interface ResultFactory {
        Result<Seq<Object>> indexed(Connection conn, ResultSet rs, IndexedExtractor extractor);

        <T> Result<T> named(Connection conn, ResultSet rs, NamedExtractor<T> extractor);
    }

    /**
     * Get JDBC driver class to be used.
     *
     * @return JDBC driver class.
     */
    Class<? extends Driver> getDriverClass();

    /**
     * Get JDBC URL to be used.
     *
     * @return JDBC URL.
     */
    String getConnectionURL();

    /**
     * Get database connection properties.
     *
     * @return database connection properties
     */
    Properties getConnectionProperties();

    /**
     * Is access point closed.
     *
     * @return {@code true} if closed, {@code false} otherwise.
     */
    boolean isClosed();

    /**
     * Obtain connection to database.
     *
     * @return connection.
     * @throws DatabaseClosedException this database access point was closed.
     */
    Connection connect() throws DatabaseClosedException;

    /**
     * Close all managed connections and makes access point closed (unable to open new connections).
     */
    @Override
    void close();

    /**
     * Builder shortcut factory method.
     *
     * @return new {@link Builder}.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class.
     */
    class Builder {
        private String driverClassName;
        private Class<? extends Driver> driverClass;
        private String connectionUrl;
        private Properties connectionProperties;
        private ConnectionFactory connectionFactory;
        private ResultFactory resultFactory;

        public Builder driverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
            return this;
        }

        public Builder driverClass(Class<? extends Driver> driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        public Builder connectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
            return this;
        }

        public Builder connectionProperties(Properties connectionProperties) {
            this.connectionProperties = connectionProperties;
            return this;
        }

        public Builder connectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder resultFactory(ResultFactory resultFactory) {
            this.resultFactory = resultFactory;
            return this;
        }

        @SuppressWarnings("unchecked")
        public DatabaseAccessPoint build() {
            if (driverClass == null && driverClassName == null) {
                throw new NullPointerException("driverClass and driverClassName are both null");
            } else if (driverClass == null) {
                Class<?> aClass;
                try {
                    aClass = Class.forName(driverClassName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("class not found", e);
                }
                if (!Driver.class.isAssignableFrom(aClass)) {
                    throw new IllegalArgumentException("driverClassName class is not a Driver class");
                }
                driverClass = (Class<? extends Driver>) aClass;
            }
            if (connectionUrl == null) {
                throw new NullPointerException("url is null");
            }
            if (connectionProperties == null) {
                connectionProperties = new Properties();
            }
            if (resultFactory == null) {
                resultFactory = new DatabaseAccessPointImpl.DefaultResultFactory();
            }
            if (connectionFactory == null) {
                connectionFactory = new DatabaseAccessPointImpl.DefaultConnectionFactory();
            }
            return new DatabaseAccessPointImpl(driverClass, connectionUrl, connectionProperties, connectionFactory, resultFactory);
        }
    }

}
