package simple.orm.jdbc.common.builders;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import simple.orm.jdbc.common.map.NamedExtractorImpl;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.ParameterGetter;
import simple.orm.jdbc.param.ParameterType;

/**
 * Builder for {@link NamedExtractor}. With extraction of values by column label.
 *
 * @param <T> class of object extracted as each ResultSet row.
 */
public class NamedExtractorByLabelBuilder<T> extends NamedExtractorBuilder<T, NamedExtractorByLabelBuilder<T>> {

    public static <T> NamedExtractorByLabelBuilder<T> builder() {
        return new NamedExtractorByLabelBuilder<>();
    }

    protected Map<String, Tuple2<ParameterType<?, ?>, String>> types;

    public NamedExtractorByLabelBuilder() {
        this.types = HashMap.empty();
    }

    public NamedExtractorByLabelBuilder<T> param(String label, ParameterType<?, ?> type) {
        return param(label, type, label);
    }

    public NamedExtractorByLabelBuilder<T> param(String label, ParameterType<?, ?> type, String propertyName) {
        if (label == null) {
            throw new NullPointerException("label is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        this.types = this.types.put(label, Tuple.of(type, propertyName));
        return this;
    }

    public NamedExtractorByLabelBuilder<T> param(String label, ParameterType<?, ?> type, ParameterGetter<?> getter) {
        return param(label, type).withGetters(getter);
    }

    public NamedExtractorByLabelBuilder<T> param(String label, ParameterType<?, ?> type, ParameterGetter<?> getter, String propertyName) {
        return param(label, type, propertyName).withGetters(getter);
    }

    public NamedExtractorByLabelBuilder<T> params(Map<String, Tuple2<ParameterType<?, ?>, String>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(t2 -> t2._1 == null || t2._2 == null || t2._2._1 == null || t2._2._2 == null).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = types.merge(this.types);
        return this;
    }

    public NamedExtractorByLabelBuilder<T> paramsSimple(Map<String, ParameterType<?, ?>> types) {
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

    public NamedExtractor<T> build() {
        return new NamedExtractorImpl<>(resultClass, getters, types);
    }

}
