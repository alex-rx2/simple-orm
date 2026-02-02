package simple.orm.jdbc.param;

import java.sql.JDBCType;
import java.util.Objects;
import java.util.function.Function;

/**
 * +
 * {@link ParameterJdbcType} implementation.
 */
public class ParameterJdbcTypeImpl<Jdbc> implements ParameterJdbcType<Jdbc> {

    protected JDBCType jdbcType;
    protected Class<Jdbc> jdbcClass;

    public ParameterJdbcTypeImpl(JDBCType jdbcType, Class<Jdbc> jdbcClass) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (jdbcClass == null) {
            throw new NullPointerException("jdbcClass is null");
        }
        this.jdbcType = jdbcType;
        this.jdbcClass = jdbcClass;
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
    public <Java> ParameterType<Jdbc, Java> implementedBy(Class<Java> javaClass,
                                                          Function<Java, Jdbc> javaToJdbc,
                                                          Function<Jdbc, Java> jdbcToJava) {
        return ParameterType.of(jdbcType, jdbcClass, javaClass, javaToJdbc, jdbcToJava);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ParameterJdbcType))
            return false;
        ParameterJdbcType<?> that = (ParameterJdbcType<?>) o;
        return jdbcType == that.getJDBCType()
                && jdbcClass == that.getJDBCTypeClass()
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jdbcType, jdbcClass);
    }

    @Override
    public String toString() {
        return "ParameterJdbcTypeImpl(" +
                "jdbcType=" + jdbcType + "," +
                "jdbcClass=" + jdbcClass.getName() +
                ")";
    }
}
