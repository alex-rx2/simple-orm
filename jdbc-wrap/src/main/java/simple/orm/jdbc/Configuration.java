package simple.orm.jdbc;

import java.sql.Driver;
import java.util.Properties;

/**
 * Configuration for Simple ORM database connections and features.
 */
public interface Configuration {

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
     * Factory method.
     *
     * @param driverClassName fully qualified name of JDBC driver class.
     * @param url             database connection url.
     * @return configuration.
     * @throws ClassNotFoundException driver class not found.
     */
    @SuppressWarnings("unchecked")
    static Configuration of(String driverClassName, String url, Properties properties) throws ClassNotFoundException {
        Class<?> aClass = Class.forName(driverClassName);
        if (!Driver.class.isAssignableFrom(aClass)) {
            throw new IllegalArgumentException("driverClassName class is not a Driver class");
        }
        return of((Class<Driver>) aClass, url, properties);
    }

    /**
     * Factory method.
     *
     * @param driverClass JDBC driver class.
     * @param url         database connection url.
     * @param properties  database connection properties.
     * @return configuration.
     */
    static Configuration of(Class<? extends Driver> driverClass, String url, Properties properties) {
        if (driverClass == null) {
            throw new NullPointerException("driverClass is null");
        }
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        return new ConfigurationImpl(driverClass, url, properties == null ? new Properties() : properties);
    }

    static Configuration of(String driverClassName, String url) throws ClassNotFoundException {
        return of(driverClassName, url, new Properties());
    }

    static Configuration of(Class<? extends Driver> driverClass, String url) {
        return of(driverClass, url, new Properties());
    }
}
