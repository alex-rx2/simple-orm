package simple.orm.jdbc.impl.map;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.map.ParameterSetter;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;
import simple.orm.jdbc.query.NamedParametersMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * {@link NamedInjector} implementation.
 * <br><br>
 * <b>Important:</b> it is expected that NamedInjectorImpl is always used with same query (with same NamedParametersMap).
 *
 * @param <T> {@inheritDoc}
 */
public class NamedInjectorImpl<T> implements NamedInjector<T> {

    // types - map of ( parameter name in query -> (type, property name) )
    private final Map<String, Tuple2<ParameterType<?, ?>, String>> types;
    private final Map<ParameterJdbcType<?>, ParameterSetter<?>> setters;
    // internal cache of Class and Method/Field objects to access properties
    protected Class<T> sourceClass;
    protected Map<String, Either<Method, Field>> methodsAndFields;

    public NamedInjectorImpl(Map<ParameterJdbcType<?>, ParameterSetter<?>> setters, Map<String, Tuple2<ParameterType<?, ?>, String>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
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
        if (parametersMap.getParameters().size() != types.size()) {
            throw new IllegalArgumentException("parameters count mismatch");
        }
        Option<Tuple2<Integer, String>> parameterWithoutType = parametersMap.getParameters().find(t2 -> !types.containsKey(t2._2));
        if (parameterWithoutType.isDefined()) {
            throw new IllegalArgumentException("query parameter '" + parameterWithoutType.get()._2 + "' has no type defined in injector");
        }
        Seq<Tuple3<Integer, ParameterType<?, ?>, Object>> values = extractValues(source, parametersMap);
        doInjectParameters(stmt, values);
    }

    private Seq<Tuple3<Integer, ParameterType<?, ?>, Object>> extractValues(T source, NamedParametersMap parametersMap) {
        // parameters - seq of (index, type, property name)
        Seq<Tuple3<Integer, ParameterType<?, ?>, String>> params =
                parametersMap.getParameters()
                        .map(indexParam ->
                                types.get(indexParam._2)
                                        .map(typeProperty ->
                                                Tuple.<Integer, ParameterType<?, ?>, String>of(indexParam._1, typeProperty._1, typeProperty._2))
                                        .get()
                        );
        checkCachedReflections(params, source.getClass());
        return params.map(t3 -> Tuple.of(t3._1, t3._2, extractValue(source, t3._3)));
    }

    @SuppressWarnings("unchecked")
    private void checkCachedReflections(Seq<Tuple3<Integer, ParameterType<?, ?>, String>> params, Class<?> aClass) {
        if (sourceClass != aClass) {
            methodsAndFields = null;
        }
        sourceClass = (Class<T>) aClass;
        if (methodsAndFields == null) {
            methodsAndFields = HashMap.ofEntries(
                    params.map(t3 -> Tuple.of(t3._3, findAccessor(t3._1, t3._3, t3._2.getJavaTypeClass())))
            );
        }
    }

    private Either<Method, Field> findAccessor(int index, String propertyName, Class<?> typeJavaClass) {
        // try to find getter
        String getter;
        if (propertyName.isEmpty()) {
            getter = "get";
        } else {
            getter = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        try {
            Method method = sourceClass.getMethod(getter);
            if (!typeJavaClass.isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("for parameter no" + index + " property getter '" + getter + "'" +
                        " returns incompatible result of type " + method.getReturnType().getName() +
                        " (" + typeJavaClass.getName() + " is expected)");
            }
            method.trySetAccessible();
            return Either.left(method);
        } catch (NoSuchMethodException ignored) {
            ;
        }
        // try to find field
        try {
            Field field = findField(sourceClass, propertyName);
            if (!typeJavaClass.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException("for parameter no" + index + " property field '" + propertyName + "'" +
                        " has incompatible type " + field.getType().getName() +
                        " (" + typeJavaClass.getName() + " is expected)");
            }
            field.trySetAccessible();
            return Either.right(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("no property '" + propertyName + "' getter or field found in class " + sourceClass.getName(), e);
        }
    }

    private Field findField(Class<?> aClass, String field) throws NoSuchFieldException {
        try {
            return aClass.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            Class<?> supClass = aClass.getSuperclass();
            if (supClass != null && supClass != Object.class) {
                return findField(supClass, field);
            } else {
                throw e;
            }
        }
    }

    private Object extractValue(T source, String propertyName) {
        Either<Method, Field> accessor = methodsAndFields.get(propertyName)
                .getOrElseThrow(() -> new IllegalStateException("no Method or Field accessor found in internal cache for '" + propertyName + "'"));
        if (accessor.isLeft()) {
            try {
                return accessor.getLeft().invoke(source);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("property '" + propertyName + "' getter invocation failed", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("property '" + propertyName + "' getter is not accessible", e);
            }
        } else {
            try {
                return accessor.get().get(source);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("property '" + propertyName + "' field is not accessible", e);
            }
        }
    }

    private void doInjectParameters(PreparedStatement stmt, Seq<Tuple3<Integer, ParameterType<?, ?>, Object>> values) {
        values.forEach(t3 -> inject(stmt, t3._1, t3._2, t3._3));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void inject(PreparedStatement stmt, int index, ParameterType type, Object value) throws JdbcException {
        try {
            if (value == null) {
                stmt.setNull(index, type.getJDBCType().getVendorTypeNumber());
                return;
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
