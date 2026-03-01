package simple.orm.mapping.named;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

import java.sql.ResultSet;
import java.util.Objects;

import static simple.orm.util.RefUtils.isAssignableFrom;
import static simple.orm.util.StringUtils.qnn;

/**
 * Abstract {@link NamedExtractor} implementation working by using combination of
 * {@link ObjectConstructor}s and {@link PropertyInjector}s to create result object for each extracted
 * {@link ResultSet} row.
 * <br>
 * Concrete implementations should build {@link ObjectConstructor}s and {@link PropertyInjector}s on demand
 * by implementing corresponding build methods
 * ({@link #buildObjectConstructor(Class, Map)}, {@link #buildInjector(Class, String)}).
 * These methods are called if no external {@link ObjectConstructor} or {@link PropertyInjector} was provided.
 */
public abstract class AbstractNamedExtractor<T> implements NamedExtractor<T> {

    protected final MappersFinder mappersFinder;
    protected final Seq<NamedParameter> parameters;
    protected final Class<T> targetClass;
    protected final Map<Class<?>, ObjectConstructor<?>> providedConstructors;
    protected final Map<RefName, PropertyInjector<?, ?>> providedInjectors;
    protected final Mutable<Map<Class<?>, ObjectConstructor<?>>> builtConstructors;
    protected final Mutable<Map<RefName, PropertyInjector<?, ?>>> builtInjectors;

    public AbstractNamedExtractor(MappersFinder mappersFinder,
                                  Seq<NamedParameter> parameters,
                                  Class<T> targetClass,
                                  Traversable<ObjectConstructor<?>> constructors,
                                  Traversable<PropertyInjector<?, ?>> injectors
    ) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        if (parameters == null) {
            throw new NullPointerException("parameters is null");
        }
        if (parameters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("parameters contains nulls");
        }
        if (parameters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("extractors contains nulls");
        }
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (injectors == null) {
            throw new NullPointerException("injectors is null");
        }
        if (injectors.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("injectors contains nulls");
        }
        if (constructors == null) {
            throw new NullPointerException("constructors is null");
        }
        if (constructors.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("constructors contains nulls");
        }
        this.mappersFinder = mappersFinder;
        this.parameters = parameters;
        this.targetClass = targetClass;
        this.providedConstructors = HashMap.ofEntries(constructors.map(c -> Tuple.of(c.getTargetClass(), c)));
        this.providedInjectors = HashMap.ofEntries(injectors.map(e -> Tuple.of(e.getRef(), e)));
        this.builtConstructors = Mutable.of(HashMap.empty());
        this.builtInjectors = Mutable.of(HashMap.empty());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T extractRow(ResultSet rs) {
        final Seq<Tuple2<String, Object>> values = parameters.map(param -> extractValue(rs, param));
        return (T) createObject(targetClass, HashMap.ofEntries(values));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Tuple2<String, Object> extractValue(ResultSet rs, NamedParameter param) {
        final Integer index = param.index;
        final String label = param.label;
        final TypeMapper mapper = param.mapper != null ?
                param.mapper :
                (index == null ?
                        mappersFinder.findMapper(label, param.info, rs) :
                        mappersFinder.findMapper(index, param.info, rs)
                );
        final ParameterGetter getter = mapper.getJdbcType().getGetter();
        final Object jdbcValue = index == null ? getter.getValue(rs, label) : getter.getValue(rs, index);
        return Tuple.of(param.name, mapper.jdbcToJava(jdbcValue));
    }

    protected Object createObject(Class<?> targetClass, Map<String, Object> values) {
        final ObjectConstructor<?> constructor = obtainObjectConstructor(targetClass, values);
        final ObjectConstructor.CreatedObject<?> createdObject = constructor.createNew(values);
        final Map<String, Object> valuesToInject = values.removeAll(createdObject.consumedProperties);
        if (createdObject.object == null) {
            // check if there are non-null properties to inject into null object
            if (valuesToInject.find(t2 -> t2._2 != null).isDefined()) {
                throw new IllegalArgumentException("created object is null, but some of its " +
                        "unconsumed properties are not null, all object values: " + values);
            }
            return null;
        } else {
            // group properties
            final Map<String, Map<String, Object>> groupedProperties = groupProperties(valuesToInject);
            // inject simple properties
            groupedProperties.get("").getOrElse(HashMap.empty())
                    .forEach((propName, value) -> injectSimpleValue(createdObject.object, propName, value));
            // inject complex properties (other objects to create)
            groupedProperties.remove("")
                    .forEach((propName, subProps) -> injectComplexValue(createdObject.object, propName, subProps));
            // return object
            return createdObject.object;
        }
    }

    protected ObjectConstructor<?> obtainObjectConstructor(Class<?> targetClass, Map<String, Object> values) {
        // check provided
        Option<ObjectConstructor<?>> constructorOpt = providedConstructors.get(targetClass);
        if (constructorOpt.isDefined()) {
            return constructorOpt.get();
        }
        // check cached
        constructorOpt = builtConstructors.get().get(targetClass);
        if (constructorOpt.isDefined()) {
            return constructorOpt.get();
        }
        // build and cache new one
        ObjectConstructor<?> constructor = buildObjectConstructor(targetClass, values);
        builtConstructors.apply(cache -> cache.put(targetClass, constructor));
        return constructor;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void injectSimpleValue(Object object, String propName, Object value) {
        final PropertyInjector injector = obtainInjector(object.getClass(), propName);
        if (value != null && !isAssignableFrom(injector.getValueClass(), value.getClass())) {
            throw new IllegalArgumentException("property " + qnn(propName) + " of " + object.getClass().getName() +
                    " has incompatible type (" + injector.getValueClass().getName() +
                    ") with type of value extracted from ResultSet (" + value.getClass().getName() + ')');
        }
        injector.injectProperty(object, value);
    }

    protected void injectComplexValue(Object object, String propName, Map<String, Object> subProps) {
        final PropertyInjector<?, ?> injector = obtainInjector(object.getClass(), propName);
        final Class<?> valueClass = injector.getValueClass();
        final Object value = createObject(valueClass, subProps);
        injectSimpleValue(object, propName, value);
    }

    protected PropertyInjector<?, ?> obtainInjector(Class<?> targetClass, String propName) {
        final RefName ref = new RefName(propName, targetClass);
        // check provided
        Option<PropertyInjector<?, ?>> injectorOpt = providedInjectors.get(ref);
        if (injectorOpt.isDefined()) {
            return injectorOpt.get();
        }
        // check cached
        injectorOpt = builtInjectors.get().get(ref);
        if (injectorOpt.isDefined()) {
            return injectorOpt.get();
        }
        // build and cache new one
        final PropertyInjector<?, ?> injector = buildInjector(targetClass, propName);
        builtInjectors.apply(cache -> cache.put(ref, injector));
        return injector;
    }

    protected abstract ObjectConstructor<?> buildObjectConstructor(Class<?> targetClass, Map<String, Object> values);

    protected abstract PropertyInjector<?, ?> buildInjector(Class<?> targetClass, String propName);

    protected static Map<String, Map<String, Object>> groupProperties(Map<String, Object> values) {
        return values
                // group values by their topmost property name
                // (e.g. property x.y.z goes to group x, property x goes to group with empty name)
                .groupBy(t2 -> groupKey(t2._1))
                // remove property prefix in subgroups (e.g. x.y.z -> y.z, but x stays x)
                .mapValues(AbstractNamedExtractor::removePropertyNamePrefix);
    }

    private static String groupKey(String propertyName) {
        final int dot = propertyName.indexOf('.');
        return dot == -1 ? "" : propertyName.substring(0, dot);
    }

    private static Map<String, Object> removePropertyNamePrefix(Map<String, Object> group) {
        return group.mapKeys(AbstractNamedExtractor::removePropertyNamePrefix);
    }

    private static String removePropertyNamePrefix(String propertyName) {
        final int dot = propertyName.indexOf('.');
        return dot == -1 ? propertyName : propertyName.substring(dot + 1);
    }

}
