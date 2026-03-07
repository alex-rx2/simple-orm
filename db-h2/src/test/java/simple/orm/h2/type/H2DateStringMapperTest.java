package simple.orm.h2.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.h2.H2Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests on {@link H2DateStringMapper}.
 * <br>
 * Note: directly taking from {@link H2Mappers}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class H2DateStringMapperTest {

    @Test
    public void testDate() {
        final H2DateStringMapper<LocalDate> mapper = (H2DateStringMapper<LocalDate>) H2Mappers.DATE_STR;
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc("2022-02-24")).isEqualTo(LocalDate.of(2022, 2, 24));
            assertThat(mapper.javaToJdbc("1966-12-13")).isEqualTo(LocalDate.of(1966, 12, 13));
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava(LocalDate.of(2022, 2, 24))).isEqualTo("2022-02-24");
            assertThat(mapper.jdbcToJava(LocalDate.of(1966, 12, 13))).isEqualTo("1966-12-13");
        }
    }

    @Test
    public void testTime() {
        final H2DateStringMapper<LocalTime> mapper = (H2DateStringMapper<LocalTime>) H2Mappers.TIME_STR;
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc("12:30")).isEqualTo(LocalTime.of(12, 30, 0));
            assertThat(mapper.javaToJdbc("02:59:59")).isEqualTo(LocalTime.of(2, 59, 59));
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava(LocalTime.of(12, 30))).isEqualTo("12:30:00");
            assertThat(mapper.jdbcToJava(LocalTime.of(2, 59, 59))).isEqualTo("02:59:59");
        }
    }

    @Test
    public void testTimeWithTimezone() {
        final H2DateStringMapper<OffsetTime> mapper = (H2DateStringMapper<OffsetTime>) H2Mappers.TIME_STR_TZ;
        {
            // UTC+-, GMT+- not supported for time with timezone *facepalm* (though they have code to parse them in TimeZoneProvider)
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc("12:30+10")).isEqualTo(OffsetTime.of(12, 30, 0, 0, ZoneOffset.ofHours(10)));
            assertThat(mapper.javaToJdbc("12:30:33+10:30")).isEqualTo(OffsetTime.of(12, 30, 33, 0, ZoneOffset.ofHoursMinutes(10, 30)));
            assertThat(mapper.javaToJdbc("12:30:33-10:30")).isEqualTo(OffsetTime.of(12, 30, 33, 0, ZoneOffset.ofHoursMinutes(-10, -30)));
            assertThat(mapper.javaToJdbc("02:59:59Z")).isEqualTo(OffsetTime.of(2, 59, 59, 0, ZoneOffset.UTC));
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava(OffsetTime.of(12, 30, 0, 0, ZoneOffset.ofHours(10)))).isEqualTo("12:30:00+10");
            assertThat(mapper.jdbcToJava(OffsetTime.of(12, 30, 0, 0, ZoneOffset.ofHoursMinutes(10, 30)))).isEqualTo("12:30:00+10:30");
            assertThat(mapper.jdbcToJava(OffsetTime.of(12, 30, 0, 0, ZoneOffset.ofHoursMinutes(-10, -30)))).isEqualTo("12:30:00-10:30");
            assertThat(mapper.jdbcToJava(OffsetTime.of(2, 59, 59, 0, ZoneOffset.UTC))).isEqualTo("02:59:59+00");
        }
    }

    @Test
    public void testTimestamp() {
        final H2DateStringMapper<LocalDateTime> mapper = (H2DateStringMapper<LocalDateTime>) H2Mappers.TIMESTAMP_STR;
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc("2022-02-24 12:30")).isEqualTo(LocalDateTime.of(2022, 2, 24, 12, 30, 0, 0));
            assertThat(mapper.javaToJdbc("1966-12-13 00:00:20.000333")).isEqualTo(LocalDateTime.of(1966, 12, 13, 0, 0, 20, 333000));
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava(LocalDateTime.of(2022, 2, 24, 12, 30, 0, 0))).isEqualTo("2022-02-24 12:30:00");
            assertThat(mapper.jdbcToJava(LocalDateTime.of(1966, 12, 13, 0, 0, 20, 333000))).isEqualTo("1966-12-13 00:00:20.000333");
        }
    }

    @Test
    public void testTimestampWithTimezone() {
        final H2DateStringMapper<OffsetDateTime> mapper = (H2DateStringMapper<OffsetDateTime>) H2Mappers.TIMESTAMP_STR_TZ;
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc("2022-02-24 12:30+10"))
                    .isEqualTo(OffsetDateTime.of(2022, 2, 24, 12, 30, 0, 0, ZoneOffset.ofHours(10)));
            assertThat(mapper.javaToJdbc("2022-02-24 12:30:33.333+10:30"))
                    .isEqualTo(OffsetDateTime.of(2022, 2, 24, 12, 30, 33, 333000000, ZoneOffset.ofHoursMinutes(10, 30)));
            assertThat(mapper.javaToJdbc("2022-02-24 12:30:33.333-10:30"))
                    .isEqualTo(OffsetDateTime.of(2022, 2, 24, 12, 30, 33, 333000000, ZoneOffset.ofHoursMinutes(-10, -30)));
            assertThat(mapper.javaToJdbc("1966-12-13 00:00:20.000333Z"))
                    .isEqualTo(OffsetDateTime.of(1966, 12, 13, 0, 0, 20, 333000, ZoneOffset.UTC));
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava(OffsetDateTime.of(2022, 2, 24, 12, 30, 0, 0, ZoneOffset.ofHours(10))))
                    .isEqualTo("2022-02-24 12:30:00+10");
            assertThat(mapper.jdbcToJava(OffsetDateTime.of(2022, 2, 24, 12, 30, 33, 333000000, ZoneOffset.ofHoursMinutes(10, 30))))
                    .isEqualTo("2022-02-24 12:30:33.333+10:30");
            assertThat(mapper.jdbcToJava(OffsetDateTime.of(2022, 2, 24, 12, 30, 33, 333000000, ZoneOffset.ofHoursMinutes(-10, -30))))
                    .isEqualTo("2022-02-24 12:30:33.333-10:30");
            assertThat(mapper.jdbcToJava(OffsetDateTime.of(1966, 12, 13, 0, 0, 20, 333000, ZoneOffset.UTC)))
                    .isEqualTo("1966-12-13 00:00:20.000333+00");
        }
    }

}
