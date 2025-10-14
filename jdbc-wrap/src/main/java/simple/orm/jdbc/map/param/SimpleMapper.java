package simple.orm.jdbc.map.param;

import java.util.function.Function;

/**
 * Implementation of {@link ParameterMapper} for simple cases, where mapping code is small, simple and is provided as functions.
 */
public class SimpleMapper<Jdbc, Java> extends AbstractParameterMapper<Jdbc, Java> {

    protected final Function<Java, Jdbc> mapToJDBC;
    protected final Function<Jdbc, Java> mapToJava;

    /**
     * @param type      JDBC type of parameter.
     * @param mapToJDBC function to map from java value to jdbc value.
     * @param mapToJava function to map from jdbc value to java value.
     */
    public SimpleMapper(ParameterType<Jdbc, Java> type,
                        Function<Java, Jdbc> mapToJDBC,
                        Function<Jdbc, Java> mapToJava) {
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
    public Jdbc mapToJDBC(Java object) {
        return mapToJDBC.apply(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Java mapToJava(Jdbc object) {
        return mapToJava.apply(object);
    }

}
