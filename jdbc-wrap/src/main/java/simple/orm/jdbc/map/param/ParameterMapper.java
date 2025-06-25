package simple.orm.jdbc.map.param;

/**
 * Parameters mappers.
 *
 * @param <T> class implementing parameter in java (in custom business objects, etc.).
 * @param <I> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 */
public interface ParameterMapper<T, I> {

    /**
     * Returns parameter type metadata.
     *
     * @return parameter type.
     */
    ParameterType<T, I> getType();

    /**
     * Map parameter from Java to JDBC.
     *
     * @param object java parameter value.
     * @return JDBC parameter value.
     */
    I mapToJDBC(T object);

    /**
     * Map parameter from JDBC to Java.
     *
     * @param object JDBC parameter value.
     * @return java parameter value.
     */
    T mapToJava(I object);

}
