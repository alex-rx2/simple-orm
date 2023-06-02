package simple.orm.jdbc;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


class ConfigurationTest {

    @Test
    void testFactoryMethodsWithDriverClassName() throws Exception {
        Properties props = new Properties();
        props.put("a", "b");
        {
            Configuration configuration = Configuration.of("simple.orm.jdbc.FakeDriver", "jdbc://someURL:000", props);
            assertThat(configuration).isInstanceOf(ConfigurationImpl.class);
            assertThat(configuration.getConnectionURL()).isEqualTo("jdbc://someURL:000");
            assertThat(configuration.getDriverClass()).isSameAs(FakeDriver.class);
            assertThat(configuration.getConnectionProperties()).containsExactlyInAnyOrderEntriesOf(HashMap.of("a", "b").toJavaMap());
        }
        {
            assertThatCode(() -> Configuration.of("simple.orm.jdbc.FakeDriver2", "jdbc://someURL:000", props))
                    .isExactlyInstanceOf(ClassNotFoundException.class);
            assertThatCode(() -> Configuration.of("java.lang.String", "jdbc://someURL:000", props))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
            assertThatCode(() -> Configuration.of("simple.orm.jdbc.FakeDriver", "jdbc://someURL:000", null))
                    .doesNotThrowAnyException();
            assertThatCode(() -> Configuration.of("simple.orm.jdbc.FakeDriver", null, props))
                    .isExactlyInstanceOf(NullPointerException.class);
            assertThatCode(() -> Configuration.of((String) null, "jdbc://someURL:000", props))
                    .isExactlyInstanceOf(NullPointerException.class);
        }
        {
            Configuration configuration = Configuration.of("simple.orm.jdbc.FakeDriver", "jdbc://someURL:000", null);
            assertThat(configuration).isInstanceOf(ConfigurationImpl.class);
            assertThat(configuration.getConnectionURL()).isEqualTo("jdbc://someURL:000");
            assertThat(configuration.getDriverClass()).isSameAs(FakeDriver.class);
            assertThat(configuration.getConnectionProperties()).isEmpty();
        }
    }

    @Test
    void testFactoryMethodsWithDriverClass() throws Exception {
        Properties props = new Properties();
        props.put("a", "b");
        {
            Configuration configuration = Configuration.of(FakeDriver.class, "jdbc://someURL:000", props);
            assertThat(configuration).isInstanceOf(ConfigurationImpl.class);
            assertThat(configuration.getConnectionURL()).isEqualTo("jdbc://someURL:000");
            assertThat(configuration.getDriverClass()).isSameAs(FakeDriver.class);
            assertThat(configuration.getConnectionProperties()).containsExactlyInAnyOrderEntriesOf(HashMap.of("a", "b").toJavaMap());
        }
        {
            assertThatCode(() -> Configuration.of(FakeDriver.class, "jdbc://someURL:000", null))
                    .doesNotThrowAnyException();
            assertThatCode(() -> Configuration.of(FakeDriver.class, null, props))
                    .isExactlyInstanceOf(NullPointerException.class);
            assertThatCode(() -> Configuration.of((Class<? extends FakeDriver>) null, "jdbc://someURL:000", props))
                    .isExactlyInstanceOf(NullPointerException.class);
        }
        {
            Configuration configuration = Configuration.of(FakeDriver.class, "jdbc://someURL:000", null);
            assertThat(configuration).isInstanceOf(ConfigurationImpl.class);
            assertThat(configuration.getConnectionURL()).isEqualTo("jdbc://someURL:000");
            assertThat(configuration.getDriverClass()).isSameAs(FakeDriver.class);
            assertThat(configuration.getConnectionProperties()).isEmpty();
        }
    }

}