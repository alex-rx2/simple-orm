package simple.orm.mapping.param;

import simple.orm.jdbc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of {@link ParameterSetter}.
 */
public class ParameterSetterImpl<T> implements ParameterSetter<T> {

    public interface Setter<T> {
        void inject(PreparedStatement stmt, int index, T value) throws SQLException;
    }

    public interface NullSetter {
        void setNull(PreparedStatement stmt, int index) throws SQLException;
    }

    private final Setter<T> setterNoNull;
    private final NullSetter setterNull;

    public ParameterSetterImpl(Setter<T> setter) {
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        this.setterNoNull = setter;
        this.setterNull = (stmt, index) -> setter.inject(stmt, index, null);
    }

    public ParameterSetterImpl(Setter<T> setterNoNull, NullSetter setterNull) {
        if (setterNoNull == null) {
            throw new NullPointerException("setterNoNull is null");
        }
        if (setterNull == null) {
            throw new NullPointerException("setterNull is null");
        }
        this.setterNoNull = setterNoNull;
        this.setterNull = setterNull;
    }

    @Override
    public void setValue(PreparedStatement stmt, int index, T value) {
        try {
            if (value == null) {
                setterNull.setNull(stmt, index);
            } else {
                setterNoNull.inject(stmt, index, value);
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
