package simple.orm.mapping.type;

import simple.orm.mapping.param.ParameterJdbcType;

/**
 * Abstract base class for {@link TypeMapper} implementations.
 */
public abstract class AbstractTypeMapper<Jdbc, Java> implements TypeMapper<Jdbc, Java> {

    protected final ParameterJdbcType<Jdbc> jdbcType;
    protected final Class<Java> javaType;

    public AbstractTypeMapper(ParameterJdbcType<Jdbc> jdbcType, Class<Java> javaType) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (javaType == null) {
            throw new NullPointerException("javaType is null");
        }
        this.jdbcType = jdbcType;
        this.javaType = javaType;
    }

    @Override
    public ParameterJdbcType<Jdbc> getJdbcType() {
        return jdbcType;
    }

    @Override
    public Class<Java> getJavaType() {
        return javaType;
    }

    @Override
    public abstract Jdbc javaToJdbc(Java value);

    @Override
    public abstract Java jdbcToJava(Jdbc value);

}
