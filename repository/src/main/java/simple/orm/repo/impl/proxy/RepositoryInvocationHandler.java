package simple.orm.repo.impl.proxy;

import io.vavr.collection.Map;
import io.vavr.control.Option;
import simple.orm.jdbc.query.Query;
import simple.orm.repo.ImplementationStyle;
import simple.orm.repo.impl.meta.ObjectMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * {@link InvocationHandler} for repository implementation based on {@link Proxy}.
 */
public class RepositoryInvocationHandler implements InvocationHandler {

    private final Class<?> repoInterface;
    private final Map<String, Query<?, ?>> queries;

    public RepositoryInvocationHandler(Class<?> repoInterface,
                                       Map<String, Query<?, ?>> queries) {
        this.repoInterface = repoInterface;
        this.queries = queries;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // check queries
        final Option<Query<?, ?>> query = queries.get(method.getName());
        if (query.isDefined()) {
            return query.get();
        }
        // check standard object methods
        ObjectMethod objectMethod = ObjectMethod.match(method);
        if (objectMethod != null && objectMethod.shouldImplement) {
            switch (objectMethod) {
                case HASH_CODE -> {
                    return this.hashCode() * 31 + 1;
                }
                case EQUALS -> {
                    return proxy == args[0];
                }
                case TO_STRING -> {
                    return repoInterface.getName() + " repository " + ImplementationStyle.JAVA_PROXY + " implementation";
                }
            }
        }
        // something went wrong
        throw new UnsupportedOperationException(method + " is not supported");
    }

}
