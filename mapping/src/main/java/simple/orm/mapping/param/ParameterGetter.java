package simple.orm.mapping.param;

import simple.orm.jdbc.JdbcException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for getter of a single column from ResultSet.
 * <br>
 * ParameterGetter is expected to properly handle NULLs.
 */
public interface ParameterGetter<T> {

    /**
     * Getter method.
     *
     * @param rs    JDBC {@link ResultSet}.
     * @param index parameter index (starts with 1 as per JDBC spec).
     * @return value at specified index.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    T getValue(ResultSet rs, int index);

    /**
     * Getter method.
     *
     * @param rs    JDBC {@link ResultSet}.
     * @param label column label (as per JDBC ResultSet corresponding methods).
     * @return value at specified index.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    T getValue(ResultSet rs, String label);

}
