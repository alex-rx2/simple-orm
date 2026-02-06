package simple.orm.jdbc.common.builders;

import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.common.map.IndexedExtractorImpl;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.ParameterGetter;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;

import java.util.Objects;

/**
 * Builder for {@link IndexedExtractor}.
 */
public class IndexedExtractorBuilder {

    public static IndexedExtractorBuilder builder() {
        return new IndexedExtractorBuilder();
    }

    protected Seq<ParameterType<?, ?>> types;
    protected Map<ParameterJdbcType<?>, ParameterGetter<?>> getters;

    protected IndexedExtractorBuilder() {
        this.types = List.empty();
        this.getters = HashMap.empty();
    }

    public IndexedExtractorBuilder param(ParameterType<?, ?> type) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        this.types = this.types.append(type);
        return this;
    }

    public IndexedExtractorBuilder param(ParameterType<?, ?> type, ParameterGetter<?> getter) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (getter == null) {
            throw new NullPointerException("getter is null");
        }
        if (!type.getParameterJdbcType().equals(getter.getJdbcType())) {
            throw new IllegalArgumentException("types mismatch");
        }
        this.types = this.types.append(type);
        this.getters = this.getters.put(getter.getJdbcType(), getter);
        return this;
    }

    public IndexedExtractorBuilder params(ParameterType<?, ?>... types) {
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

    public IndexedExtractorBuilder withGetters(ParameterGetter<?>... getters) {
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        return withGetters(List.of(getters));
    }

    public IndexedExtractorBuilder withGetters(Seq<ParameterGetter<?>> getters) {
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        if (getters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("getters contains nulls");
        }
        return withGetters(HashMap.ofEntries(getters.map(s -> Tuple.of(s.getJdbcType(), s))));
    }

    public IndexedExtractorBuilder withGetters(Map<ParameterJdbcType<?>, ParameterGetter<?>> getters) {
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        if (getters.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("getters contains nulls");
        }
        this.getters = getters.merge(this.getters);
        return this;
    }

    public IndexedExtractor build() {
        return new IndexedExtractorImpl(getters, types);
    }

}
