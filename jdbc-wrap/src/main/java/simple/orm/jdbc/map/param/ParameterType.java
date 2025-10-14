package simple.orm.jdbc.map.param;

import java.sql.JDBCType;

/**
 * Type metadata for JDBC (query or result) parameter.
 *
 * @param <T> class implementing parameter in java (in custom business objects, etc.).
 * @param <I> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 */
public class ParameterType<T, I> {

    protected final JDBCType jdbcType;
    protected final Class<T> javaClass;
    protected final Class<I> jdbcClass;

    public ParameterType(JDBCType jdbcType, Class<T> javaClass, Class<I> jdbcClass) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (javaClass == null) {
            throw new NullPointerException("javaClass is null");
        }
        if (jdbcClass == null) {
            throw new NullPointerException("jdbcClass is null");
        }
        this.jdbcType = jdbcType;
        this.javaClass = javaClass;
        this.jdbcClass = jdbcClass;
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
    public Class<T> getJavaTypeClass() {
        return javaClass;
    }

    /**
     * Returns class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
     *
     * @return class implementing parameter value in JDBC.
     */
    public Class<I> getJDBCTypeImplementingClass() {
        return jdbcClass;
    }

    /**
     * Factory method.
     *
     * @return a ParameterType object.
     */
    public static <T, I> ParameterType<T, I> of(JDBCType jdbcType, Class<T> javaClass, Class<I> jdbcClass) {
        return new ParameterType<>(jdbcType, javaClass, jdbcClass);
    }

    @Override
    public int hashCode() {
        return jdbcType.hashCode() * 31 * 31 + javaClass.hashCode() * 31 + jdbcClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterType)) {
            return false;
        }
        ParameterType<?, ?> other = (ParameterType<?, ?>) obj;
        return this.jdbcType == other.jdbcType
                && this.javaClass == other.javaClass
                && this.jdbcClass == other.jdbcClass
                ;
    }

    @Override
    public String toString() {
        return "ParameterType(" + jdbcType +
                ",javaClass=" + javaClass.getSimpleName() +
                ",jdbcClass=" + jdbcClass.getSimpleName() + ")"
                ;
    }
}
