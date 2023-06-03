package simple.orm.jdbc;

import java.sql.Driver;
import java.util.Properties;

/**
 * Abstraction of configured database access point.
 * <br>
 * Opens and manages connections.
 * <br>
 * TODO transactions management
 */
public interface DatabaseAccessPoint extends AutoCloseable {

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
     */
    Connection connect();

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

        public String getDriverClassName() {
            return driverClassName;
        }

        public Builder driverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
            return this;
        }

        public Class<? extends Driver> getDriverClass() {
            return driverClass;
        }

        public Builder driverClass(Class<? extends Driver> driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        public String getConnectionUrl() {
            return connectionUrl;
        }

        public Builder connectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
            return this;
        }

        public Properties getConnectionProperties() {
            return connectionProperties;
        }

        public Builder connectionProperties(Properties connectionProperties) {
            this.connectionProperties = connectionProperties;
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
            return new DatabaseAccessPointImpl(driverClass, connectionUrl, connectionProperties);
        }
    }
}
