package simple.orm.jdbc.map.in;

import simple.orm.jdbc.exc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of {@link ParameterSetter}.
 */
public class ParameterSetterImpl<T> implements ParameterSetter<T> {

    public interface Setter<T> {
        void inject(PreparedStatement stmt, int index, T value) throws SQLException;
    }

    private final Class<T> jdbcClass;
    private final Setter<T> setter;

    public ParameterSetterImpl(Class<T> jdbcClass, Setter<T> setter) {
        if (jdbcClass == null) {
            throw new NullPointerException("jdbcClass is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        this.jdbcClass = jdbcClass;
        this.setter = setter;
    }

    @Override
    public Class<T> getJdbcClass() {
        return jdbcClass;
    }

    @Override
    public void inject(PreparedStatement stmt, int index, T value) {
        try {
            setter.inject(stmt, index, value);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
