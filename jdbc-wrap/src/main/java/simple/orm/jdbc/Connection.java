package simple.orm.jdbc;

/**
 * Abstraction of database connection, able to execute queries and manage transactions.
 */
public interface Connection extends AutoCloseable {

    /**
     * Get connected database.
     *
     * @return database.
     */
    Database getDatabase();

    /**
     * Execute query.
     *
     * @param query query to execute.
     * @param <T>   type of result.
     * @return execution result.
     */
    <T> T execute(Query query);

    // TODO transaction management

}
