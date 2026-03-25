package simple.orm.repo.impl.proxy;

import io.vavr.collection.Map;
import simple.orm.jdbc.query.Query;

import java.lang.reflect.InvocationHandler;
import java.util.function.Supplier;

/**
 * Utility class to build instances of {@link RepositoryInvocationHandler}.
 */
public class ProxyHandlerBuilder {

    public InvocationHandler build(Class<?> repoInterface, Map<String, Query<?, ?>> queries) {
        return new RepositoryInvocationHandler(repoInterface, queries);
    }

    public InvocationHandler buildLazy(Class<?> repoInterface, Map<String, Supplier<Query<?, ?>>> suppliers) {
        return new RepositoryInvocationHandlerLazy(repoInterface, suppliers);
    }

}
