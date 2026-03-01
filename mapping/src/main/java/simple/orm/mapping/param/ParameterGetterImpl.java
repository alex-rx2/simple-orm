package simple.orm.mapping.param;

import simple.orm.jdbc.JdbcException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link ParameterGetter} implementation.
 */
public class ParameterGetterImpl<T> implements ParameterGetter<T> {

    public interface GetterIdx<T> {
        T getValue(ResultSet rs, int index) throws SQLException;
    }

    public interface GetterLabel<T> {
        T getValue(ResultSet rs, String label) throws SQLException;
    }

    public static <T> T wrapCheckWasNull(ResultSet rs, int index, GetterIdx<T> getter, T nullValue) throws SQLException {
        T value = getter.getValue(rs, index);
        if (value == nullValue && rs.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    public static <T> T wrapCheckWasNull(ResultSet rs, String label, GetterLabel<T> getter, T nullValue) throws SQLException {
        T value = getter.getValue(rs, label);
        if (value == nullValue && rs.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    private final GetterIdx<T> getterIdx;
    private final GetterLabel<T> getterLabel;

    public ParameterGetterImpl(GetterIdx<T> getterIdx, GetterLabel<T> getterLabel) {
        if (getterIdx == null) {
            throw new NullPointerException("getterIdx is null");
        }
        if (getterLabel == null) {
            throw new NullPointerException("getterLabel is null");
        }
        this.getterIdx = getterIdx;
        this.getterLabel = getterLabel;
    }

    @Override
    public T getValue(ResultSet rs, int index) {
        try {
            return getterIdx.getValue(rs, index);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public T getValue(ResultSet rs, String label) {
        try {
            return getterLabel.getValue(rs, label);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
