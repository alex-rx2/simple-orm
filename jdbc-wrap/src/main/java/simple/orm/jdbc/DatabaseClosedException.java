package simple.orm.jdbc;

/**
 * {@link DatabaseAccessPoint} is closed.
 */
public class DatabaseClosedException extends RuntimeException {
    public DatabaseClosedException(String message) {
        super(message);
    }
}
