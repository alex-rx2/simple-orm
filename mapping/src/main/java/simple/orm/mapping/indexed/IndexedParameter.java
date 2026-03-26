package simple.orm.mapping.indexed;

import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.type.TypeMapper;

import java.util.Objects;

import static simple.orm.util.StringUtils.empty;

/**
 * Parameter information for {@link IndexedExtractorImpl} and {@link IndexedInjectorImpl}.
 */
public class IndexedParameter {

    public final Integer index;
    public final String label;
    public final TypeMapper<?, ?> mapper;
    public final ParamInfo<?, ?> info;

    private IndexedParameter(Integer index, String label, TypeMapper<?, ?> mapper, ParamInfo<?, ?> info) {
        this.index = index;
        this.label = label;
        this.mapper = mapper;
        this.info = info;
    }

    public static IndexedParameter of(int index, TypeMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return new IndexedParameter(index, null, mapper, null);
    }

    public static IndexedParameter of(int index, ParamInfo<?, ?> info) {
        if (info == null) {
            throw new NullPointerException("info is null");
        }
        return new IndexedParameter(index, null, null, info);
    }

    public static IndexedParameter of(String label, TypeMapper<?, ?> mapper) {
        if (label == null) {
            throw new NullPointerException("label is null");
        }
        if (empty(label)) {
            throw new NullPointerException("label is empty");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return new IndexedParameter(null, label, mapper, null);
    }

    public static IndexedParameter of(String label, ParamInfo<?, ?> info) {
        if (label == null) {
            throw new NullPointerException("label is null");
        }
        if (empty(label)) {
            throw new NullPointerException("label is empty");
        }
        if (info == null) {
            throw new NullPointerException("info is null");
        }
        return new IndexedParameter(null, label, null, info);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IndexedParameter that)) {
            return false;
        }
        return Objects.equals(this.index, that.index)
                && Objects.equals(this.label, that.label)
                && Objects.equals(this.mapper, that.mapper)
                && Objects.equals(this.info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, label, mapper, info);
    }

}
