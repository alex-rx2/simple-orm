package simple.orm.jdbc.map;

import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of {@link ParameterSetter}.
 */
public class ParameterSetterImpl<T> implements ParameterSetter<T> {

    public interface Setter<T> {
        void inject(PreparedStatement stmt, int index, T value) throws SQLException;
    }

    private final ParameterJdbcType<T> jdbcType;
    private final Setter<T> setter;

    public ParameterSetterImpl(ParameterJdbcType<T> jdbcType, Setter<T> setter) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        this.jdbcType = jdbcType;
        this.setter = setter;
    }

    @Override
    public ParameterJdbcType<T> getJdbcType() {
        return jdbcType;
    }

    @Override
    public void setValue(PreparedStatement stmt, int index, T value) {
        try {
            setter.inject(stmt, index, value);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
