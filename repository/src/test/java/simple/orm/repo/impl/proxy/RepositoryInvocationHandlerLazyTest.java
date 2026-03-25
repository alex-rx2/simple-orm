package simple.orm.repo.impl.proxy;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.query.Query;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link RepositoryInvocationHandlerLazy}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepositoryInvocationHandlerLazyTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testQueryMethod() throws Exception {
        // prepare
        final Query query1 = mock();
        final Query query2 = mock();
        final Supplier<Query<?, ?>> supplier1 = mock();
        final Supplier<Query<?, ?>> supplier2 = mock();
        when(supplier1.get()).thenReturn(query1);
        when(supplier2.get()).thenReturn(query2);
        final RepositoryInvocationHandlerLazy handler = new RepositoryInvocationHandlerLazy(
                TestInterface.class,
                HashMap.of("methodOne", supplier1, "methodTwo", supplier2)
        );
        // tests
        {
            Object result1 = handler.invoke(this, TestInterface.class.getMethod("methodOne"), new Object[0]);
            Object result2 = handler.invoke(this, TestInterface.class.getMethod("methodTwo"), new Object[0]);
            assertThat(result1).isSameAs(query1);
            assertThat(result2).isSameAs(query2);
        }
        {
            assertThatCode(() -> handler.invoke(this, String.class.getMethod("getBytes"), new Object[0]))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("getBytes()")
                    .hasMessageEndingWith(" is not supported");
        }
        // verify
        verify(supplier1, times(1)).get();
        verify(supplier2, times(1)).get();
        verifyNoMoreInteractions(supplier1, supplier2);
        verifyNoInteractions(query1, query2);
        // some more calls that should go from cache now
        {
            Object result1 = handler.invoke(this, TestInterface.class.getMethod("methodOne"), new Object[0]);
            Object result2 = handler.invoke(this, TestInterface.class.getMethod("methodTwo"), new Object[0]);
            assertThat(result1).isSameAs(query1);
            assertThat(result2).isSameAs(query2);
        }
        // verify
        verifyNoMoreInteractions(supplier1, supplier2);
        verifyNoInteractions(query1, query2);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testObjectMethods() throws Exception {
        // hashCode, equals and toString are passed down to handler
        // prepare
        final Query query1 = mock();
        final Query query2 = mock();
        final Supplier<Query<?, ?>> supplier1 = mock();
        final Supplier<Query<?, ?>> supplier2 = mock();
        when(supplier1.get()).thenReturn(query1);
        when(supplier2.get()).thenReturn(query2);
        final RepositoryInvocationHandlerLazy handler = new RepositoryInvocationHandlerLazy(
                TestInterface.class,
                HashMap.of("methodOne", supplier1, "methodTwo", supplier2)
        );
        // tests
        {
            Object result = handler.invoke(this, Object.class.getMethod("hashCode"), new Object[0]);
            assertThat(result)
                    .isInstanceOf(Integer.class)
                    .isEqualTo(handler.hashCode() * 31 + 1);
        }
        {
            Method equalsMethod = Object.class.getMethod("equals", Object.class);
            Object result1 = handler.invoke(this, equalsMethod, new Object[]{handler});
            Object result2 = handler.invoke(this, equalsMethod, new Object[]{this});
            assertThat(result1)
                    .isInstanceOf(Boolean.class)
                    .isEqualTo(Boolean.FALSE);
            assertThat(result2)
                    .isInstanceOf(Boolean.class)
                    .isEqualTo(Boolean.TRUE);
        }
        {
            Object result = handler.invoke(this, Object.class.getMethod("toString"), new Object[0]);
            assertThat(result)
                    .isInstanceOf(String.class)
                    .isEqualTo(TestInterface.class.getName() + " repository JAVA_PROXY implementation");
        }
        {
            assertThatCode(() -> handler.invoke(this, Object.class.getMethod("wait"), new Object[0]))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("wait()")
                    .hasMessageEndingWith(" is not supported");
            assertThatCode(() -> handler.invoke(this, Object.class.getMethod("notify"), new Object[0]))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("notify()")
                    .hasMessageEndingWith(" is not supported");
            assertThatCode(() -> handler.invoke(this, Object.class.getMethod("notifyAll"), new Object[0]))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("notifyAll()")
                    .hasMessageEndingWith(" is not supported");
            assertThatCode(() -> handler.invoke(this, Object.class.getDeclaredMethod("finalize"), new Object[0]))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("finalize()")
                    .hasMessageEndingWith(" is not supported");
            assertThatCode(() -> handler.invoke(this, Object.class.getDeclaredMethod("clone"), new Object[0]))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("clone()")
                    .hasMessageEndingWith(" is not supported");
        }
        // verify
        verifyNoInteractions(supplier1, supplier2, query1, query2);
    }

    public interface TestInterface {

        Query<?, ?> methodOne();

        Query<?, ?> methodTwo();

    }

}
