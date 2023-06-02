package simple.orm.jdbc;

/**
 * Abstraction of configured database access point.
 * <br>
 * Should open and manage connections.
 */
public interface Database extends AutoCloseable {

    /**
     * Get configuration used for this database.
     *
     * @return configuration.
     */
    Configuration getConfiguration();

    /**
     * Obtain connection to database.
     *
     * @return connection.
     */
    Connection connect();

    /**
     * Factory method.
     *
     * @param configuration configuration.
     * @return database.
     */
    static Database of(Configuration configuration) {
        return new DatabaseImpl(configuration);
    }
}
