package simple.orm.jdbc.map;

import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for getter of a single column from ResultSet.
 */
@Deprecated
public interface ParameterGetter<T> {

    /**
     * Parameter type.
     *
     * @return parameter type.
     */
    ParameterJdbcType<T> getJdbcType();

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
