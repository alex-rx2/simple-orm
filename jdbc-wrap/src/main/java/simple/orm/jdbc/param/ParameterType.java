package simple.orm.jdbc.param;

import io.vavr.Function1;

import java.sql.JDBCType;

/**
 * JDBC query or result parameter specification.
 * With JDBC and Java implementation class information and conversion methods between them.
 *
 * @param <Jdbc> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 * @param <Java> class implementing parameter in java (in custom business objects, etc.).
 */
@Deprecated
public interface ParameterType<Jdbc, Java> {

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
     * Returns {@link ParameterJdbcType} part of this parameter.
     *
     * @return ParameterJdbcType part of this parameter.
     */
    ParameterJdbcType<Jdbc> getParameterJdbcType();

    /**
     * Returns class implementing parameter in java (in custom business objects, etc.).
     *
     * @return class implementing parameter in java.
     */
    Class<Java> getJavaTypeClass();

    /**
     * Conversion method from Java value to JDBC value.
     *
     * @param value Java value of parameter.
     * @return JDBC value of parameter.
     */
    Jdbc fromJava(Java value);

    /**
     * Conversion method from JDBC value to Java value.
     *
     * @param value JDBC value of parameter.
     * @return Java value of parameter.
     */
    Java fromJDBC(Jdbc value);

    /**
     * Factory method.
     */
    static <Jdbc, Java> ParameterType<Jdbc, Java> of(JDBCType jdbcType,
                                                     Class<Jdbc> jdbcClass,
                                                     Class<Java> javaClass,
                                                     Function1<Java, Jdbc> javaToJdbc,
                                                     Function1<Jdbc, Java> jdbcToJava) {
        return new ParameterTypeImpl<>(jdbcType, jdbcClass, javaClass, javaToJdbc, jdbcToJava);
    }

}
