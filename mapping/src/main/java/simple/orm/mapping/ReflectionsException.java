package simple.orm.mapping;

import java.lang.reflect.InvocationTargetException;

/**
 * Wrap for checked reflections related exception.
 * @see IllegalAccessException
 * @see InvocationTargetException
 * @see InstantiationException
 */
public class ReflectionsException extends RuntimeException {

    public ReflectionsException(String message, Exception cause) {
        super(message, cause);
    }

}
