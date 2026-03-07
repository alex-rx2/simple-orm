package simple.orm.h2.type;

import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.AbstractTypeMapper;
import simple.orm.mapping.type.TypeMapper;

/**
 * {@link TypeMapper} for applications that represent SMALLINT as {@link Integer} in their code (data objects).
 */
public class ShortIntMapper extends AbstractTypeMapper<Short, Integer> {

    private final boolean ignoreOutOfRange;

    public ShortIntMapper(ParameterJdbcType<Short> jdbcType, boolean ignoreOutOfRange) {
        super(jdbcType, Integer.class);
        this.ignoreOutOfRange = ignoreOutOfRange;
    }

    @Override
    public Short javaToJdbc(Integer value) {
        if (value == null) {
            return null;
        }
        if (value > Short.MAX_VALUE) {
            if (ignoreOutOfRange) {
                return Short.MAX_VALUE;
            } else {
                throw new IllegalArgumentException("provided value " + value + " > Short.MAX");
            }
        }
        if (value < Short.MIN_VALUE) {
            if (ignoreOutOfRange) {
                return Short.MIN_VALUE;
            } else {
                throw new IllegalArgumentException("provided value " + value + " < Short.MIN");
            }
        }
        return value.shortValue();
    }

    @Override
    public Integer jdbcToJava(Short value) {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

}
