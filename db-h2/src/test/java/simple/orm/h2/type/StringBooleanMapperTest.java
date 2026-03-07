package simple.orm.h2.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.h2.H2Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * Tests on {@link StringBooleanMapper}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StringBooleanMapperTest {

    @Test
    public void testCaseSensitive() {
        final StringBooleanMapper mapper = new StringBooleanMapper(H2Types.CHARACTER, "TRUE", "FALSE", true);
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc(Boolean.TRUE)).isEqualTo("TRUE");
            assertThat(mapper.javaToJdbc(Boolean.FALSE)).isEqualTo("FALSE");
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava("TRUE")).isEqualTo(Boolean.TRUE);
            assertThat(mapper.jdbcToJava("FALSE")).isEqualTo(Boolean.FALSE);
            assertThatCode(()->mapper.jdbcToJava("true")).isInstanceOf(IllegalArgumentException.class);
            assertThatCode(()->mapper.jdbcToJava("false")).isInstanceOf(IllegalArgumentException.class);
            assertThatCode(()->mapper.jdbcToJava("Yes")).isInstanceOf(IllegalArgumentException.class);
            assertThatCode(()->mapper.jdbcToJava("No")).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void testCaseInsensitive() {
        final StringBooleanMapper mapper = new StringBooleanMapper(H2Types.CHARACTER, "TRUE", "FALSE", false);
        {
            assertThat(mapper.javaToJdbc(null)).isNull();
            assertThat(mapper.javaToJdbc(Boolean.TRUE)).isEqualTo("TRUE");
            assertThat(mapper.javaToJdbc(Boolean.FALSE)).isEqualTo("FALSE");
        }
        {
            assertThat(mapper.jdbcToJava(null)).isNull();
            assertThat(mapper.jdbcToJava("TRUE")).isEqualTo(Boolean.TRUE);
            assertThat(mapper.jdbcToJava("FALSE")).isEqualTo(Boolean.FALSE);
            assertThat(mapper.jdbcToJava("true")).isEqualTo(Boolean.TRUE);
            assertThat(mapper.jdbcToJava("false")).isEqualTo(Boolean.FALSE);
            assertThatCode(()->mapper.jdbcToJava("Yes")).isInstanceOf(IllegalArgumentException.class);
            assertThatCode(()->mapper.jdbcToJava("No")).isInstanceOf(IllegalArgumentException.class);
        }
    }

}
