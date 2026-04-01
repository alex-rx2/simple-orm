package simple.orm.mapping.builder;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.impl.cache.ReflectionsCache;
import simple.orm.mapping.named.NamedExtractorImpl;
import simple.orm.mapping.named.NamedParameter;
import simple.orm.mapping.named.ObjectConstructor;
import simple.orm.mapping.named.PropertyInjector;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.util.Mutable;

/**
 * Abstract base for builder of {@link NamedExtractor}.
 *
 * @param <SELF> actual builder, extending this abstract one.
 */
public class AbstractNamedExtractorBuilder<T, SELF extends AbstractNamedExtractorBuilder<T, SELF>> {

    protected MappersFinder mappersFinder;
    protected ReflectionsFinder reflectionsFinder;
    protected final Mutable<Seq<NamedParameter>> params;
    protected Class<T> targetClass;
    protected final Mutable<Seq<ObjectConstructor<?>>> constructors;
    protected final Mutable<Seq<PropertyInjector<?, ?>>> injectors;

    AbstractNamedExtractorBuilder() {
        this.params = Mutable.of(List.empty());
        this.constructors = Mutable.of(List.empty());
        this.injectors = Mutable.of(List.empty());
    }

    @SuppressWarnings("unchecked")
    SELF param(NamedParameter param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(param));
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF targetClass(Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        this.targetClass = targetClass;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF constructor(ObjectConstructor<?> constructor) {
        if (constructor == null) {
            throw new NullPointerException("constructor is null");
        }
        constructors.apply(list -> list.append(constructor));
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF injector(PropertyInjector<?, ?> injector) {
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        injectors.apply(list -> list.append(injector));
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withCustomMappersFinder(MappersFinder mappersFinder) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        this.mappersFinder = mappersFinder;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withDefaultMappersFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        this.mappersFinder = new FoundMappersCache(mappers);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withCustomReflectionsFinder(ReflectionsFinder reflectionsFinder) {
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        this.reflectionsFinder = reflectionsFinder;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withDefaultReflectionsFinder() {
        this.reflectionsFinder = new ReflectionsCache();
        return (SELF) this;
    }

    public NamedExtractor<T> build() {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        return new NamedExtractorImpl<>(
                mappersFinder, reflectionsFinder, params.get(), targetClass, constructors.get(), injectors.get());
    }

}
