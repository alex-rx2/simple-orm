package simple.orm.jdbc.map.out;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link NamedExtractor} implementation.
 * <br><br>
 * This extractor supports two modes of extraction:
 * <ul>
 *     <li> by index of column in ResultSet,
 *     <li> by label of column in ResultSet (as per JDBC spec).
 * </ul>
 * In accordance to these modes of extraction either mapping of column indexes to object property names
 * or mapping of column labels to object property names must be provided (even if latter is obvious).
 * <br>
 * Result object is created with no-arguments constructor, properties are injected via setters or directly into fields.
 *
 * @param <T> {@inheritDoc}
 */
public class NamedExtractorImpl<T> implements NamedExtractor<T> {

    protected Class<T> resultClass;
    // types - seq of (index or label in ResultSet),(type),(property name)
    protected Seq<Tuple3<Either<Integer, String>, ParameterType<?, ?>, String>> types;
    protected Map<ParameterJdbcType<?>, ParameterGetter<?>> getters;
    // internal cache of constructor and Method/Field accessors
    protected Constructor<T> constructor;
    protected Map<String, Either<Method, Field>> methodsAndFields;

    public NamedExtractorImpl(Class<T> resultClass,
                              Map<ParameterJdbcType<?>, ParameterGetter<?>> getters,
                              Seq<Tuple2<ParameterType<?, ?>, String>> typesAndNamesByIndex) {
        if (resultClass == null) {
            throw new NullPointerException("resultClass is null");
        }
        if (typesAndNamesByIndex == null) {
            throw new NullPointerException("typesAndNamesByIndex is null");
        }
        if (typesAndNamesByIndex.find(t2 -> t2 == null || t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("typesAndNamesByIndex contains nulls");
        }
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        this.resultClass = resultClass;
        this.types = typesAndNamesByIndex.zipWithIndex(
                (t2, idx) -> Tuple.of(Either.left(idx + 1), t2._1, t2._2)
        );
        this.getters = getters;
    }

    public NamedExtractorImpl(Class<T> resultClass,
                              Map<ParameterJdbcType<?>, ParameterGetter<?>> getters,
                              Map<String, ParameterType<?, ?>> typesByLabel,
                              Map<String, String> labelsToNames) {
        if (resultClass == null) {
            throw new NullPointerException("resultClass is null");
        }
        if (typesByLabel == null) {
            throw new NullPointerException("typesByLabel is null");
        }
        if (labelsToNames == null) {
            throw new NullPointerException("labelsToNames is null");
        }
        if (typesByLabel.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("typesByLabel contains nulls");
        }
        if (labelsToNames.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("labelsToNames contains nulls");
        }
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        this.resultClass = resultClass;
        this.types = typesByLabel.toList().map(
                t2 -> Tuple.of(Either.right(t2._1), t2._2, labelsToNames.getOrElse(t2._1, t2._1))
        );
        this.getters = getters;
    }

    @Override
    public T extractRow(ResultSet rs) {
        if (rs == null) {
            throw new NullPointerException("rs is null");
        }
        Seq<Tuple3<Object, String, ParameterType<?, ?>>> values = doExtractRow(rs);
        return constructResultObject(values);
    }

    protected Seq<Tuple3<Object, String, ParameterType<?, ?>>> doExtractRow(ResultSet rs) {
        return types.map(t3 -> Tuple.of(extract(rs, t3._1, t3._2), t3._3, t3._2));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object extract(ResultSet rs, Either<Integer, String> indexOrLabel, ParameterType type) {
        try {
            ParameterGetter<?> getter = getters.get(type.getParameterJdbcType()).getOrNull();
            Object jdbcValue;
            if (getter == null) {
                if (indexOrLabel.isLeft()) {
                    jdbcValue = rs.getObject(indexOrLabel.getLeft(), type.getJDBCTypeClass());
                } else {
                    jdbcValue = rs.getObject(indexOrLabel.get(), type.getJDBCTypeClass());
                }
            } else {
                if (indexOrLabel.isLeft()) {
                    jdbcValue = getter.getValue(rs, indexOrLabel.getLeft());
                } else {
                    jdbcValue = getter.getValue(rs, indexOrLabel.get());
                }
            }
            return type.fromJDBC(jdbcValue);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    private T constructResultObject(Seq<Tuple3<Object, String, ParameterType<?, ?>>> values) {
        checkCachedReflections(values);
        // create object
        T result;
        try {
            Constructor<T> constructor = resultClass.getConstructor();
            if (!constructor.isAccessible() && !Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }
            result = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("default constructor not found in " + resultClass.getName(), e);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException("failed to create new instance of result object", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("default constructor of result object class is not accessible", e);
        }
        // set its properties
        values.forEach(t3 -> injectValue(result, t3._2, t3._1, t3._3.getJavaTypeClass()));
        // return it
        return result;
    }

    protected void checkCachedReflections(Seq<Tuple3<Object, String, ParameterType<?, ?>>> values) {
        if (constructor == null) {
            try {
                constructor = resultClass.getConstructor();
                if (!constructor.isAccessible() && !Modifier.isPublic(constructor.getModifiers())) {
                    constructor.setAccessible(true);
                }
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("default constructor not found in " + resultClass.getName(), e);
            }
        }
        if (methodsAndFields == null) {
            methodsAndFields = HashMap.ofEntries(
                    values.map(t3 -> Tuple.of(t3._2, findAccessor(t3._2, t3._3.getJavaTypeClass())))
            );
        }
    }

    protected Either<Method, Field> findAccessor(String propertyName, Class<?> typeJavaClass) {
        // try to find setter
        String setter;
        if (propertyName.isEmpty()) {
            setter = "set";
        } else {
            setter = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        try {
            Method method = resultClass.getMethod(setter, typeJavaClass);
            if (!method.isAccessible() && !Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            return Either.left(method);
        } catch (NoSuchMethodException ignored) {
            ;
        }
        // try to find field
        try {
            Field field = resultClass.getField(propertyName);
            if (!typeJavaClass.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException("field for property '" + propertyName + "'" +
                        " has incompatible type " + field.getType().getName() +
                        " (" + typeJavaClass.getName() + " is expected)");
            }
            if (!field.isAccessible() && !Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }
            return Either.right(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("no property '" + propertyName + "' getter or field found in class " + resultClass, e);
        }
    }

    private void injectValue(T result, String propertyName, Object value, Class<?> valueClass) {
        // try to use setter
        String setter;
        if (propertyName.isEmpty()) {
            setter = "set";
        } else {
            setter = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        try {
            Method method = resultClass.getMethod(setter, valueClass);
            if (!method.isAccessible() && !Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            method.invoke(result, value);
            return;
        } catch (NoSuchMethodException ignored) {
            ;
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("property '" + propertyName + "' setter invocation failed", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("property '" + propertyName + "' setter is not accessible", e);
        }
        // try direct field access if no getter found
        try {
            Field field = resultClass.getField(propertyName);
            if (!field.isAccessible() && !Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }
            field.set(result, value);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("no property '" + propertyName + "' setter or field found in class " + resultClass, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("property '" + propertyName + "' field is not accessible", e);
        }
    }

}
