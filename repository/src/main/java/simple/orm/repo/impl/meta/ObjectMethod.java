package simple.orm.repo.impl.meta;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Object methods, that {@link Proxy} should or should not implement.
 * <br>
 * Note: first three will be redirected to proxy invocation handler,
 *       also some weirdo might declare them in the repository interface...
 */
public enum ObjectMethod {

    HASH_CODE(true),
    EQUALS(true),
    TO_STRING(true),

    CLONE(false),
    NOTIFY(false),
    NOTIFY_ALL(false),
    WAIT(false),
    FINALIZE(false),
    ;

    public final boolean shouldImplement;

    ObjectMethod(boolean shouldImplement) {
        this.shouldImplement = shouldImplement;
    }

    public static ObjectMethod match(Method method) {
        if (method == null) {
            return null;
        }
        // should implement
        if ("hashCode".equals(method.getName())
                && method.getReturnType() == Integer.TYPE
                && method.getParameterCount() == 0
        ) {
            return HASH_CODE;
        }
        if ("equals".equals(method.getName())
                && method.getReturnType() == Boolean.TYPE
                && method.getParameterCount() == 1
        ) {
            return EQUALS;
        }
        if ("toString".equals(method.getName())
                && method.getReturnType() == String.class
                && method.getParameterCount() == 0
        ) {
            return TO_STRING;
        }
        // should not
        if ("clone".equals(method.getName())
                && method.getParameterCount() == 0
        ) {
            return CLONE;
        }
        if ("notify".equals(method.getName())
                && method.getReturnType() == Void.TYPE
                && method.getParameterCount() == 0
        ) {
            return NOTIFY;
        }
        if ("notifyAll".equals(method.getName())
                && method.getReturnType() == Void.TYPE
                && method.getParameterCount() == 0
        ) {
            return NOTIFY_ALL;
        }
        if ("wait".equals(method.getName())
                && method.getReturnType() == Void.TYPE
                && method.getParameterCount() == 0
        ) {
            return WAIT;
        }
        if ("wait".equals(method.getName())
                && method.getReturnType() == Void.TYPE
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0] == Long.TYPE
        ) {
            return WAIT;
        }
        if ("wait".equals(method.getName())
                && method.getReturnType() == Void.TYPE
                && method.getParameterCount() == 2
                && method.getParameterTypes()[0] == Long.TYPE
                && method.getParameterTypes()[1] == Integer.TYPE
        ) {
            return WAIT;
        }
        if ("finalize".equals(method.getName())
                && method.getReturnType() == Void.TYPE
                && method.getParameterCount() == 0
        ) {
            return FINALIZE;
        }
        return null;
    }

}
