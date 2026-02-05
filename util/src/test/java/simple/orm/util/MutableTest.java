package simple.orm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

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

}
