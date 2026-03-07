package simple.orm.h2.type;

import io.vavr.Function1;
import org.h2.util.DateTimeUtils;
import org.h2.util.JSR310Utils;
import org.h2.value.ValueDate;
import org.h2.value.ValueTime;
import org.h2.value.ValueTimeTimeZone;
import org.h2.value.ValueTimestamp;
import org.h2.value.ValueTimestampTimeZone;
import simple.orm.h2.H2Types;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.AbstractTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

/**
 * {@link TypeMapper} for date/time date types which value are represented as string in java application.
 * <br>
 * Note: this mapper uses own H2 database {@link DateTimeUtils} date/time parsing algorithm (and formatting)
 *
 * @param <T> exact date/time class used in JDBC API.
 */
public final class H2DateStringMapper<T> extends AbstractTypeMapper<T, String> {

    private final Function1<String, T> javaToJdbc;
    private final Function1<T, String> jdbcToJava;

    private H2DateStringMapper(ParameterJdbcType<T> jdbcType,
                               Function1<String, T> javaToJdbc,
                               Function1<T, String> jdbcToJava) {
        super(jdbcType, String.class);
        if (javaToJdbc == null) {
            throw new NullPointerException("javaToJdbc is null");
        }
        if (jdbcToJava == null) {
            throw new NullPointerException("jdbcToJava is null");
        }
        this.javaToJdbc = javaToJdbc;
        this.jdbcToJava = jdbcToJava;
    }

    @Override
    public T javaToJdbc(String value) {
        return value == null ? null : javaToJdbc.apply(value);
    }

    @Override
    public String jdbcToJava(T value) {
        return value == null ? null : jdbcToJava.apply(value);
    }

    public static H2DateStringMapper<LocalDate> date() {
        return new H2DateStringMapper<>(
                H2Types.DATE,
                s -> {
                    ValueDate date = ValueDate.fromDateValue(DateTimeUtils.parseDateValue(s, 0, s.length()));
                    return JSR310Utils.valueToLocalDate(date, null);
                },
                localDate -> {
                    ValueDate date = JSR310Utils.localDateToValue(localDate);
                    return date.getString();
                }
        );
    }

    public static H2DateStringMapper<LocalTime> time() {
        return new H2DateStringMapper<>(
                H2Types.TIME,
                s -> {
                    ValueTime time = (ValueTime) DateTimeUtils.parseTime(s, null, false);
                    return JSR310Utils.valueToLocalTime(time, null);
                },
                localTime -> {
                    ValueTime time = JSR310Utils.localTimeToValue(localTime);
                    return time.getString();
                }
        );
    }

    public static H2DateStringMapper<OffsetTime> timeWithTZ() {
        return new H2DateStringMapper<>(
                H2Types.TIME_WITH_TIMEZONE,
                s -> {
                    ValueTimeTimeZone timeTZ = (ValueTimeTimeZone) DateTimeUtils.parseTime(s, null, true);
                    return JSR310Utils.valueToOffsetTime(timeTZ, null);
                },
                offsetTime -> {
                    ValueTimeTimeZone timeTZ = JSR310Utils.offsetTimeToValue(offsetTime);
                    return timeTZ.getString();
                }
        );
    }

    public static H2DateStringMapper<LocalDateTime> timestamp() {
        return new H2DateStringMapper<>(
                H2Types.TIMESTAMP,
                s -> {
                    ValueTimestamp timestamp = (ValueTimestamp) DateTimeUtils.parseTimestamp(s, null, false);
                    return JSR310Utils.valueToLocalDateTime(timestamp, null);
                },
                localDateTime -> {
                    ValueTimestamp timestamp = JSR310Utils.localDateTimeToValue(localDateTime);
                    return timestamp.getString();
                }
        );
    }

    public static H2DateStringMapper<OffsetDateTime> timestampWithTZ() {
        return new H2DateStringMapper<>(
                H2Types.TIMESTAMP_WITH_TIMEZONE,
                s -> {
                    ValueTimestampTimeZone timestampTZ = (ValueTimestampTimeZone) DateTimeUtils.parseTimestamp(s, null, true);
                    return JSR310Utils.valueToOffsetDateTime(timestampTZ, null);
                },
                offsetDateTime -> {
                    ValueTimestampTimeZone timestampTZ = JSR310Utils.offsetDateTimeToValue(offsetDateTime);
                    return timestampTZ.getString();
                }
        );
    }

}
