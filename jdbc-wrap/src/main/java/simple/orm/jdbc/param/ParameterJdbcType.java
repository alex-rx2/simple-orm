package simple.orm.jdbc.param;

import io.vavr.Function1;

import java.sql.JDBCType;

/**
 * Type metadata for JDBC (query or result) parameter.
 *
 * @param <Jdbc> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 */
@Deprecated
public interface ParameterJdbcType<Jdbc> {

    /**
     * Returns {@link JDBCType} type of parameter.
     *
     * @return {@link JDBCType} type of parameter.
     */
    JDBCType getJDBCType();

    /**
     * Returns class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
     *
     * @return class implementing parameter value in JDBC.
     */
    Class<Jdbc> getJDBCTypeClass();

    /**
     * Factory method to create {@link ParameterJdbcType}.
     */
    static <Jdbc> ParameterJdbcType<Jdbc> of(JDBCType jdbcType, Class<Jdbc> jdbcClass) {
        return new ParameterJdbcTypeImpl<>(jdbcType, jdbcClass);
    }

    /**
     * Factory method to create {@link ParameterType} of this type implemented by specified Java class.
     */
    <Java> ParameterType<Jdbc, Java> implementedBy(Class<Java> javaClass, Function1<Java, Jdbc> javaToJdbc, Function1<Jdbc, Java> jdbcToJava);

}
