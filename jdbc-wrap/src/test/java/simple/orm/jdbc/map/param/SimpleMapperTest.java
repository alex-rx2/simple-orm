package simple.orm.jdbc.map.param;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests of {@link SimpleMapper}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleMapperTest {

    @Test
    public void testSimpleMapper() {
        ParameterType<String, String> ptype = mock(ParameterType.class);
        Function<String, String> toJDBC = mock(Function.class);
        Function<String, String> toJava = mock(Function.class);
        when(toJDBC.apply(anyString())).thenReturn("JDBC");
        when(toJava.apply(anyString())).thenReturn("Java");
        // mapper
        SimpleMapper<String, String> testMapper = new SimpleMapper<>(ptype, toJDBC, toJava);
        // test type
        assertThat(testMapper.getType()).isSameAs(ptype);
        // test toJDBC mapper
        assertThat(testMapper.mapToJDBC("from Java")).isEqualTo("JDBC");
        verify(toJDBC).apply(eq("from Java"));
        verifyNoMoreInteractions(toJDBC);
        verifyNoMoreInteractions(toJava);
        // test toJava mapper
        assertThat(testMapper.mapToJava("from JDBC")).isEqualTo("Java");
        verify(toJava).apply(eq("from JDBC"));
        verifyNoMoreInteractions(toJDBC);
        verifyNoMoreInteractions(toJava);
    }
}
