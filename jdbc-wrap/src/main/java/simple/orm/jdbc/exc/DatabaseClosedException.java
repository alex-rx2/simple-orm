package simple.orm.jdbc.exc;

import simple.orm.jdbc.DatabaseAccessPoint;

/**
 * {@link DatabaseAccessPoint} is closed.
 */
public class DatabaseClosedException extends RuntimeException {
    public DatabaseClosedException(String message) {
        super(message);
    }
}
