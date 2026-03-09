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
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

/**
 * Builder for {@link NamedExtractor}.
 */
public class NamedExtractorBuilder<T> {

    public static <T> NamedExtractorBuilder<T> builder(MappersFinder mappersFinder,
                                                       ReflectionsFinder reflectionsFinder) {
        return new NamedExtractorBuilder<T>()
                .withCustomMappersFinder(mappersFinder)
                .withCustomReflectionsFinder(reflectionsFinder);
    }

    public static <T> NamedExtractorBuilder<T> builder(MappersCollection mappers) {
        return new NamedExtractorBuilder<T>()
                .withDefaultMappersFinder(mappers)
                .withDefaultReflectionsFinder();
    }

    public static <T> NamedExtractorBuilder<T> builder(MappersFinder mappersFinder,
                                                       ReflectionsFinder reflectionsFinder,
                                                       Class<T> tagerClass) {
        return NamedExtractorBuilder.<T>builder(mappersFinder, reflectionsFinder)
                .targetClass(tagerClass);
    }

    public static <T> NamedExtractorBuilder<T> builder(MappersCollection mappers, Class<T> tagerClass) {
        return NamedExtractorBuilder.<T>builder(mappers)
                .targetClass(tagerClass);
    }

    protected MappersFinder mappersFinder;
    protected ReflectionsFinder reflectionsFinder;
    protected final Mutable<Seq<NamedParameter>> params;
    protected Class<T> targetClass;
    protected final Mutable<Seq<ObjectConstructor<?>>> constructors;
    protected final Mutable<Seq<PropertyInjector<?, ?>>> injectors;

    public NamedExtractorBuilder() {
        this.params = Mutable.of(List.empty());
        this.constructors = Mutable.of(List.empty());
        this.injectors = Mutable.of(List.empty());
    }

    public NamedExtractorBuilder<T> param(NamedParameter param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(param));
        return this;
    }

    public NamedExtractorBuilder<T> param(String propertyName, TypeMapper<?, ?> mapper) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        params.apply(p -> p.append(NamedParameter.of(propertyName, p.size() + 1, mapper)));
        return this;
    }

    public NamedExtractorBuilder<T> param(String propertyName, ParamInfo<?, ?> param) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(NamedParameter.of(propertyName, p.size() + 1, param)));
        return this;
    }

    public NamedExtractorBuilder<T> param(String propertyName,
                                          String mapperName,
                                          ParameterJdbcType<?> jdbcType,
                                          Class<?> javaType,
                                          String tag
    ) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        params.apply(p -> p.append(NamedParameter.of(
                propertyName,
                p.size() + 1,
                ParamInfo.of(mapperName, jdbcType, javaType, tag)
        )));
        return this;
    }

    public NamedExtractorBuilder<T> param(String propertyName, String label, TypeMapper<?, ?> mapper) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        params.apply(p -> p.append(NamedParameter.of(propertyName, label, mapper)));
        return this;
    }

    public NamedExtractorBuilder<T> param(String propertyName, String label, ParamInfo<?, ?> param) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(NamedParameter.of(propertyName, label, param)));
        return this;
    }

    public NamedExtractorBuilder<T> param(String propertyName,
                                          String label,
                                          String mapperName,
                                          ParameterJdbcType<?> jdbcType,
                                          Class<?> javaType,
                                          String tag
    ) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        params.apply(p -> p.append(NamedParameter.of(
                propertyName,
                label,
                ParamInfo.of(mapperName, jdbcType, javaType, tag)
        )));
        return this;
    }

    public NamedExtractorBuilder<T> targetClass(Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        this.targetClass = targetClass;
        return this;
    }

    public NamedExtractorBuilder<T> constructor(ObjectConstructor<?> constructor) {
        if (constructor == null) {
            throw new NullPointerException("constructor is null");
        }
        constructors.apply(list -> list.append(constructor));
        return this;
    }

    public NamedExtractorBuilder<T> injector(PropertyInjector<?, ?> injector) {
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        injectors.apply(list -> list.append(injector));
        return this;
    }

    public NamedExtractorBuilder<T> withCustomMappersFinder(MappersFinder mappersFinder) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        this.mappersFinder = mappersFinder;
        return this;
    }

    public NamedExtractorBuilder<T> withDefaultMappersFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        this.mappersFinder = new FoundMappersCache(mappers);
        return this;
    }

    public NamedExtractorBuilder<T> withCustomReflectionsFinder(ReflectionsFinder reflectionsFinder) {
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        this.reflectionsFinder = reflectionsFinder;
        return this;
    }

    public NamedExtractorBuilder<T> withDefaultReflectionsFinder() {
        this.reflectionsFinder = new ReflectionsCache();
        return this;
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
