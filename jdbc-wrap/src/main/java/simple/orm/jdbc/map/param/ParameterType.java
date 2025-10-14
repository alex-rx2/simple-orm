package simple.orm.jdbc.map.param;

import java.sql.JDBCType;

/**
 * Type metadata for JDBC (query or result) parameter.
 *
 * @param <Jdbc> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 * @param <Java> class implementing parameter in java (in custom business objects, etc.).
 */
public class ParameterType<Jdbc, Java> {

    protected final JDBCType jdbcType;
    protected final Class<Jdbc> jdbcClass;
    protected final Class<Java> javaClass;

    public ParameterType(JDBCType jdbcType, Class<Jdbc> jdbcClass, Class<Java> javaClass) {
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

    /**
     * Returns {@link JDBCType} type of parameter.
     *
     * @return {@link JDBCType} type of parameter.
     */
    public JDBCType getJDBCType() {
        return jdbcType;
    }

    /**
     * Returns class implementing parameter in java (in custom business objects, etc.).
     *
     * @return class implementing parameter in java.
     */
    public Class<Java> getJavaTypeClass() {
        return javaClass;
    }

    /**
     * Returns class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
     *
     * @return class implementing parameter value in JDBC.
     */
    public Class<Jdbc> getJDBCTypeImplementingClass() {
        return jdbcClass;
    }

    /**
     * Factory method.
     *
     * @return a ParameterType object.
     */
    public static <Jdbc, Java> ParameterType<Jdbc, Java> of(JDBCType jdbcType, Class<Jdbc> jdbcClass, Class<Java> javaClass) {
        return new ParameterType<>(jdbcType, jdbcClass, javaClass);
    }

    @Override
    public int hashCode() {
        return jdbcType.hashCode() * 31 * 31 + jdbcClass.hashCode() * 31 + javaClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterType)) {
            return false;
        }
        ParameterType<?, ?> other = (ParameterType<?, ?>) obj;
        return this.jdbcType == other.jdbcType
                && this.jdbcClass == other.jdbcClass
                && this.javaClass == other.javaClass
                ;
    }

    @Override
    public String toString() {
        return "ParameterType(" + jdbcType +
                ",jdbcClass=" + jdbcClass.getSimpleName() +
                ",javaClass=" + javaClass.getSimpleName() + ")"
                ;
    }
}
