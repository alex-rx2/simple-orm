package simple.orm.repo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Query injection parameters.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamInjector {

    /**
     * Array of injection parameters metadata.
     *
     * @return injection parameters.
     */
    InjectParam[] value();

}
