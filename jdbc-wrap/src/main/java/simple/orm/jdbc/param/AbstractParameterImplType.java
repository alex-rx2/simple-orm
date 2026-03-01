package simple.orm.jdbc.param;

import java.sql.JDBCType;

/**
 * {@link ParameterType} implementation without mappers.
 */
@Deprecated
public abstract class AbstractParameterImplType<Jdbc, Java> implements ParameterType<Jdbc, Java> {

    protected final JDBCType jdbcType;
    protected final Class<Jdbc> jdbcClass;
    protected final Class<Java> javaClass;

    protected AbstractParameterImplType(JDBCType jdbcType, Class<Jdbc> jdbcClass, Class<Java> javaClass) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (jdbcClass == null) {
            throw new NullPointerException("jdbcClass is null");
        }
        if (javaClass == null) {
            throw new NullPointerException("javaClass is null");
        }
        this.jdbcType = jdbcType;
        this.jdbcClass = jdbcClass;
        this.javaClass = javaClass;
    }

    @Override
    public JDBCType getJDBCType() {
        return jdbcType;
    }

    @Override
    public Class<Jdbc> getJDBCTypeClass() {
        return jdbcClass;
    }

    @Override
    public ParameterJdbcType<Jdbc> getParameterJdbcType() {
        return ParameterJdbcType.of(jdbcType, jdbcClass);
    }

    @Override
    public Class<Java> getJavaTypeClass() {
        return javaClass;
    }

    @Override
    public abstract Jdbc fromJava(Java value);

    @Override
    public abstract Java fromJDBC(Jdbc value);

}
