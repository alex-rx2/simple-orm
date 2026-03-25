package simple.orm.repo.anno;

import simple.orm.jdbc.query.Query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Actual SQL query source for a {@link Query}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuerySource {

    /**
     * Query SQL is provided as is here.
     *
     * @return query SQL.
     */
    String query() default "";

    /**
     * Query SQL should be loaded from the provided URI.
     *
     * @return URI to load query SQL.
     */
    String uri() default "";

    /**
     * Defines charset to be used if query SQL should be loaded from provided URI.
     *
     * @return charset to be used when loading query from URI.
     */
    String uriCharset() default "UTF-8";

}
