package simple.orm.jdbc.map.builders;

import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.impl.map.ParameterGetterImpl;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.ParameterGetter;
import simple.orm.jdbc.param.ParameterJdbcType;

import java.util.Objects;

/**
 * Base class for {@link NamedExtractor} builders.
 */
abstract class NamedExtractorBuilder<T, SELF> {

    protected Class<T> resultClass;
    protected Map<ParameterJdbcType<?>, ParameterGetter<?>> getters;

    public NamedExtractorBuilder() {
        this.getters = ParameterGetterImpl.DEFAULT_GETTERS_MAP;
    }

    @SuppressWarnings("unchecked")
    public SELF resultClass(Class<T> resultClass) {
        if (resultClass == null) {
            throw new NullPointerException("resultClass is null");
        }
        if (this.resultClass != null) {
            throw new NullPointerException("resultClass is already defined");
        }
        this.resultClass = resultClass;
        return (SELF) this;
    }

    public SELF withGetters(ParameterGetter<?>... getters) {
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        return withGetters(List.of(getters));
    }

    public SELF withGetters(Seq<ParameterGetter<?>> getters) {
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        if (getters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("getters contains nulls");
        }
        return withGetters(HashMap.ofEntries(getters.map(s -> Tuple.of(s.getJdbcType(), s))));
    }

    @SuppressWarnings("unchecked")
    public SELF withGetters(Map<ParameterJdbcType<?>, ParameterGetter<?>> getters) {
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        if (getters.find(t2 -> t2._1 == null || t2._2 == null).isDefined()) {
            throw new NullPointerException("getters contains nulls");
        }
        this.getters = getters.merge(this.getters);
        return (SELF) this;
    }

}
