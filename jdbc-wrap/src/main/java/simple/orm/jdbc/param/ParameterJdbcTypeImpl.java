package simple.orm.jdbc.param;

import io.vavr.Function1;

import java.sql.JDBCType;
import java.util.Objects;

/**
 * +
 * {@link ParameterJdbcType} implementation.
 */
@Deprecated
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
                                                          Function1<Java, Jdbc> javaToJdbc,
                                                          Function1<Jdbc, Java> jdbcToJava) {
        return ParameterType.of(jdbcType, jdbcClass, javaClass, javaToJdbc, jdbcToJava);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ParameterJdbcType<?> that))
            return false;
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
        return "JdbcType(" + jdbcType + "->" + jdbcClass.getName() + ")";
    }
}
