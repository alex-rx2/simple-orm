package simple.orm.jdbc.map.in;

import simple.orm.jdbc.exc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for single defined (non-null) setter of PreparedStatement parameter.
 */
public interface ParameterSetter<T> {

    /**
     * Parameter class.
     *
     * @return parameter class.
     */
    Class<T> getJdbcClass();

    /**
     * Setter method.
     *
     * @param stmt  JDBC {@link PreparedStatement}.
     * @param index parameter index (starts with 1 as per JDBC spec).
     * @param value <b>non-null</b> value to be set.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void inject(PreparedStatement stmt, int index, T value);

}
