package simple.orm.jdbc.map.param;

import java.sql.JDBCType;

/**
 * Type metadata for JDBC (query or result) parameter.
 *
 * @param <T> class implementing parameter in java (in custom business objects, etc.).
 * @param <I> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 *
 * TODO JDBCType seems to be of no use for us
 */
public class ParameterType<T, I> {

    protected final JDBCType jdbcType;
    protected final Class<T> javaClass;
    protected final Class<I> jdbcClass;

    public ParameterType(JDBCType jdbcType, Class<T> javaClass, Class<I> jdbcClass) {
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

}
