package simple.orm.jdbc.map.param;

/**
 * Parameters mappers.
 *
 * @param <Jdbc> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 * @param <Java> class implementing parameter in java (in custom business objects, etc.).
 */
public interface ParameterMapper<Jdbc, Java> {

    /**
     * Returns parameter type metadata.
     *
     * @return parameter type.
     */
    ParameterType<Jdbc, Java> getType();

    /**
     * Map parameter from Java to JDBC.
     *
     * @param object java parameter value.
     * @return JDBC parameter value.
     */
    Jdbc mapToJDBC(Java object);

    /**
     * Map parameter from JDBC to Java.
     *
     * @param object JDBC parameter value.
     * @return java parameter value.
     */
    Java mapToJava(Jdbc object);

}
