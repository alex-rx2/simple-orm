package simple.orm.util;

import io.vavr.API;

import java.util.function.Predicate;

import static io.vavr.API.*;

/**
 * Utility reflections methods.
 */
public final class RefUtils {
    private RefUtils() {
    }

    public static boolean isAssignableFrom(Class<?> target, Class<?> value) {
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        if (target == void.class) {
            return false;
        }
        if (value == null) {
            return true;
        }
        if (!target.isPrimitive() && !value.isPrimitive()) {
            return target.isAssignableFrom(value);
        }
        if (target == value) {
            return true;
        }
        return API.Match(target).of(
                Case($(exactlyP(boolean.class, Boolean.class)), t -> exactly(value, boolean.class, Boolean.class)),
                Case($(exactlyP(int.class, Integer.class)), t -> exactly(value, int.class, Integer.class)),
                Case($(exactlyP(long.class, Long.class)), t -> exactly(value, long.class, Long.class)),
                Case($(exactlyP(char.class, Character.class)), t -> exactly(value, char.class, Character.class)),
                Case($(exactlyP(double.class, Double.class)), t -> exactly(value, double.class, Double.class)),
                Case($(exactlyP(byte.class, Byte.class)), t -> exactly(value, byte.class, Byte.class)),
                Case($(exactlyP(float.class, Float.class)), t -> exactly(value, float.class, Float.class)),
                Case($(exactlyP(short.class, Short.class)), v -> exactly(value, short.class, Short.class)),
                Case($(), t -> {
                    throw new IllegalStateException("should not be reachable");
                })
        );
    }

    private static Predicate<Class<?>> exactlyP(Class<?> exactlyThis, Class<?> orThat) {
        return aClass -> aClass == exactlyThis || aClass == orThat;
    }

    private static boolean exactly(Class<?> aClass, Class<?> exactlyThis, Class<?> orThat) {
        return aClass == exactlyThis || aClass == orThat;
    }

}
