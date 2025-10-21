package simple.orm.jdbc.map;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.out.NamedExtractor;
import simple.orm.jdbc.map.out.NamedExtractorImpl;
import simple.orm.jdbc.map.out.ParameterGetter;
import simple.orm.jdbc.param.ParameterType;

/**
 * Builder for {@link NamedExtractor}. With extraction of values by column index.
 *
 * @param <T> class of object extracted as each ResultSet row.
 */
public class NamedExtractorByIndexBuilder<T> extends NamedExtractorBuilder<T, NamedExtractorByIndexBuilder<T>> {

    public static <T> NamedExtractorByIndexBuilder<T> builder() {
        return new NamedExtractorByIndexBuilder<>();
    }

    protected Seq<Tuple2<ParameterType<?, ?>, String>> types;

    public NamedExtractorByIndexBuilder() {
        this.types = List.empty();
    }

    public NamedExtractorByIndexBuilder<T> param(ParameterType<?, ?> type, String propertyName) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        this.types = this.types.append(Tuple.of(type, propertyName));
        return this;
    }

    public NamedExtractorByIndexBuilder<T> param(ParameterType<?, ?> type, ParameterGetter<?> getter, String propertyName) {
        return param(type, propertyName).withGetters(getter);
    }

    public NamedExtractorByIndexBuilder<T> params(Seq<Tuple2<ParameterType<?, ?>, String>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        this.types = this.types.appendAll(types);
        return this;
    }

    public NamedExtractor<T> build() {
        return new NamedExtractorImpl<>(resultClass, getters, types);
    }
}
