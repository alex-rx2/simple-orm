package simple.orm.repo.impl.proxy;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import simple.orm.jdbc.query.Query;
import simple.orm.repo.ImplementationStyle;
import simple.orm.repo.impl.meta.ObjectMethod;
import simple.orm.util.SafeMutable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * {@link InvocationHandler} for repository implementation based on {@link Proxy}.
 */
public class RepositoryInvocationHandlerLazy implements InvocationHandler {

    private final Class<?> repoInterface;
    private final Map<String, Supplier<Query<?, ?>>> suppliers;
    private final SafeMutable<Map<String, Query<?, ?>>> queries;

    public RepositoryInvocationHandlerLazy(Class<?> repoInterface,
                                           Map<String, Supplier<Query<?, ?>>> suppliers) {
        this.repoInterface = repoInterface;
        this.suppliers = suppliers;
        this.queries = SafeMutable.of(HashMap.empty());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        final String methodName = method.getName();
        // check queries
        final Option<Query<?, ?>> queryOpt = queries.get().get(methodName);
        if (queryOpt.isDefined()) {
            return queryOpt.get();
        }
        // check suppliers
        final Option<Supplier<Query<?, ?>>> supplierOpt = suppliers.get(methodName);
        if (supplierOpt.isDefined()) {
            return queries
                    .apply(cache -> cache.put(methodName, supplierOpt.get().get()))
                    .get(methodName)
                    .get();
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
