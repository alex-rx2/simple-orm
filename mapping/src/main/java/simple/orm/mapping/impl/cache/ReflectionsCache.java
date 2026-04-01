package simple.orm.mapping.impl.cache;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import io.vavr.control.Option;
import simple.orm.mapping.NoAccessorFoundException;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.named.RefName;
import simple.orm.util.Mutable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static simple.orm.util.StringUtils.qnn;

/**
 * Implementation of {@link ReflectionsFinder} with internal caching mechanism.
 */
public class ReflectionsCache implements ReflectionsFinder {

    private final Mutable<Map<RefName, Either<Method, Field>>> getters;
    private final Mutable<Map<RefName, Either<Method, Field>>> setters;
    private final Mutable<Map<Class<?>, Constructor<?>>> constructors;

    public ReflectionsCache() {
        this.getters = Mutable.of(HashMap.empty());
        this.setters = Mutable.of(HashMap.empty());
        this.constructors = Mutable.of(HashMap.empty());
    }

    @Override
    public Either<Method, Field> findGetter(String pName, Class<?> target) {
        if (pName == null) {
            throw new NullPointerException("pName is null");
        }
        if (pName.isEmpty()) {
            throw new NullPointerException("pName is empty");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        // check cache
        final String getterName = generateGetterName(pName);
        final String getterNameBool = generateBoolGetterName(pName);
        final Option<Either<Method, Field>> cached =
                getters.get().get(new RefName(getterName, target))
                        .orElse(() -> getters.get().get(new RefName(getterNameBool, target)))
                        .orElse(() -> getters.get().get(new RefName(pName, target)));
        if (cached.isDefined()) {
            return cached.get();
        }
        // sift through reflections (getter, then record style)
        RefName refName = new RefName(getterName, target);
        Method method = findGetterByName(getterName, target);
        if (method == null) {
            refName = new RefName(getterNameBool, target);
            method = findGetterByName(getterNameBool, target);
            if (method != null
                    && method.getReturnType() != boolean.class
                    && method.getReturnType() != Boolean.class) {
                // what to do, what to do?
                method = null;
            }
            if (method == null) {
                refName = new RefName(pName, target);
                method = findGetterByName(pName, target);
            }
        }
        Field field = null;
        if (method == null) {
            refName = new RefName(pName, target);
            field = findFieldByName(pName, target);
        }
        if (method == null && field == null) {
            throw new NoAccessorFoundException("no method or field found to access value of property " + qnn(pName));
        }
        // update cache
        final Either<Method, Field> accessor = method != null ? Either.left(method) : Either.right(field);
        final RefName refNameFinal = refName;
        getters.apply(map -> map.put(refNameFinal, accessor));
        // return
        return accessor;
    }

    private String generateGetterName(String pName) {
        return "get" + Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
    }

    private String generateBoolGetterName(String pName) {
        return "is" + Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
    }

    private Method findGetterByName(String methodName, Class<?> target) {
        // sift through reflections
        try {
            final Method method = target.getDeclaredMethod(methodName);
            if (method.getReturnType() != void.class) {
                method.trySetAccessible();
                return method;
            }
        } catch (NoSuchMethodException e) {
            // ignore
        }
        Class<?> superclass = target.getSuperclass();
        if (superclass != null) {
            return findGetterByName(methodName, superclass);
        } else {
            return null;
        }
    }

    private Field findFieldByName(String fieldName, Class<?> target) {
        // sift through reflections
        try {
            final Field field = target.getDeclaredField(fieldName);
            field.trySetAccessible();
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superclass = target.getSuperclass();
            if (superclass != null) {
                return findFieldByName(fieldName, superclass);
            } else {
                return null;
            }
        }
    }

    @Override
    public Either<Method, Field> findSetter(String pName, Class<?> target) {
        if (pName == null) {
            throw new NullPointerException("pName is null");
        }
        if (pName.isEmpty()) {
            throw new NullPointerException("pName is empty");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        // check cache
        final String setterName = generateSetterName(pName);
        final Option<Either<Method, Field>> cached =
                setters.get().get(new RefName(setterName, target))
                        .orElse(() -> setters.get().get(new RefName(pName, target)));
        if (cached.isDefined()) {
            return cached.get();
        }
        // sift through reflections (setter, then builder style)
        RefName refName = new RefName(setterName, target);
        Method method = findSetterByName(setterName, target);
        if (method == null) {
            refName = new RefName(pName, target);
            method = findSetterByName(pName, target);
        }
        Field field = null;
        if (method == null) {
            refName = new RefName(pName, target);
            field = findFieldByName(pName, target);
        }
        if (method == null && field == null) {
            throw new NoAccessorFoundException("no method or field found to set value of property " + qnn(pName));
        }
        // update cache
        final Either<Method, Field> accessor = method != null ? Either.left(method) : Either.right(field);
        final RefName refNameFinal = refName;
        setters.apply(map -> map.put(refNameFinal, accessor));
        // return
        return accessor;
    }

    private String generateSetterName(String pName) {
        return "set" + Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
    }

    private Method findSetterByName(String methodName, Class<?> target) {
        // sift through reflections
        final Method[] methods = target.getDeclaredMethods();
        for (Method method : methods) {
            if (!methodName.equals(method.getName())
                    || method.getReturnType() != void.class
                    || method.getParameterCount() != 1) {
                continue;
            }
            method.trySetAccessible();
            return method;
        }
        // check superclass methods
        Class<?> superclass = target.getSuperclass();
        if (superclass != null) {
            return findSetterByName(methodName, superclass);
        } else {
            return null;
        }
    }

    @Override
    public Constructor<?> findDefaultConstructor(Class<?> target) {
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        // check cache
        final Option<Constructor<?>> cached = constructors.get().get(target);
        if (cached.isDefined()) {
            return cached.get();
        }
        // find through reflections
        try {
            Constructor<?> constructor = target.getConstructor();
            constructor.trySetAccessible();
            // cache it
            constructors.apply(cache -> cache.put(target, constructor));
            // and return
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new NoAccessorFoundException("no default constructor found for " + target.getName());
        }
    }

}
