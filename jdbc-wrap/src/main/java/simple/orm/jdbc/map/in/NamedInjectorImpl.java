package simple.orm.jdbc.map.in;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;
import simple.orm.jdbc.query.NamedParametersMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link NamedInjector} implementation.
 */
public class NamedInjectorImpl<T> implements NamedInjector<T> {

    protected Seq<ParameterType<?, ?>> types;
    protected Map<ParameterJdbcType<?>, ParameterSetter<?>> setters;
    // todo - optimization - internal cache of Method/Field (and verify property is assignable to parameter type)
    // todo - optimization - simplify extractValues (access property for each index, even if several times same property)

    public NamedInjectorImpl(ParameterType<?, ?>... types) {
        this(Array.of(types));
    }

    public NamedInjectorImpl(Map<ParameterJdbcType<?>, ParameterSetter<?>> setters, ParameterType<?, ?>... types) {
        this(setters, Array.of(types));
    }

    public NamedInjectorImpl(Seq<ParameterType<?, ?>> types) {
        this(ParameterSetterImpl.DEFAULT_SETTERS_MAP, types);
    }

    public NamedInjectorImpl(Map<ParameterJdbcType<?>, ParameterSetter<?>> setters, Seq<ParameterType<?, ?>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = types;
        this.setters = setters;
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
        doInjectParameters(stmt, values);
    }

    protected Seq<Object> extractValues(T source, Seq<Tuple2<Integer, String>> params) {
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

    protected Object extractValue(T source, String propertyName) {
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
                throw new IllegalArgumentException("no property '" + propertyName + "' getter or field found in class " + aClass, e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("property '" + propertyName + "' field is not accessible", e);
            }
        }
        // if we got here - we found it
        return value.getOrNull();
    }

    protected void doInjectParameters(PreparedStatement stmt, Seq<Object> params) {
        types.zipWithIndex((pt, idx) -> Tuple.of(idx + 1, pt))
                .zipWith(params, (t2, p) -> Tuple.of(t2._1, t2._2, p))
                .forEach(t3 -> inject(stmt, t3._1, t3._2, t3._3));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void inject(PreparedStatement stmt, int index, ParameterType type, Object value) throws JdbcException {
        try {
            if (value == null) {
                stmt.setNull(index, type.getJDBCType().getVendorTypeNumber());
                return;
            }
            if (!type.getJavaTypeClass().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("for parameter no" + index + " of type " + type + " provided value class is " + value.getClass());
            }
            Object jdbcValue = type.fromJava(value);
            ParameterSetter setter = setters.get(type.getParameterJdbcType()).getOrNull();
            if (setter == null) {
                stmt.setObject(index, jdbcValue, type.getJDBCType().getVendorTypeNumber());
            } else {
                setter.setValue(stmt, index, value);
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
