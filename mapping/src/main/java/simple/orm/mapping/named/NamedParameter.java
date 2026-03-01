package simple.orm.mapping.named;

import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.type.TypeMapper;

/**
 * Parameter information for {@link NamedExtractorImpl} and {@link NamedInjectorImpl}.
 */
public class NamedParameter {

    public final Integer index;
    public final String label;
    public final String name;
    public final TypeMapper<?, ?> mapper;
    public final ParamInfo<?, ?> info;

    private NamedParameter(Integer index,
                           String label,
                           String name,
                           TypeMapper<?, ?> mapper,
                           ParamInfo<?, ?> info) {
        this.index = index;
        this.label = label;
        this.name = name;
        this.mapper = mapper;
        this.info = info;
    }

    public static NamedParameter of(String name, int index, TypeMapper<?, ?> mapper) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (name.isEmpty()) {
            throw new NullPointerException("name is empty");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return new NamedParameter(index, null, name, mapper, null);
    }

    public static NamedParameter of(String name, int index, ParamInfo<?, ?> info) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (name.isEmpty()) {
            throw new NullPointerException("name is empty");
        }
        if (info == null) {
            throw new NullPointerException("info is null");
        }
        return new NamedParameter(index, null, name, null, info);
    }

    public static NamedParameter of(String name, String label, TypeMapper<?, ?> mapper) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (name.isEmpty()) {
            throw new NullPointerException("name is empty");
        }
        if (label == null) {
            throw new NullPointerException("label is null");
        }
        if (label.isEmpty()) {
            throw new NullPointerException("label is empty");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return new NamedParameter(null, label, name, mapper, null);
    }

    public static NamedParameter of(String name, String label, ParamInfo<?, ?> info) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (name.isEmpty()) {
            throw new NullPointerException("name is empty");
        }
        if (label == null) {
            throw new NullPointerException("label is null");
        }
        if (label.isEmpty()) {
            throw new NullPointerException("label is empty");
        }
        if (info == null) {
            throw new NullPointerException("info is null");
        }
        return new NamedParameter(null, label, name, null, info);
    }

}
