package simple.orm.jdbc.common.builders;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.impl.map.NamedInjectorImpl;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.map.ParameterSetter;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;

import java.util.Objects;

/**
 * Builder for {@link NamedInjector}.
 *
 * @param <T> class of object used as source of parameters' values for query.
 */
public class NamedInjectorBuilder<T> {

    public static <T> NamedInjectorBuilder<T> builder() {
        return new NamedInjectorBuilder<>();
    }

    protected Map<String, Tuple2<ParameterType<?, ?>, String>> types;
    protected Map<ParameterJdbcType<?>, ParameterSetter<?>> setters;

    public NamedInjectorBuilder() {
        this.types = HashMap.empty();
        this.setters = HashMap.empty();
    }

    public NamedInjectorBuilder<T> param(String name, ParameterType<?, ?> type) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        this.types = this.types.put(name, Tuple.of(type, name));
        return this;
    }

    public NamedInjectorBuilder<T> param(String paramName, ParameterType<?, ?> type, String propertyName) {
        if (paramName == null) {
            throw new NullPointerException("paramName is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        this.types = this.types.put(paramName, Tuple.of(type, propertyName));
        return this;
    }

    public NamedInjectorBuilder<T> param(String paramName, ParameterType<?, ?> type, ParameterSetter<?> setter, String propertyName) {
        if (paramName == null) {
            throw new NullPointerException("paramName is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        if (!type.getParameterJdbcType().equals(setter.getJdbcType())) {
            throw new IllegalArgumentException("types mismatch");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        this.types = this.types.put(paramName, Tuple.of(type, propertyName));
        this.setters = this.setters.put(setter.getJdbcType(), setter);
        return this;
    }

    public NamedInjectorBuilder<T> param(String name, ParameterType<?, ?> type, ParameterSetter<?> setter) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        if (!type.getParameterJdbcType().equals(setter.getJdbcType())) {
            throw new IllegalArgumentException("types mismatch");
        }
        this.types = this.types.put(name, Tuple.of(type, name));
        this.setters = this.setters.put(setter.getJdbcType(), setter);
        return this;
    }

    public NamedInjectorBuilder<T> params(Map<String, Tuple2<ParameterType<?, ?>, String>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(t2 -> t2._1 == null || t2._2 == null || t2._2._1 == null || t2._2._2 == null).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = types.merge(this.types);
        return this;
    }

    public NamedInjectorBuilder<T> paramsSimple(Map<String, ParameterType<?, ?>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = types
                .<String, Tuple2<ParameterType<?, ?>, String>>map((name, type) -> Tuple.of(name, Tuple.of(type, name)))
                .merge(this.types);
        return this;
    }

    public NamedInjectorBuilder<T> withSetters(ParameterSetter<?>... setters) {
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        return withSetters(List.of(setters));
    }

    public NamedInjectorBuilder<T> withSetters(Seq<ParameterSetter<?>> setters) {
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        if (setters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("setters contains nulls");
        }
        return withSetters(HashMap.ofEntries(setters.map(s -> Tuple.of(s.getJdbcType(), s))));
    }

    public NamedInjectorBuilder<T> withSetters(Map<ParameterJdbcType<?>, ParameterSetter<?>> setters) {
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        if (setters.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("setters contains nulls");
        }
        this.setters = setters.merge(this.setters);
        return this;
    }

    public NamedInjector<T> build() {
        return new NamedInjectorImpl<T>(setters, types);
    }
}
