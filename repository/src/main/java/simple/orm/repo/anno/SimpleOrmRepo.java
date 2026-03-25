package simple.orm.repo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare interface as a repository.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleOrmRepo {

    /**
     * Repository type.
     *
     * @return repository type.
     */
    RepoType type();

    /**
     * Query timeout in seconds.
     * Will be used for queries that have their <nobr>timeout < 0</nobr>.
     * <br><code>0</code> - no timeout.
     * <br><code>-1</code> - take default timeout from connection.
     *
     * @return query timeout in seconds.
     */
    int timeout() default -1;

}
