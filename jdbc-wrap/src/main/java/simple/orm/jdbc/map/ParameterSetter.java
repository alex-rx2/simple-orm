package simple.orm.jdbc.map;

import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for setter of a single defined (non-null) parameter of PreparedStatement.
 */
@Deprecated
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
