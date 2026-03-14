package simple.orm.mapping.indexed;

import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.type.TypeMapper;

import java.util.Objects;

/**
 * Parameter information for {@link IndexedExtractorImpl} and {@link IndexedInjectorImpl}.
 */
public class IndexedParameter {

    public final int index;
    public final TypeMapper<?, ?> mapper;
    public final ParamInfo<?, ?> info;

    private IndexedParameter(int index, TypeMapper<?, ?> mapper, ParamInfo<?, ?> info) {
        this.index = index;
        this.mapper = mapper;
        this.info = info;
    }

    public static IndexedParameter of(int index, TypeMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return new IndexedParameter(index, mapper, null);
    }

    public static IndexedParameter of(int index, ParamInfo<?, ?> info) {
        if (info == null) {
            throw new NullPointerException("info is null");
        }
        return new IndexedParameter(index, null, info);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IndexedParameter that)) {
            return false;
        }
        return this.index == that.index
                && Objects.equals(this.mapper, that.mapper)
                && Objects.equals(this.info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, mapper, info);
    }

}
