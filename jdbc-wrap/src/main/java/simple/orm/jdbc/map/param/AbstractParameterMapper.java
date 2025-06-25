package simple.orm.jdbc.map.param;

/**
 * Base class for further {@link ParameterMapper} implementations.
 */
public abstract class AbstractParameterMapper<T, I> implements ParameterMapper<T, I> {

    protected final ParameterType<T, I> type;

    /**
     * @param type JDBC type of parameter.
     */
    public AbstractParameterMapper(ParameterType<T, I> type) {
        if (type == null)
            throw new NullPointerException("type is null");
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParameterType<T, I> getType() {
        return type;
    }

}
