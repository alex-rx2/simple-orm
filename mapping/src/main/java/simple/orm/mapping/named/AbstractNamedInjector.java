package simple.orm.mapping.named;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.param.ParameterSetter;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import static simple.orm.util.RefUtils.isAssignableFrom;
import static simple.orm.util.StringUtils.qnn;

/**
 * Abstract {@link NamedInjector} implementation working by obtaining {@link PropertyExtractor} for each property.
 * Extracting property value from source object using obtained {@link PropertyExtractor}.
 * And injecting property value into {@link PreparedStatement}.
 * <br>
 * Concrete implementations should build {@link PropertyExtractor}s on demand
 * by implementing method {@link #buildExtractor(String, Class)}.
 * This method is called if no external {@link PropertyExtractor} was provided for property.
 */
public abstract class AbstractNamedInjector<T> implements NamedInjector<T> {

    protected final MappersFinder mappersFinder;
    protected final Seq<NamedParameter> parameters;
    protected final Class<T> sourceClass;
    // provided extractors
    protected final Map<RefName, PropertyExtractor<?, ?>> providedExtractors;
    // cache of built extractors
    protected final Mutable<Map<RefName, PropertyExtractor<?, ?>>> builtExtractors;

    protected AbstractNamedInjector(MappersFinder mappersFinder,
                                    Seq<NamedParameter> parameters,
                                    Class<T> sourceClass,
                                    Traversable<PropertyExtractor<T, ?>> extractors) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        if (parameters == null) {
            throw new NullPointerException("parameters is null");
        }
        if (parameters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("parameters contains nulls");
        }
        if (parameters.find(p -> p.index == null).isDefined()) {
            throw new IllegalArgumentException("all parameters must have index for injection");
        }
        if (sourceClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (extractors == null) {
            throw new NullPointerException("extractors is null");
        }
        if (extractors.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("extractors contains nulls");
        }
        this.mappersFinder = mappersFinder;
        this.parameters = parameters;
        this.sourceClass = sourceClass;
        this.providedExtractors = HashMap.ofEntries(extractors.map(e -> Tuple.of(e.getRef(), e)));
        this.builtExtractors = Mutable.of(HashMap.empty());
    }

    @Override
    public void injectParameters(PreparedStatement stmt, T source) {
        if (stmt == null) {
            throw new NullPointerException("stmt is null");
        }
        doInjectParameters(stmt, source);
    }

    private void doInjectParameters(PreparedStatement stmt, T source) {
        parameters.forEach(p -> doInjectParameter(stmt, p, source));
    }

    protected void doInjectParameter(PreparedStatement stmt, NamedParameter param, T source) {
        // obtain parameter value
        final Object value = extractValue(param.name, sourceClass, source);
        // inject into PreparedStatement
        doInject(stmt, param, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doInject(PreparedStatement stmt, NamedParameter param, Object value) {
        TypeMapper mapper = param.mapper;
        if (value == null && mapper == null) {
            // "manual" null insertion
            final Integer sqlType = mappersFinder.findSQLType(param.index, param.info, stmt);
            try {
                if (sqlType != null) {
                    stmt.setNull(param.index, sqlType);
                } else {
                    stmt.setObject(param.index, null);
                }
            } catch (SQLException e) {
                throw new JdbcException(e);
            }
        } else {
            if (mapper == null) {
                mapper = mappersFinder.findMapper(param.index, param.info, value.getClass(), stmt);
            }
            if (value != null && !isAssignableFrom(mapper.getJavaType(), value.getClass())) {
                throw new IllegalArgumentException("parameter " + qnn(param.name) + " actual value has unexpected type " +
                        value.getClass().getName() + " (while mapper java type is " + mapper.getJavaType().getName() + ")");
            }
            final Object jdbcValue = mapper.javaToJdbc(value);
            final ParameterSetter setter = mapper.getJdbcType().getSetter();
            setter.setValue(stmt, param.index, jdbcValue);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Object extractValue(String propertyPath, Class<?> sourceClass, Object source) {
        final int dot = propertyPath.indexOf('.');
        final String propName = dot == -1 ? propertyPath : propertyPath.substring(0, dot);
        final PropertyExtractor extractor = obtainExtractor(propName, sourceClass);
        final Object propValue = extractor.extractValue(source);
        if (dot == -1) {
            return propValue;
        } else {
            return extractValue(
                    propertyPath.substring(dot + 1),
                    propValue == null ? extractor.getValueClass() : propValue.getClass(),
                    propValue
            );
        }
    }

    protected PropertyExtractor<?, ?> obtainExtractor(String propName, Class<?> sourceClass) {
        final RefName refName = new RefName(propName, sourceClass);
        // get from provided or cached ones
        Option<PropertyExtractor<?, ?>> extractorOpt = providedExtractors
                .get(refName)
                .orElse(() -> builtExtractors.get().get(refName));
        if (extractorOpt.isDefined()) {
            return extractorOpt.get();
        }
        // try to find provided extractor for superclass/interface
        extractorOpt = providedExtractors
                .find(t2 -> propName.equals(t2._1.name()) && t2._1.target().isAssignableFrom(sourceClass))
                .map(Tuple2::_2);
        if (!extractorOpt.isDefined()) {
            // if nothing found - build new extractor
            extractorOpt = Option.of(buildExtractor(propName, sourceClass));
        }
        // cache extractor
        final PropertyExtractor<?, ?> extractor = extractorOpt.get();
        builtExtractors.apply(cache -> cache.put(extractor.getRef(), extractor));
        // and return it
        return extractor;
    }

    /**
     * Abstract method to build {@link PropertyExtractor} on demand for specified property.
     *
     * @param propName    name of the property.
     * @param sourceClass type of object that holds the property.
     * @return new {@link PropertyExtractor} for the property.
     */
    protected abstract <X> PropertyExtractor<X, ?> buildExtractor(String propName, Class<X> sourceClass);

}
