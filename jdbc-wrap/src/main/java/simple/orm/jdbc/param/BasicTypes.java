package simple.orm.jdbc.param;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.function.Function;

/**
 * Most basic types for usage as in/out parameters of queries.
 */
public final class BasicTypes {
    private BasicTypes() {
    }

    // bit/boolean
    public static final ParameterType<Boolean, Boolean> BIT_BOOL =
            BasicJdbcTypes.BIT.implementedBy(Boolean.class, Function.identity(), Function.identity());
    public static final ParameterType<Boolean, Integer> BIT_INT =
            BasicJdbcTypes.BIT.implementedBy(
                    Integer.class,
                    integer -> integer != 0,
                    bool -> bool ? 1 : 0
            );
    public static final ParameterType<Boolean, Boolean> BOOLEAN =
            BasicJdbcTypes.BOOLEAN.implementedBy(Boolean.class, Function.identity(), Function.identity());

    // numeric
    public static final ParameterType<Integer, Integer> TINYINT =
            BasicJdbcTypes.TINYINT.implementedBy(Integer.class, Function.identity(), Function.identity());
    public static final ParameterType<Integer, Integer> SMALLINT =
            BasicJdbcTypes.SMALLINT.implementedBy(Integer.class, Function.identity(), Function.identity());
    public static final ParameterType<Integer, Integer> INTEGER =
            BasicJdbcTypes.INTEGER.implementedBy(Integer.class, Function.identity(), Function.identity());
    public static final ParameterType<Long, Long> BIGINT =
            BasicJdbcTypes.BIGINT.implementedBy(Long.class, Function.identity(), Function.identity());
    public static final ParameterType<Float, Float> FLOAT =
            BasicJdbcTypes.FLOAT.implementedBy(Float.class, Function.identity(), Function.identity());
    public static final ParameterType<Float, Float> REAL =
            BasicJdbcTypes.REAL.implementedBy(Float.class, Function.identity(), Function.identity());
    public static final ParameterType<Double, Double> DOUBLE =
            BasicJdbcTypes.DOUBLE.implementedBy(Double.class, Function.identity(), Function.identity());
    public static final ParameterType<BigDecimal, BigDecimal> NUMERIC =
            BasicJdbcTypes.NUMERIC.implementedBy(BigDecimal.class, Function.identity(), Function.identity());
    public static final ParameterType<BigDecimal, BigDecimal> DECIMAL =
            BasicJdbcTypes.DECIMAL.implementedBy(BigDecimal.class, Function.identity(), Function.identity());

    // char/varchar
    public static final ParameterType<String, String> CHAR =
            BasicJdbcTypes.CHAR.implementedBy(String.class, Function.identity(), Function.identity());
    public static final ParameterType<String, String> VARCHAR =
            BasicJdbcTypes.VARCHAR.implementedBy(String.class, Function.identity(), Function.identity());
    public static final ParameterType<String, String> LONGVARCHAR =
            BasicJdbcTypes.LONGVARCHAR.implementedBy(String.class, Function.identity(), Function.identity());
    public static final ParameterType<String, String> NCHAR =
            BasicJdbcTypes.NCHAR.implementedBy(String.class, Function.identity(), Function.identity());
    public static final ParameterType<String, String> NVARCHAR =
            BasicJdbcTypes.NVARCHAR.implementedBy(String.class, Function.identity(), Function.identity());
    public static final ParameterType<String, String> LONGNVARCHAR =
            BasicJdbcTypes.LONGNVARCHAR.implementedBy(String.class, Function.identity(), Function.identity());

    // date/time
    public static final ParameterType<Date, Date> DATE_SQL =
            BasicJdbcTypes.DATE.implementedBy(Date.class, Function.identity(), Function.identity());
    public static final ParameterType<Date, String> DATE_STRING =
            BasicJdbcTypes.DATE.implementedBy(
                    String.class,
                    Date::valueOf,
                    Date::toString
            );
    public static final ParameterType<Time, Time> TIME_SQL =
            BasicJdbcTypes.TIME.implementedBy(Time.class, Function.identity(), Function.identity());
    public static final ParameterType<Time, String> TIME_STRING =
            BasicJdbcTypes.TIME.implementedBy(
                    String.class,
                    Time::valueOf,
                    Time::toString
            );
    public static final ParameterType<Timestamp, Timestamp> TIMESTAMP_SQL =
            BasicJdbcTypes.TIMESTAMP.implementedBy(Timestamp.class, Function.identity(), Function.identity());
    public static final ParameterType<Timestamp, String> TIMESTAMP_STRING =
            BasicJdbcTypes.TIMESTAMP.implementedBy(
                    String.class,
                    Timestamp::valueOf,
                    Timestamp::toString
            );

}
