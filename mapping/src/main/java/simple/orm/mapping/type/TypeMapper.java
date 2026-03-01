package simple.orm.mapping.type;

import simple.orm.mapping.param.ParameterJdbcType;

/**
 * Type mapper, capable of converting JDBC values to java values and vice versa.
 *
 * @param <Jdbc> class implementing parameter value in JDBC API (used to inject parameters or extracting it from result set).
 * @param <Java> class implementing parameter in java application (in custom business objects, etc.).
 */
public interface TypeMapper<Jdbc, Java> {

    /**
     * Returns JDBC type.
     *
     * @return JDBC type.
     */
    ParameterJdbcType<Jdbc> getJdbcType();

    /**
     * Returns java type.
     *
     * @return java type;
     */
    Class<Java> getJavaType();

    /**
     * Conversion method from java value to JDBC value.
     *
     * @param value java value.
     * @return JDBC value.
     */
    Jdbc javaToJdbc(Java value);

    /**
     * Conversion method from JDBC value to java value.
     *
     * @param value JDBC value.
     * @return Java value.
     */
    Java jdbcToJava(Jdbc value);

}
