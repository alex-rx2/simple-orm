package simple.orm.h2.type;

import io.vavr.Function2;
import simple.orm.h2.H2Types;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.AbstractTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * {@link TypeMapper} for date/time date types which value are represented as string in java application.
 *
 * @param <T> exact date/time class used in JDBC API.
 */
public class DateStringMapper<T> extends AbstractTypeMapper<T, String> {

    private final DateTimeFormatter formatter;
    private final Function2<String, DateTimeFormatter, T> javaToJdbc;
    private final Function2<T, DateTimeFormatter, String> jdbcToJava;

    protected DateStringMapper(ParameterJdbcType<T> jdbcType,
                               DateTimeFormatter formatter,
                               Function2<String, DateTimeFormatter, T> javaToJdbc,
                               Function2<T, DateTimeFormatter, String> jdbcToJava) {
        super(jdbcType, String.class);
        if (formatter == null) {
            throw new NullPointerException("formatter is null");
        }
        if (javaToJdbc == null) {
            throw new NullPointerException("javaToJdbc is null");
        }
        if (jdbcToJava == null) {
            throw new NullPointerException("jdbcToJava is null");
        }
        this.formatter = formatter;
        this.javaToJdbc = javaToJdbc;
        this.jdbcToJava = jdbcToJava;
    }

    @Override
    public T javaToJdbc(String value) {
        return value == null ? null : javaToJdbc.apply(value, formatter);
    }

    @Override
    public String jdbcToJava(T value) {
        return value == null ? null : jdbcToJava.apply(value, formatter);
    }

    public static DateStringMapper<LocalDate> date(String pattern) {
        return date(DateTimeFormatter.ofPattern(pattern));
    }

    public static DateStringMapper<LocalDate> date(String pattern, Locale locale) {
        return date(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static DateStringMapper<LocalDate> date(DateTimeFormatter formatter) {
        return new DateStringMapper<>(
                H2Types.DATE,
                formatter,
                LocalDate::parse,
                LocalDate::format
        );
    }

    public static DateStringMapper<LocalTime> time(String pattern) {
        return time(DateTimeFormatter.ofPattern(pattern));
    }

    public static DateStringMapper<LocalTime> time(String pattern, Locale locale) {
        return time(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static DateStringMapper<LocalTime> time(DateTimeFormatter formatter) {
        return new DateStringMapper<>(
                H2Types.TIME,
                formatter,
                LocalTime::parse,
                LocalTime::format
        );
    }

    public static DateStringMapper<OffsetTime> timeWithTZ(String pattern) {
        return timeWithTZ(DateTimeFormatter.ofPattern(pattern));
    }

    public static DateStringMapper<OffsetTime> timeWithTZ(String pattern, Locale locale) {
        return timeWithTZ(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static DateStringMapper<OffsetTime> timeWithTZ(DateTimeFormatter formatter) {
        return new DateStringMapper<>(
                H2Types.TIME_WITH_TIMEZONE,
                formatter,
                OffsetTime::parse,
                OffsetTime::format
        );
    }

    public static DateStringMapper<LocalDateTime> timestamp(String pattern) {
        return timestamp(DateTimeFormatter.ofPattern(pattern));
    }

    public static DateStringMapper<LocalDateTime> timestamp(String pattern, Locale locale) {
        return timestamp(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static DateStringMapper<LocalDateTime> timestamp(DateTimeFormatter formatter) {
        return new DateStringMapper<>(
                H2Types.TIMESTAMP,
                formatter,
                LocalDateTime::parse,
                LocalDateTime::format
        );
    }

    public static DateStringMapper<OffsetDateTime> timestampWithTZ(String pattern) {
        return timestampWithTZ(DateTimeFormatter.ofPattern(pattern));
    }

    public static DateStringMapper<OffsetDateTime> timestampWithTZ(String pattern, Locale locale) {
        return timestampWithTZ(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static DateStringMapper<OffsetDateTime> timestampWithTZ(DateTimeFormatter formatter) {
        return new DateStringMapper<>(
                H2Types.TIMESTAMP_WITH_TIMEZONE,
                formatter,
                OffsetDateTime::parse,
                OffsetDateTime::format
        );
    }

}
