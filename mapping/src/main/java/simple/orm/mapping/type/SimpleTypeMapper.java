package simple.orm.mapping.type;

import io.vavr.Function1;
import simple.orm.mapping.param.ParameterJdbcType;

/**
 * Simple implementation of {@link TypeMapper}.
 */
public class SimpleTypeMapper<Jdbc, Java> extends AbstractTypeMapper<Jdbc, Java> {

    protected final Function1<Java, Jdbc> toJdbc;
    protected final Function1<Jdbc, Java> toJava;

    public SimpleTypeMapper(ParameterJdbcType<Jdbc> jdbcType,
                            Class<Java> javaType,
                            Function1<Java, Jdbc> toJdbc,
                            Function1<Jdbc, Java> toJava
    ) {
        super(jdbcType, javaType);
        if (toJdbc == null) {
            throw new NullPointerException("toJdbc is null");
        }
        if (toJava == null) {
            throw new NullPointerException("toJava is null");
        }
        this.toJdbc = toJdbc;
        this.toJava = toJava;
    }

    @Override
    public Jdbc javaToJdbc(Java value) {
        return toJdbc.apply(value);
    }

    @Override
    public Java jdbcToJava(Jdbc value) {
        return toJava.apply(value);
    }

}
