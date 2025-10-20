package simple.orm.jdbc.map.in;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;

import static simple.orm.jdbc.param.BasicJdbcTypes.*;

/**
 * Implementation of {@link ParameterSetter}.
 */
public class ParameterSetterImpl<T> implements ParameterSetter<T> {

    public interface Setter<T> {
        void inject(PreparedStatement stmt, int index, T value) throws SQLException;
    }

    public static final Seq<ParameterSetter<?>> DEFAULT_SETTERS = Array.of(
            // bit/boolean
            new ParameterSetterImpl<>(BIT, PreparedStatement::setBoolean),
            new ParameterSetterImpl<>(BOOLEAN, PreparedStatement::setBoolean),
            // numeric
            new ParameterSetterImpl<>(TINYINT, PreparedStatement::setInt),
            new ParameterSetterImpl<>(SMALLINT, PreparedStatement::setInt),
            new ParameterSetterImpl<>(INTEGER, PreparedStatement::setInt),
            new ParameterSetterImpl<>(BIGINT, PreparedStatement::setLong),
            new ParameterSetterImpl<>(FLOAT, PreparedStatement::setFloat),
            new ParameterSetterImpl<>(REAL, PreparedStatement::setFloat),
            new ParameterSetterImpl<>(DOUBLE, PreparedStatement::setDouble),
            new ParameterSetterImpl<>(NUMERIC, PreparedStatement::setBigDecimal),
            new ParameterSetterImpl<>(DECIMAL, PreparedStatement::setBigDecimal),
            // char/varchar
            new ParameterSetterImpl<>(CHAR, PreparedStatement::setString),
            new ParameterSetterImpl<>(VARCHAR, PreparedStatement::setString),
            new ParameterSetterImpl<>(LONGVARCHAR, PreparedStatement::setString),
            new ParameterSetterImpl<>(NCHAR, PreparedStatement::setNString),
            new ParameterSetterImpl<>(NVARCHAR, PreparedStatement::setNString),
            new ParameterSetterImpl<>(LONGNVARCHAR, PreparedStatement::setNString),
            // date/time
            new ParameterSetterImpl<>(DATE, PreparedStatement::setDate),
            new ParameterSetterImpl<>(TIME, PreparedStatement::setTime),
            new ParameterSetterImpl<>(TIMESTAMP, PreparedStatement::setTimestamp)
    );

    public static final Map<ParameterJdbcType<?>, ParameterSetter<?>> DEFAULT_SETTERS_MAP =
            DEFAULT_SETTERS.toMap(ParameterSetter::getJdbcType, Function.identity());

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
