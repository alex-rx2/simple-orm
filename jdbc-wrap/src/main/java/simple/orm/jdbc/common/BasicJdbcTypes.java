package simple.orm.jdbc.common;

import simple.orm.jdbc.param.ParameterJdbcType;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Most basic (non-RDBMS specific) JDBC types. And implementing classes fitting most RDBMS cases.
 * <br>
 * It is recommended to have separate types collection for each RDBMS.
 */
public final class BasicJdbcTypes {
    private BasicJdbcTypes() {
    }

    // bit/boolean
    public static final ParameterJdbcType<Boolean> BIT = ParameterJdbcType.of(JDBCType.BIT, Boolean.class);
    public static final ParameterJdbcType<Boolean> BOOLEAN = ParameterJdbcType.of(JDBCType.BOOLEAN, Boolean.class);
    // numeric
    public static final ParameterJdbcType<Integer> TINYINT = ParameterJdbcType.of(JDBCType.TINYINT, Integer.class);
    public static final ParameterJdbcType<Integer> SMALLINT = ParameterJdbcType.of(JDBCType.SMALLINT, Integer.class);
    public static final ParameterJdbcType<Integer> INTEGER = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class);
    public static final ParameterJdbcType<Long> BIGINT = ParameterJdbcType.of(JDBCType.BIGINT, Long.class);
    public static final ParameterJdbcType<Float> FLOAT = ParameterJdbcType.of(JDBCType.FLOAT, Float.class);
    public static final ParameterJdbcType<Float> REAL = ParameterJdbcType.of(JDBCType.REAL, Float.class);
    public static final ParameterJdbcType<Double> DOUBLE = ParameterJdbcType.of(JDBCType.DOUBLE, Double.class);
    public static final ParameterJdbcType<BigDecimal> NUMERIC = ParameterJdbcType.of(JDBCType.NUMERIC, BigDecimal.class);
    public static final ParameterJdbcType<BigDecimal> DECIMAL = ParameterJdbcType.of(JDBCType.DECIMAL, BigDecimal.class);
    // char/varchar
    public static final ParameterJdbcType<String> CHAR = ParameterJdbcType.of(JDBCType.CHAR, String.class);
    public static final ParameterJdbcType<String> VARCHAR = ParameterJdbcType.of(JDBCType.VARCHAR, String.class);
    public static final ParameterJdbcType<String> LONGVARCHAR = ParameterJdbcType.of(JDBCType.LONGVARCHAR, String.class);
    public static final ParameterJdbcType<String> NCHAR = ParameterJdbcType.of(JDBCType.NCHAR, String.class);
    public static final ParameterJdbcType<String> NVARCHAR = ParameterJdbcType.of(JDBCType.NVARCHAR, String.class);
    public static final ParameterJdbcType<String> LONGNVARCHAR = ParameterJdbcType.of(JDBCType.LONGNVARCHAR, String.class);
    // date/time
    public static final ParameterJdbcType<Date> DATE = ParameterJdbcType.of(JDBCType.DATE, Date.class);
    public static final ParameterJdbcType<Time> TIME = ParameterJdbcType.of(JDBCType.TIME, Time.class);
    public static final ParameterJdbcType<Timestamp> TIMESTAMP = ParameterJdbcType.of(JDBCType.TIMESTAMP, Timestamp.class);

}
