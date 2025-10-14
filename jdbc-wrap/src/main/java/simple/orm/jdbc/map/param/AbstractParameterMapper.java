package simple.orm.jdbc.map.param;

/**
 * Base class for further {@link ParameterMapper} implementations.
 */
public abstract class AbstractParameterMapper<Jdbc, Java> implements ParameterMapper<Jdbc, Java> {

    protected final ParameterType<Jdbc, Java> type;

    /**
     * @param type JDBC type of parameter.
     */
    public AbstractParameterMapper(ParameterType<Jdbc, Java> type) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParameterType<Jdbc, Java> getType() {
        return type;
    }

}
