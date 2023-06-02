package simple.orm.jdbc;

import java.sql.Driver;
import java.util.Properties;

/**
 * Simple configuration implementation.
 * <br>
 * See {@link Configuration}.
 */
class ConfigurationImpl implements Configuration {

    private final Class<? extends Driver> driverClass;
    private final String url;
    private final Properties properties;

    public ConfigurationImpl(Class<? extends Driver> driverClass, String url, Properties properties) {
        this.driverClass = driverClass;
        this.url = url;
        this.properties = properties;
    }

    @Override
    public Class<? extends Driver> getDriverClass() {
        return driverClass;
    }

    @Override
    public String getConnectionURL() {
        return url;
    }

    @Override
    public Properties getConnectionProperties() {
        return properties;
    }
}
