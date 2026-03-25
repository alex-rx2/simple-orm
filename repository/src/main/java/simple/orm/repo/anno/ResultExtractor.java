package simple.orm.repo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Query extraction parameters.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultExtractor {

    /**
     * Array of extraction parameters metadata.
     *
     * @return extraction parameters.
     */
    ExtractParam[] value();

}
