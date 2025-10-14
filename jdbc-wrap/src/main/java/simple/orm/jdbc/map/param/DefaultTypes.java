package simple.orm.jdbc.map.param;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Collection of standard types.
 */
public final class DefaultTypes {
    private DefaultTypes() {
    }

    // Character types
    public static final ParameterType<String, String> CHAR_AS_STRING =
            new ParameterType<>(JDBCType.CHAR, String.class, String.class);
    public static final ParameterType<String, String> VARCHAR_AS_STRING =
            new ParameterType<>(JDBCType.VARCHAR, String.class, String.class);
    public static final ParameterType<String, String> LONGVARCHAR_AS_STRING =
            new ParameterType<>(JDBCType.LONGVARCHAR, String.class, String.class);
    public static final ParameterType<String, String> NVARCHAR_AS_STRING =
            new ParameterType<>(JDBCType.NVARCHAR, String.class, String.class);
    public static final ParameterType<String, String> LONGNVARCHAR_AS_STRING =
            new ParameterType<>(JDBCType.LONGNVARCHAR, String.class, String.class);
    
    // Numeric types
    public static final ParameterType<BigDecimal, BigDecimal> NUMERIC_AS_BIGDECIMAL =
            new ParameterType<>(JDBCType.NUMERIC, BigDecimal.class, BigDecimal.class);
    public static final ParameterType<BigDecimal, BigDecimal> DECIMAL_AS_BIGDECIMAL =
            new ParameterType<>(JDBCType.DECIMAL, BigDecimal.class, BigDecimal.class);
    public static final ParameterType<Boolean, Boolean> BIT_AS_BOOLEAN =
            new ParameterType<>(JDBCType.BIT, Boolean.class, Boolean.class);
    public static final ParameterType<Integer, Integer> TINYINT_AS_INTEGER =
            new ParameterType<>(JDBCType.TINYINT, Integer.class, Integer.class);
    public static final ParameterType<Integer, Integer> SMALLINT_AS_INTEGER =
            new ParameterType<>(JDBCType.SMALLINT, Integer.class, Integer.class);
    public static final ParameterType<Integer, Integer> INTEGER_AS_INTEGER =
            new ParameterType<>(JDBCType.INTEGER, Integer.class, Integer.class);
    public static final ParameterType<Long, Long> BIGINT_AS_LONG =
            new ParameterType<>(JDBCType.BIGINT, Long.class, Long.class);
    public static final ParameterType<Float, Float> REAL_AS_FLOAT =
            new ParameterType<>(JDBCType.REAL, Float.class, Float.class);
    public static final ParameterType<Double, Double> FLOAT_AS_DOUBLE =
            new ParameterType<>(JDBCType.FLOAT, Double.class, Double.class);
    public static final ParameterType<Double, Double> DOUBLE_AS_DOUBLE =
            new ParameterType<>(JDBCType.DOUBLE, Double.class, Double.class);

    // Date/time types
    public static final ParameterType<Date, Date> SQLDATE_AS_SQLDATE =
            new ParameterType<>(JDBCType.DATE, Date.class, Date.class);
    public static final ParameterType<Time, Time> SQLTIME_AS_SQLTIME =
            new ParameterType<>(JDBCType.TIME, Time.class, Time.class);
    public static final ParameterType<Timestamp, Timestamp> SQLTIMESTAMP_AS_SQLTIMESTAMP =
            new ParameterType<>(JDBCType.TIMESTAMP, Timestamp.class, Timestamp.class);

    // Other types
    public static final ParameterType<Boolean, Boolean> BOOLEAN_AS_BOOLEAN =
            new ParameterType<>(JDBCType.BOOLEAN, Boolean.class, Boolean.class);
}
