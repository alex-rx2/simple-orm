package simple.orm.jdbc.map.in;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import simple.orm.jdbc.param.ParameterType;
import simple.orm.jdbc.query.NamedParametersMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.util.Objects;

/**
 * NamedInjector implementation.
 */
public class NamedInjectorImpl<T> implements NamedInjector<T> {

    protected Seq<ParameterType<?, ?>> types;
    protected IndexedInjectorImpl indexedInjector;

    public NamedInjectorImpl(ParameterType<?, ?>... types) {
        this(Array.of(types));
    }

    public NamedInjectorImpl(Seq<ParameterType<?, ?>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = types;
        this.indexedInjector = new IndexedInjectorImpl(types);
    }

    @Override
    public void injectParameters(PreparedStatement stmt, T source, NamedParametersMap parametersMap) {
        if (stmt == null) {
            throw new NullPointerException("stmt is null");
        }
        if (source == null) {
            throw new NullPointerException("source is null");
        }
        if (parametersMap == null) {
            throw new NullPointerException("parametersMap is null");
        }
        Seq<Object> values = extractValues(source, parametersMap.getParameters());
        indexedInjector.doInjectParameters(stmt, values);
    }

    private Seq<Object> extractValues(T source, Seq<Tuple2<Integer, String>> params) {
        return params
                // group and map into list of (name, seq of indexes)
                .groupBy(t2 -> t2._2)
                .toList()
                .map(item -> Tuple.of(item._1, item._2.map(t2 -> t2._1)))
                // replace name with value
                .map(item -> Tuple.of(extractValue(source, item._1), item._2))
                // flatten with indexes making a list of (index,value)
                .flatMap(item -> item._2.map(idx -> Tuple.of(idx, item._1)))
                // sort by index
                .sortBy(item -> item._1)
                .map(item -> item._2);
    }

    private Object extractValue(T source, String propertyName) {
        Option<Object> value;
        Class<?> aClass = source.getClass();
        // try to use getter
        String getter;
        if (propertyName.isEmpty()) {
            getter = "get";
        } else {
            getter = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        try {
            Method method = aClass.getMethod(getter);
            if (!method.isAccessible() && !Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            value = Option.of(method.invoke(source));
        } catch (NoSuchMethodException e) {
            value = null;
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("property '" + propertyName + "' getter invocation failed", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("property '" + propertyName + "' getter is not accessible", e);
        }
        // try direct field access if no getter found
        if (value == null) {
            try {
                Field field = aClass.getField(propertyName);
                if (!field.isAccessible() && !Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }
                value = Option.of(field.get(source));
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("no property '" + propertyName + "' getter or field found in class " + aClass);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("property '" + propertyName + "' field is not accessible", e);
            }
        }
        // if we got here - we found it
        return value.getOrNull();
    }

}
