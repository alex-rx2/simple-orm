package simple.orm.repo;

import simple.orm.jdbc.query.Query;

import java.lang.reflect.Proxy;

/**
 * The style of repository implementation the {@link RepositoryBuilder} will generate.
 */
public enum ImplementationStyle {

    /**
     * Repository is build using {@link Proxy}.
     * <br>
     * {@link Query} instances are created all at once during repository creation.
     * <br>
     * This style is considered as default for implementations.
     */
    JAVA_PROXY,

    /**
     * Repository is build using {@link Proxy}.
     * <br>
     * {@link Query} instances are created on first call of corresponding method.
     */
    JAVA_PROXY_LAZY,

}
