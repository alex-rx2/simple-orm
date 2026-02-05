package simple.orm.jdbc.common;

import io.vavr.Function1;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.ParameterGetter;
import simple.orm.jdbc.map.ParameterGetterImpl;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.sql.ResultSet;

import static simple.orm.jdbc.common.BasicJdbcTypes.*;
import static simple.orm.jdbc.map.ParameterGetterImpl.wrapCheckWasNull;

/**
 * Default ParameterGetter implementations for {@link BasicJdbcTypes} types.
 */
public final class BasicGetters {
    private BasicGetters() {
    }

    /**
     * Default ParameterGetter implementations for {@link BasicJdbcTypes} types.
     */
    public static final Seq<ParameterGetter<?>> BASIC_GETTERS = Array.of(
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

    /**
     * Map of default getters by type.
     */
    public static final Map<ParameterJdbcType<?>, ParameterGetter<?>> BASIC_GETTERS_MAP =
            BASIC_GETTERS.toMap(ParameterGetter::getJdbcType, Function1.identity());
}
