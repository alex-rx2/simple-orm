package simple.orm.jdbc.impl.map;

import io.vavr.Function1;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.map.ParameterGetter;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.ResultSet;
import java.sql.SQLException;

import static simple.orm.jdbc.param.BasicJdbcTypes.*;

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

    public static final Seq<ParameterGetter<?>> DEFAULT_GETTERS = Array.of(
            // bit/boolean
            new ParameterGetterImpl<>(BIT,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getBoolean, Boolean.FALSE),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getBoolean, Boolean.FALSE)
            ),
            new ParameterGetterImpl<>(BOOLEAN,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getBoolean, Boolean.FALSE),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getBoolean, Boolean.FALSE)
            ),
            // numeric
            new ParameterGetterImpl<>(TINYINT,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getInt, 0)
            ),
            new ParameterGetterImpl<>(SMALLINT,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getInt, 0)
            ),
            new ParameterGetterImpl<>(INTEGER,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getInt, 0)
            ),
            new ParameterGetterImpl<>(BIGINT,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getLong, 0L),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getLong, 0L)
            ),
            new ParameterGetterImpl<>(FLOAT,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getFloat, 0.0f),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getFloat, 0.0f)
            ),
            new ParameterGetterImpl<>(REAL,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getFloat, 0.0f),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getFloat, 0.0f)
            ),
            new ParameterGetterImpl<>(DOUBLE,
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getDouble, 0.0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getDouble, 0.0)
            ),
            new ParameterGetterImpl<>(NUMERIC, ResultSet::getBigDecimal, ResultSet::getBigDecimal),
            new ParameterGetterImpl<>(DECIMAL, ResultSet::getBigDecimal, ResultSet::getBigDecimal),
            // char/varchar
            new ParameterGetterImpl<>(CHAR, ResultSet::getString, ResultSet::getString),
            new ParameterGetterImpl<>(VARCHAR, ResultSet::getString, ResultSet::getString),
            new ParameterGetterImpl<>(LONGVARCHAR, ResultSet::getString, ResultSet::getString),
            new ParameterGetterImpl<>(NCHAR, ResultSet::getNString, ResultSet::getNString),
            new ParameterGetterImpl<>(NVARCHAR, ResultSet::getNString, ResultSet::getNString),
            new ParameterGetterImpl<>(LONGNVARCHAR, ResultSet::getNString, ResultSet::getNString),
            // date/time
            new ParameterGetterImpl<>(DATE, ResultSet::getDate, ResultSet::getDate),
            new ParameterGetterImpl<>(TIME, ResultSet::getTime, ResultSet::getTime),
            new ParameterGetterImpl<>(TIMESTAMP, ResultSet::getTimestamp, ResultSet::getTimestamp)
    );

    public static final Map<ParameterJdbcType<?>, ParameterGetter<?>> DEFAULT_GETTERS_MAP =
            DEFAULT_GETTERS.toMap(ParameterGetter::getJdbcType, Function1.identity());

    private static <T> T wrapCheckWasNull(ResultSet rs, int index, GetterIdx<T> getter, T nullValue) throws SQLException {
        T value = getter.getValue(rs, index);
        if (value == nullValue && rs.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    private static <T> T wrapCheckWasNull(ResultSet rs, String label, GetterLabel<T> getter, T nullValue) throws SQLException {
        T value = getter.getValue(rs, label);
        if (value == nullValue && rs.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    private final ParameterJdbcType<T> jdbcType;
    private final ParameterGetterImpl.GetterIdx<T> getterIdx;
    private final ParameterGetterImpl.GetterLabel<T> getterLabel;

    public ParameterGetterImpl(ParameterJdbcType<T> jdbcType, GetterIdx<T> getterIdx, GetterLabel<T> getterLabel) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (getterIdx == null) {
            throw new NullPointerException("getterIdx is null");
        }
        if (getterLabel == null) {
            throw new NullPointerException("getterLabel is null");
        }
        this.jdbcType = jdbcType;
        this.getterIdx = getterIdx;
        this.getterLabel = getterLabel;
    }

    @Override
    public ParameterJdbcType<T> getJdbcType() {
        return jdbcType;
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
