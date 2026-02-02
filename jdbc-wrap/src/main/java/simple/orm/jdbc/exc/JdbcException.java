package simple.orm.jdbc.exc;

import java.sql.SQLException;

/**
 * Runtime wrap of JDBC exceptions.
 */
public class JdbcException extends RuntimeException {
    public JdbcException(SQLException sqlException) {
        this(sqlException.getMessage(), sqlException);
    }

    public JdbcException(String message) {
        super(message);
    }

    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }
}
