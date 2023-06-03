package simple.orm.jdbc;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Couple simple tests on {@link DatabaseAccessPoint.Builder}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseAccessPointBuilderTest {

    @Test
    public void testBuilder() {
        {
            DatabaseAccessPoint dap = DatabaseAccessPoint.builder()
                    .driverClassName("simple.orm.jdbc.FakeDriver")
                    .connectionUrl("jdbc://dbhost:0000")
                    .build();
            assertThat(dap.getDriverClass()).isSameAs(FakeDriver.class);
            assertThat(dap.getConnectionURL()).isEqualTo("jdbc://dbhost:0000");
            assertThat(dap.getConnectionProperties()).isEmpty();
        }
        {
            Properties props = new Properties();
            props.put("a", "b");
            DatabaseAccessPoint dap = DatabaseAccessPoint.builder()
                    .driverClass(FakeDriver.class)
                    .connectionUrl("jdbc://dbhost:1111")
                    .connectionProperties(props)
                    .build();
            assertThat(dap.getDriverClass()).isSameAs(FakeDriver.class);
            assertThat(dap.getConnectionURL()).isEqualTo("jdbc://dbhost:1111");
            assertThat(dap.getConnectionProperties())
                    .containsExactlyInAnyOrderEntriesOf(
                            HashMap.of("a", "b").toJavaMap()
                    );
        }
    }

    @Test
    public void testBuilderExceptions() {
        // empty builder
        assertThatCode(() -> DatabaseAccessPoint.builder().build())
                .isInstanceOf(NullPointerException.class);
        // no driver class or driver class name
        assertThatCode(() ->
                DatabaseAccessPoint.builder()
                        .connectionUrl("jdbc://dbhost:1111")
                        .connectionProperties(new Properties())
                        .build()
        ).isInstanceOf(NullPointerException.class);
        // no url, with driver class
        assertThatCode(() ->
                DatabaseAccessPoint.builder()
                        .driverClass(FakeDriver.class)
                        .connectionProperties(new Properties())
                        .build()
        ).isInstanceOf(NullPointerException.class);
        // no url, with driver class name
        assertThatCode(() ->
                DatabaseAccessPoint.builder()
                        .driverClassName("simple.orm.jdbc.FakeDriver")
                        .connectionProperties(new Properties())
                        .build()
        ).isInstanceOf(NullPointerException.class);
        // wrong driver class name - can't find class
        assertThatCode(() ->
                DatabaseAccessPoint.builder()
                        .driverClassName("FakeDriver")
                        .connectionUrl("jdbc://dbhost:1111")
                        .connectionProperties(new Properties())
                        .build()
        ).isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(ClassNotFoundException.class);
        // wrong driver class name - not a Driver
        assertThatCode(() ->
                DatabaseAccessPoint.builder()
                        .driverClassName("java.lang.String")
                        .connectionUrl("jdbc://dbhost:1111")
                        .connectionProperties(new Properties())
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

}
