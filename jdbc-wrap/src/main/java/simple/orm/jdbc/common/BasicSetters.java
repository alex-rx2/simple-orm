package simple.orm.jdbc.common;

import io.vavr.Function1;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.common.map.ParameterSetterImpl;
import simple.orm.jdbc.map.ParameterSetter;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.PreparedStatement;

import static simple.orm.jdbc.common.BasicJdbcTypes.*;

/**
 * Default ParameterSetter implementations for {@link BasicJdbcTypes} types.
 */
public final class BasicSetters {
    private BasicSetters() {
    }

    /**
     * Default ParameterSetter implementations for {@link BasicJdbcTypes} types.
     */
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

    /**
     * Map of default setters by type.
     */
    public static final Map<ParameterJdbcType<?>, ParameterSetter<?>> DEFAULT_SETTERS_MAP =
            DEFAULT_SETTERS.toMap(ParameterSetter::getJdbcType, Function1.identity());

}
