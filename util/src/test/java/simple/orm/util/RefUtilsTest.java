package simple.orm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static simple.orm.util.RefUtils.isAssignableFrom;

/**
 * {@link RefUtils} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefUtilsTest {

    @Test
    public void testPrimitiveTypesNotAssignableInJava() {
        // that was a shock for me LOL
        assertThat(int.class.isAssignableFrom(Integer.class)).isFalse(); // laughable that IntelliJ IDEA thinks otherwise
        assertThat(Integer.TYPE.isAssignableFrom(Integer.class)).isFalse();
        assertThat(Integer.class.isAssignableFrom(int.class)).isFalse();
        assertThat(Integer.class.isAssignableFrom(Integer.TYPE)).isFalse();
    }

    @Test
    public void testIsAssignableFrom_true() {
        // set 1
        assertThat(isAssignableFrom(byte.class, byte.class)).isTrue();
        assertThat(isAssignableFrom(short.class, short.class)).isTrue();
        assertThat(isAssignableFrom(char.class, char.class)).isTrue();
        assertThat(isAssignableFrom(int.class, int.class)).isTrue();
        assertThat(isAssignableFrom(long.class, long.class)).isTrue();
        assertThat(isAssignableFrom(float.class, float.class)).isTrue();
        assertThat(isAssignableFrom(double.class, double.class)).isTrue();
        assertThat(isAssignableFrom(boolean.class, boolean.class)).isTrue();
        // set 2
        assertThat(isAssignableFrom(Byte.class, byte.class)).isTrue();
        assertThat(isAssignableFrom(Short.class, short.class)).isTrue();
        assertThat(isAssignableFrom(Character.class, char.class)).isTrue();
        assertThat(isAssignableFrom(Integer.class, int.class)).isTrue();
        assertThat(isAssignableFrom(Long.class, long.class)).isTrue();
        assertThat(isAssignableFrom(Float.class, float.class)).isTrue();
        assertThat(isAssignableFrom(Double.class, double.class)).isTrue();
        assertThat(isAssignableFrom(Boolean.class, boolean.class)).isTrue();
        // set 3
        assertThat(isAssignableFrom(byte.class, Byte.class)).isTrue();
        assertThat(isAssignableFrom(short.class, Short.class)).isTrue();
        assertThat(isAssignableFrom(char.class, Character.class)).isTrue();
        assertThat(isAssignableFrom(int.class, Integer.class)).isTrue();
        assertThat(isAssignableFrom(long.class, Long.class)).isTrue();
        assertThat(isAssignableFrom(float.class, Float.class)).isTrue();
        assertThat(isAssignableFrom(double.class, Double.class)).isTrue();
        assertThat(isAssignableFrom(boolean.class, Boolean.class)).isTrue();
        // set 4
        assertThat(isAssignableFrom(Byte.class, Byte.class)).isTrue();
        assertThat(isAssignableFrom(Short.class, Short.class)).isTrue();
        assertThat(isAssignableFrom(Character.class, Character.class)).isTrue();
        assertThat(isAssignableFrom(Integer.class, Integer.class)).isTrue();
        assertThat(isAssignableFrom(Long.class, Long.class)).isTrue();
        assertThat(isAssignableFrom(Float.class, Float.class)).isTrue();
        assertThat(isAssignableFrom(Double.class, Double.class)).isTrue();
        assertThat(isAssignableFrom(Boolean.class, Boolean.class)).isTrue();
        // set 5
        assertThat(isAssignableFrom(String.class, String.class)).isTrue();
        assertThat(isAssignableFrom(Number.class, BigDecimal.class)).isTrue();
    }

    @Test
    public void testIsAssignableFrom_false() {
        // set 1
        assertThat(isAssignableFrom(byte.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(short.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(char.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(int.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(long.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(float.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(double.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(boolean.class, Object.class)).isFalse();
        // set 2
        assertThat(isAssignableFrom(Byte.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Short.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Character.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Integer.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Long.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Float.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Double.class, Object.class)).isFalse();
        assertThat(isAssignableFrom(Boolean.class, Object.class)).isFalse();
        // set 3
        assertThat(isAssignableFrom(BigDecimal.class, Number.class)).isFalse();
    }

}
