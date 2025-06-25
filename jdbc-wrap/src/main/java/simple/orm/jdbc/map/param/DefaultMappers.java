package simple.orm.jdbc.map.param;

import io.vavr.collection.Array;
import io.vavr.collection.Traversable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.function.Function;

/**
 * Collection of standard mappers.
 */
public final class DefaultMappers {

    private DefaultMappers() {
    }

    // Character types
    public static final ParameterMapper<String, String> CHAR_AS_STRING =
            new SimpleMapper<>(DefaultTypes.CHAR_AS_STRING, Function.identity(), Function.identity());
    public static final ParameterMapper<String, String> VARCHAR_AS_STRING =
            new SimpleMapper<>(DefaultTypes.VARCHAR_AS_STRING, Function.identity(), Function.identity());
    public static final ParameterMapper<String, String> LONGVARCHAR_AS_STRING =
            new SimpleMapper<>(DefaultTypes.LONGVARCHAR_AS_STRING, Function.identity(), Function.identity());
    public static final ParameterMapper<String, String> NVARCHAR_AS_STRING =
            new SimpleMapper<>(DefaultTypes.NVARCHAR_AS_STRING, Function.identity(), Function.identity());
    public static final ParameterMapper<String, String> LONGNVARCHAR_AS_STRING =
            new SimpleMapper<>(DefaultTypes.LONGNVARCHAR_AS_STRING, Function.identity(), Function.identity());

    // Numeric types
    public static final ParameterMapper<BigDecimal, BigDecimal> NUMERIC_AS_BIGDECIMAL =
            new SimpleMapper<>(DefaultTypes.NUMERIC_AS_BIGDECIMAL, Function.identity(), Function.identity());
    public static final ParameterMapper<BigDecimal, BigDecimal> DECIMAL_AS_BIGDECIMAL =
            new SimpleMapper<>(DefaultTypes.DECIMAL_AS_BIGDECIMAL, Function.identity(), Function.identity());
    public static final ParameterMapper<Boolean, Boolean> BIT_AS_BOOLEAN =
            new SimpleMapper<>(DefaultTypes.BIT_AS_BOOLEAN, Function.identity(), Function.identity());
    public static final ParameterMapper<Integer, Integer> TINYINT_AS_INTEGER =
            new SimpleMapper<>(DefaultTypes.TINYINT_AS_INTEGER, Function.identity(), Function.identity());
    public static final ParameterMapper<Integer, Integer> SMALLINT_AS_INTEGER =
            new SimpleMapper<>(DefaultTypes.SMALLINT_AS_INTEGER, Function.identity(), Function.identity());
    public static final ParameterMapper<Integer, Integer> INTEGER_AS_INTEGER =
            new SimpleMapper<>(DefaultTypes.INTEGER_AS_INTEGER, Function.identity(), Function.identity());
    public static final ParameterMapper<Long, Long> BIGINT_AS_LONG =
            new SimpleMapper<>(DefaultTypes.BIGINT_AS_LONG, Function.identity(), Function.identity());
    public static final ParameterMapper<Float, Float> REAL_AS_FLOAT =
            new SimpleMapper<>(DefaultTypes.REAL_AS_FLOAT, Function.identity(), Function.identity());
    public static final ParameterMapper<Double, Double> FLOAT_AS_DOUBLE =
            new SimpleMapper<>(DefaultTypes.FLOAT_AS_DOUBLE, Function.identity(), Function.identity());
    public static final ParameterMapper<Double, Double> DOUBLE_AS_DOUBLE =
            new SimpleMapper<>(DefaultTypes.DOUBLE_AS_DOUBLE, Function.identity(), Function.identity());

    // Date/time types
    public static final ParameterMapper<Date, Date> DATE_AS_DATE =
            new SimpleMapper<>(DefaultTypes.DATE_AS_DATE, Function.identity(), Function.identity());
    public static final ParameterMapper<Time, Time> TIME_AS_TIME =
            new SimpleMapper<>(DefaultTypes.TIME_AS_TIME, Function.identity(), Function.identity());
    public static final ParameterMapper<Timestamp, Timestamp> TIMESTAMP_AS_TIMESTAMP =
            new SimpleMapper<>(DefaultTypes.TIMESTAMP_AS_TIMESTAMP, Function.identity(), Function.identity());

    // Other types
    public static final ParameterMapper<Boolean, Boolean> BOOLEAN_AS_BOOLEAN =
            new SimpleMapper<>(DefaultTypes.BOOLEAN_AS_BOOLEAN, Function.identity(), Function.identity());

    /**
     * Returns the list of all default mappers.
     *
     * @return list of all default mappers.
     */
    public static Traversable<ParameterMapper<?, ?>> getDefaultMappers() {
        return Array.of(
                CHAR_AS_STRING,
                VARCHAR_AS_STRING,
                LONGVARCHAR_AS_STRING,
                NVARCHAR_AS_STRING,
                LONGNVARCHAR_AS_STRING,
                NUMERIC_AS_BIGDECIMAL,
                DECIMAL_AS_BIGDECIMAL,
                BIT_AS_BOOLEAN,
                TINYINT_AS_INTEGER,
                SMALLINT_AS_INTEGER,
                INTEGER_AS_INTEGER,
                BIGINT_AS_LONG,
                REAL_AS_FLOAT,
                FLOAT_AS_DOUBLE,
                DOUBLE_AS_DOUBLE,
                DATE_AS_DATE,
                TIME_AS_TIME,
                TIMESTAMP_AS_TIMESTAMP,
                BOOLEAN_AS_BOOLEAN
        );
    }
}
