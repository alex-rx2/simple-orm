package simple.orm.jdbc;

import java.util.Properties;

/**
 * Base class for simple h2 database tests.
 */
public abstract class BaseH2Test {

    public final Class<org.h2.Driver> h2DriverClass = org.h2.Driver.class;
    public final String h2InMemUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4";
    public final Properties h2ConnectionProperties;

    public BaseH2Test() {
        h2ConnectionProperties = new Properties();
        h2ConnectionProperties.put("sa", "");
    }

}
