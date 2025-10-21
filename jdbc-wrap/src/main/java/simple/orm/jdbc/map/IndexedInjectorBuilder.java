package simple.orm.jdbc.map;

import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.in.IndexedInjector;
import simple.orm.jdbc.map.in.IndexedInjectorImpl;
import simple.orm.jdbc.map.in.ParameterSetter;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;

import java.util.Objects;

/**
 * Builder for {@link IndexedInjector}.
 */
public class IndexedInjectorBuilder {

    public static IndexedInjectorBuilder builder() {
        return new IndexedInjectorBuilder();
    }

    protected Seq<ParameterType<?, ?>> types;
    protected Map<ParameterJdbcType<?>, ParameterSetter<?>> setters;

    protected IndexedInjectorBuilder() {
        this.types = List.empty();
        this.setters = HashMap.empty();
    }

    public IndexedInjectorBuilder param(ParameterType<?, ?> type) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        this.types = this.types.append(type);
        return this;
    }

    public IndexedInjectorBuilder param(ParameterType<?, ?> type, ParameterSetter<?> setter) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        if (!type.getParameterJdbcType().equals(setter.getJdbcType())) {
            throw new IllegalArgumentException("types mismatch");
        }
        this.types = this.types.append(type);
        this.setters = this.setters.put(setter.getJdbcType(), setter);
        return this;
    }

    public IndexedInjectorBuilder params(ParameterType<?, ?>... types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        List<ParameterType<?, ?>> typesList = List.of(types);
        if (typesList.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = this.types.appendAll(typesList);
        return this;
    }

    public IndexedInjectorBuilder withSetters(ParameterSetter<?>... setters) {
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        return withSetters(List.of(setters));
    }

    public IndexedInjectorBuilder withSetters(Seq<ParameterSetter<?>> setters) {
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        if (setters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("setters contains nulls");
        }
        return withSetters(HashMap.ofEntries(setters.map(s -> Tuple.of(s.getJdbcType(), s))));
    }

    public IndexedInjectorBuilder withSetters(Map<ParameterJdbcType<?>, ParameterSetter<?>> setters) {
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        if (setters.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("setters contains nulls");
        }
        this.setters = setters.merge(this.setters);
        return this;
    }

    public IndexedInjector build() {
        return new IndexedInjectorImpl(setters, types);
    }

}
