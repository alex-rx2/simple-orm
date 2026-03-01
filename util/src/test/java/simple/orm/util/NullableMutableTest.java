package simple.orm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for NullableMutable class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NullableMutableTest {

    @Test
    public void testMutable() {
        NullableMutable<String> mutable = NullableMutable.of("test string");
        assertThat(mutable.get()).isEqualTo("test string");
        mutable.set("other string");
        assertThat(mutable.get()).isEqualTo("other string");
        mutable.apply(s -> "yet an" + s);
        assertThat(mutable.get()).isEqualTo("yet another string");
    }

    @Test
    public void testNull() {
        assertThat(NullableMutable.of(null).get()).isNull();
        NullableMutable<String> mutable = NullableMutable.of("test string");
        mutable.set(null);
        assertThat(mutable.get()).isNull();
        mutable = NullableMutable.of("test string");
        mutable.apply(s -> null);
        assertThat(mutable.get()).isNull();
    }


}
