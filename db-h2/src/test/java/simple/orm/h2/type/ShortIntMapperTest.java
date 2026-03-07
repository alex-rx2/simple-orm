package simple.orm.h2.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.h2.H2Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * Tests on {@link ShortIntMapper}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShortIntMapperTest {

    @Test
    public void testThrowOnOutOfRange() {
        final ShortIntMapper mapper = new ShortIntMapper(H2Types.SMALLINT, false);
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc(123)).isEqualTo((short) 123);
            assertThat(mapper.javaToJdbc(-123)).isEqualTo((short) -123);
            assertThatCode(() -> mapper.javaToJdbc(32768)).isInstanceOf(IllegalArgumentException.class);
            assertThatCode(() -> mapper.javaToJdbc(-32769)).isInstanceOf(IllegalArgumentException.class);
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava((short) 123)).isEqualTo(123);
            assertThat(mapper.jdbcToJava((short) -123)).isEqualTo(-123);
        }
    }

    @Test
    public void testIgnoreOfRange() {
        final ShortIntMapper mapper = new ShortIntMapper(H2Types.SMALLINT, true);
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc(123)).isEqualTo((short) 123);
            assertThat(mapper.javaToJdbc(-123)).isEqualTo((short) -123);
            assertThat(mapper.javaToJdbc(32768)).isEqualTo(Short.MAX_VALUE);
            assertThat(mapper.javaToJdbc(999999)).isEqualTo(Short.MAX_VALUE);
            assertThat(mapper.javaToJdbc(-32769)).isEqualTo(Short.MIN_VALUE);
            assertThat(mapper.javaToJdbc(-999999)).isEqualTo(Short.MIN_VALUE);
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava((short) 123)).isEqualTo(123);
            assertThat(mapper.jdbcToJava((short) -123)).isEqualTo(-123);
        }
    }

}
