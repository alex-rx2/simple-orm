package simple.orm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Mutable class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MutableTest {

    @Test
    public void testMutable() {
        Mutable<String> mutable = Mutable.of("test string");
        assertThat(mutable.get()).isEqualTo("test string");
        mutable.set("other string");
        assertThat(mutable.get()).isEqualTo("other string");
        mutable.apply(s -> "yet an" + s);
        assertThat(mutable.get()).isEqualTo("yet another string");
    }

    @Test
    public void testNull() {
        assertThatCode(() -> Mutable.of(null))
                .isInstanceOf(NullPointerException.class);
        Mutable<String> mutable = Mutable.of("test string");
        assertThatCode(() -> mutable.set(null))
                .isInstanceOf(NullPointerException.class);
        assertThatCode(() -> mutable.apply(s -> null))
                .isInstanceOf(NullPointerException.class);

    }

}
