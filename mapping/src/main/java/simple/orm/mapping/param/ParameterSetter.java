package simple.orm.mapping.param;

import simple.orm.jdbc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for setter of a single parameter of PreparedStatement.
 * <br>
 * ParameterSetter is expected to properly handle nulls.
 */
public interface ParameterSetter<T> {

    /**
     * Setter method.
     *
     * @param stmt  JDBC {@link PreparedStatement}.
     * @param index parameter index (starts with 1 as per JDBC spec).
     * @param value <b>non-null</b> value to be set.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void setValue(PreparedStatement stmt, int index, T value);

}
