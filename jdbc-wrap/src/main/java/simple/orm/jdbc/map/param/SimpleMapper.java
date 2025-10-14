package simple.orm.jdbc.map.param;

import java.util.function.Function;

/**
 * Implementation of {@link ParameterMapper} for simple cases, where mapping code is small, simple and is provided as functions.
 */
public class SimpleMapper<T, I> extends AbstractParameterMapper<T, I> {

    protected final Function<T, I> mapToJDBC;
    protected final Function<I, T> mapToJava;

    /**
     * @param type      JDBC type of parameter.
     * @param mapToJDBC function to map from java value to jdbc value.
     * @param mapToJava function to map from jdbc value to java value.
     */
    public SimpleMapper(ParameterType<T, I> type,
                        Function<T, I> mapToJDBC,
                        Function<I, T> mapToJava) {
        super(type);
        if (mapToJDBC == null) {
            throw new NullPointerException("mapToJDBC is null");
        }
        if (mapToJava == null) {
            throw new NullPointerException("mapToJava is null");
        }
        this.mapToJDBC = mapToJDBC;
        this.mapToJava = mapToJava;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public I mapToJDBC(T object) {
        return mapToJDBC.apply(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T mapToJava(I object) {
        return mapToJava.apply(object);
    }

}
