package simple.orm.mapping.builder;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.impl.cache.ReflectionsCache;
import simple.orm.mapping.named.NamedInjectorImpl;
import simple.orm.mapping.named.NamedParameter;
import simple.orm.mapping.named.PropertyExtractor;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

/**
 * Builder for {@link NamedInjector}.
 *
 * @param <T> class of object used as source of parameters' values for query.
 */
public class NamedInjectorBuilder<T> {

    public static <T> NamedInjectorBuilder<T> builder(MappersFinder mappersFinder,
                                                      ReflectionsFinder reflectionsFinder) {
        return new NamedInjectorBuilder<T>()
                .withCustomMappersFinder(mappersFinder)
                .withCustomReflectionsFinder(reflectionsFinder);
    }

    public static <T> NamedInjectorBuilder<T> builder(MappersCollection mappers) {
        return new NamedInjectorBuilder<T>()
                .withDefaultMappersFinder(mappers)
                .withDefaultReflectionsFinder();
    }

    public static <T> NamedInjectorBuilder<T> builder(MappersFinder mappersFinder,
                                                      ReflectionsFinder reflectionsFinder,
                                                      Class<T> sourceClass) {
        return NamedInjectorBuilder.<T>builder(mappersFinder, reflectionsFinder)
                .sourceClass(sourceClass);
    }

    public static <T> NamedInjectorBuilder<T> builder(MappersCollection mappers, Class<T> sourceClass) {
        return NamedInjectorBuilder.<T>builder(mappers)
                .sourceClass(sourceClass);
    }

    protected MappersFinder mappersFinder;
    protected ReflectionsFinder reflectionsFinder;
    protected final Mutable<Seq<NamedParameter>> params;
    protected Class<T> sourceClass;
    protected final Mutable<Seq<PropertyExtractor<T, ?>>> extractors;

    protected NamedInjectorBuilder() {
        this.params = Mutable.of(List.empty());
        this.extractors = Mutable.of(List.empty());
    }

    public NamedInjectorBuilder<T> param(NamedParameter param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(param));
        return this;
    }

    public NamedInjectorBuilder<T> param(String propertyName, TypeMapper<?, ?> mapper) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        params.apply(p -> p.append(NamedParameter.of(p.size() + 1, propertyName, mapper)));
        return this;
    }

    public NamedInjectorBuilder<T> param(String propertyName, ParamInfo<?, ?> param) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(NamedParameter.of(p.size() + 1, propertyName, param)));
        return this;
    }

    public NamedInjectorBuilder<T> param(String propertyName,
                                         String mapperName,
                                         ParameterJdbcType<?> jdbcType,
                                         Class<?> javaType,
                                         String tag
    ) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        params.apply(p -> p.append(NamedParameter.of(
                p.size() + 1,
                propertyName,
                ParamInfo.of(mapperName, jdbcType, javaType, tag)
        )));
        return this;
    }

    public NamedInjectorBuilder<T> sourceClass(Class<T> sourceClass) {
        if (sourceClass == null) {
            throw new NullPointerException("sourceClass is null");
        }
        this.sourceClass = sourceClass;
        return this;
    }

    public NamedInjectorBuilder<T> extractor(PropertyExtractor<T, ?> extractor) {
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        extractors.apply(list -> list.append(extractor));
        return this;
    }

    public NamedInjectorBuilder<T> withCustomMappersFinder(MappersFinder mappersFinder) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        this.mappersFinder = mappersFinder;
        return this;
    }

    public NamedInjectorBuilder<T> withDefaultMappersFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        this.mappersFinder = new FoundMappersCache(mappers);
        return this;
    }

    public NamedInjectorBuilder<T> withCustomReflectionsFinder(ReflectionsFinder reflectionsFinder) {
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        this.reflectionsFinder = reflectionsFinder;
        return this;
    }

    public NamedInjectorBuilder<T> withDefaultReflectionsFinder() {
        this.reflectionsFinder = new ReflectionsCache();
        return this;
    }

    public NamedInjector<T> build() {
        if (sourceClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        return new NamedInjectorImpl<>(mappersFinder, reflectionsFinder, params.get(), sourceClass, extractors.get());
    }

}
