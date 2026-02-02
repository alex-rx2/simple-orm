package simple.orm.jdbc.map.in;

import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for setter of a single defined (non-null) parameter of PreparedStatement.
 */
public interface ParameterSetter<T> {

    /**
     * Parameter type.
     *
     * @return parameter type.
     */
    ParameterJdbcType<T> getJdbcType();

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
