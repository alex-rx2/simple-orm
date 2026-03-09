package simple.orm.mapping.builder;

import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.indexed.IndexedExtractorImpl;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

import java.util.Objects;

/**
 * Builder for {@link IndexedExtractor}.
 */
public class IndexedExtractorBuilder {

    public static IndexedExtractorBuilder builder(MappersFinder mappersFinder) {
        return new IndexedExtractorBuilder().withCustomMappersFinder(mappersFinder);
    }

    public static IndexedExtractorBuilder builder(MappersCollection mappers) {
        return new IndexedExtractorBuilder().withDefaultMappersFinder(mappers);
    }

    protected MappersFinder mappersFinder;
    protected final Mutable<Seq<IndexedParameter>> params;

    protected IndexedExtractorBuilder() {
        this.params = Mutable.of(List.empty());
    }

    public IndexedExtractorBuilder param(TypeMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        params.apply(p -> p.append(IndexedParameter.of(p.size() + 1, mapper)));
        return this;
    }

    public IndexedExtractorBuilder params(TypeMapper<?, ?>... mappers) {
        return paramsMappers(Array.of(mappers));
    }

    public IndexedExtractorBuilder paramsMappers(Traversable<TypeMapper<?, ?>> mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        if (mappers.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("mappers contains null elements");
        }
        mappers.forEach(this::param);
        return this;
    }

    public IndexedExtractorBuilder param(ParamInfo<?, ?> param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(IndexedParameter.of(p.size() + 1, param)));
        return this;
    }

    public IndexedExtractorBuilder params(ParamInfo<?, ?>... params) {
        return paramsInfos(Array.of(params));
    }

    public IndexedExtractorBuilder paramsInfos(Traversable<ParamInfo<?, ?>> params) {
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        if (params.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("params contains null elements");
        }
        params.forEach(this::param);
        return this;
    }

    public IndexedExtractorBuilder param(String mapperName,
                                         ParameterJdbcType<?> jdbcType,
                                         Class<?> javaType,
                                         String tag
    ) {
        params.apply(p -> p.append(IndexedParameter.of(
                p.size() + 1,
                ParamInfo.of(mapperName, jdbcType, javaType, tag)
        )));
        return this;
    }

    public IndexedExtractorBuilder withCustomMappersFinder(MappersFinder mappersFinder) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        this.mappersFinder = mappersFinder;
        return this;
    }

    public IndexedExtractorBuilder withDefaultMappersFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        this.mappersFinder = new FoundMappersCache(mappers);
        return this;
    }

    public IndexedExtractor build() {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        return new IndexedExtractorImpl(mappersFinder, params.get());
    }

}
