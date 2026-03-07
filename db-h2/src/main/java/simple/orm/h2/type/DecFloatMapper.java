package simple.orm.h2.type;

import simple.orm.h2.H2Types;
import simple.orm.h2.param.DecFloat;
import simple.orm.mapping.type.AbstractTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.math.BigDecimal;

/**
 * {@link TypeMapper} for DECFLOAT data type.
 */
public class DecFloatMapper extends AbstractTypeMapper<String, DecFloat> {

    public DecFloatMapper() {
        super(H2Types.DECFLOAT, DecFloat.class);
    }

    @Override
    public String javaToJdbc(DecFloat value) {
        return value == null ?
                null :
                value.type == DecFloat.Type.BIGDECIMAL ?
                        value.value.toString() :
                        value.type.special;
    }

    @Override
    public DecFloat jdbcToJava(String value) {
        if (value == null) {
            return null;
        }
        final DecFloat.Type type = DecFloat.Type.of(value);
        return new DecFloat(type, type == DecFloat.Type.BIGDECIMAL ? new BigDecimal(value) : null);
    }

}
