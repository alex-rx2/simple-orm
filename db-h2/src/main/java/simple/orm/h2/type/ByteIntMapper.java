package simple.orm.h2.type;

import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.AbstractTypeMapper;
import simple.orm.mapping.type.TypeMapper;

/**
 * {@link TypeMapper} for applications that represent TINYINT as {@link Integer} in their code (data objects).
 */
public class ByteIntMapper extends AbstractTypeMapper<Byte, Integer> {

    private final boolean ignoreOutOfRange;

    public ByteIntMapper(ParameterJdbcType<Byte> jdbcType, boolean ignoreOutOfRange) {
        super(jdbcType, Integer.class);
        this.ignoreOutOfRange = ignoreOutOfRange;
    }

    @Override
    public Byte javaToJdbc(Integer value) {
        if (value == null) {
            return null;
        }
        if (value > Byte.MAX_VALUE) {
            if (ignoreOutOfRange) {
                return Byte.MAX_VALUE;
            } else {
                throw new IllegalArgumentException("provided value " + value + " > Byte.MAX");
            }
        }
        if (value < Byte.MIN_VALUE) {
            if (ignoreOutOfRange) {
                return Byte.MIN_VALUE;
            } else {
                throw new IllegalArgumentException("provided value " + value + " < Byte.MIN");
            }
        }
        return value.byteValue();
    }

    @Override
    public Integer jdbcToJava(Byte value) {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

}
