package simple.orm.h2.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.h2.H2Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * Tests on {@link ByteIntMapper}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ByteIntMapperTest {

    @Test
    public void testThrowOnOutOfRange() {
        final ByteIntMapper mapper = new ByteIntMapper(H2Types.TINYINT, false);
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc(123)).isEqualTo((byte) 123);
            assertThat(mapper.javaToJdbc(-123)).isEqualTo((byte) -123);
            assertThatCode(() -> mapper.javaToJdbc(128)).isInstanceOf(IllegalArgumentException.class);
            assertThatCode(() -> mapper.javaToJdbc(-129)).isInstanceOf(IllegalArgumentException.class);
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava((byte) 123)).isEqualTo(123);
            assertThat(mapper.jdbcToJava((byte) -123)).isEqualTo(-123);
        }
    }

    @Test
    public void testIgnoreOfRange() {
        final ByteIntMapper mapper = new ByteIntMapper(H2Types.TINYINT, true);
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc(123)).isEqualTo((byte) 123);
            assertThat(mapper.javaToJdbc(-123)).isEqualTo((byte) -123);
            assertThat(mapper.javaToJdbc(128)).isEqualTo(Byte.MAX_VALUE);
            assertThat(mapper.javaToJdbc(333)).isEqualTo(Byte.MAX_VALUE);
            assertThat(mapper.javaToJdbc(-129)).isEqualTo(Byte.MIN_VALUE);
            assertThat(mapper.javaToJdbc(-333)).isEqualTo(Byte.MIN_VALUE);
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava((byte) 123)).isEqualTo(123);
            assertThat(mapper.jdbcToJava((byte) -123)).isEqualTo(-123);
        }
    }

}
