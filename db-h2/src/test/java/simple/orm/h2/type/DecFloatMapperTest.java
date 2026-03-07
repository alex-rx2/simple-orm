package simple.orm.h2.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.h2.param.DecFloat;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of {@link DecFloatMapper}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DecFloatMapperTest {

    private final DecFloatMapper mapper = new DecFloatMapper();

    @Test
    public void testNull() {
        assertThat(mapper.jdbcToJava(null)).isNull();
        assertThat(mapper.javaToJdbc(null)).isNull();
    }

    @Test
    public void testSpecials() {
        assertThat(mapper.javaToJdbc(new DecFloat(DecFloat.Type.INFINITY, null)))
                .isEqualTo("Infinity");
        assertThat(mapper.javaToJdbc(new DecFloat(DecFloat.Type.NINFINITY, null)))
                .isEqualTo("-Infinity");
        assertThat(mapper.javaToJdbc(new DecFloat(DecFloat.Type.NAN, null)))
                .isEqualTo("NaN");

        assertThat(mapper.jdbcToJava("Infinity"))
                .isEqualTo(new DecFloat(DecFloat.Type.INFINITY, null));
        assertThat(mapper.jdbcToJava("-Infinity"))
                .isEqualTo(new DecFloat(DecFloat.Type.NINFINITY, null));
        assertThat(mapper.jdbcToJava("NaN"))
                .isEqualTo(new DecFloat(DecFloat.Type.NAN, null));
    }

    @Test
    public void testFactoryMethods() {
        {
            DecFloat df = DecFloat.infinity();
            assertThat(df.type).isEqualTo(DecFloat.Type.INFINITY);
            assertThat(df.value).isNull();
        }
        {
            DecFloat df = DecFloat.ninfinity();
            assertThat(df.type).isEqualTo(DecFloat.Type.NINFINITY);
            assertThat(df.value).isNull();
        }
        {
            DecFloat df = DecFloat.nan();
            assertThat(df.type).isEqualTo(DecFloat.Type.NAN);
            assertThat(df.value).isNull();
        }
        {
            DecFloat df = DecFloat.of(BigDecimal.valueOf(12.13));
            assertThat(df.type).isEqualTo(DecFloat.Type.BIGDECIMAL);
            assertThat(df.value).isEqualTo(BigDecimal.valueOf(12.13));
        }
    }

    @Test
    public void testToJava_BigDecimal() {
        assertThat(mapper.jdbcToJava("123.321"))
                .isEqualTo(DecFloat.of(BigDecimal.valueOf(123.321)));
        assertThat(mapper.jdbcToJava("-1e3"))
                .isEqualTo(DecFloat.of(BigDecimal.valueOf(-1000.0)));
        assertThat(mapper.jdbcToJava("123456E-2"))
                .isEqualTo(DecFloat.of(BigDecimal.valueOf(1234.56)));
    }

    @Test
    public void testToJdbc_BigDecimal() {
        assertThat(mapper.javaToJdbc(DecFloat.of(BigDecimal.valueOf(123.321))))
                .isEqualTo("123.321");
        assertThat(mapper.javaToJdbc(DecFloat.of(new BigDecimal("-11.11e3"))))
                .isEqualTo("-1.111E+4");
        assertThat(mapper.javaToJdbc(DecFloat.of(new BigDecimal("0.00000000123"))))
                .isEqualTo("1.23E-9");
    }

}
