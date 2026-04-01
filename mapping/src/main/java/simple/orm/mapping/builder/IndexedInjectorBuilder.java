package simple.orm.mapping.builder;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.indexed.IndexedInjectorImpl;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

import java.sql.PreparedStatement;

/**
 * Builder for {@link IndexedInjector}.
 * <br>
 * Mapping of sequence objects into {@link PreparedStatement} indexes is obvious and very strict
 * so no indexes are passed to the builder itself. The index for injection is equal to the position
 * of object in a passed sequence (first object is injected as parameter #1, next as parameter #2 and so on).
 */
public class IndexedInjectorBuilder {

    public static IndexedInjectorBuilder builder(MappersFinder mappersFinder) {
        return new IndexedInjectorBuilder().withCustomMappersFinder(mappersFinder);
    }

    public static IndexedInjectorBuilder builder(MappersCollection mappers) {
        return new IndexedInjectorBuilder().withDefaultMappersFinder(mappers);
    }

    protected MappersFinder mappersFinder;
    protected final Mutable<Seq<IndexedParameter>> params;

    protected IndexedInjectorBuilder() {
        this.params = Mutable.of(List.empty());
    }

    public IndexedInjectorBuilder param(TypeMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        params.apply(p -> p.append(IndexedParameter.of(p.size() + 1, mapper)));
        return this;
    }

    public IndexedInjectorBuilder param(ParamInfo<?, ?> param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(IndexedParameter.of(p.size() + 1, param)));
        return this;
    }

    public IndexedInjectorBuilder param(String mapperName,
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

    public IndexedInjectorBuilder withCustomMappersFinder(MappersFinder mappersFinder) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        this.mappersFinder = mappersFinder;
        return this;
    }

    public IndexedInjectorBuilder withDefaultMappersFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        this.mappersFinder = new FoundMappersCache(mappers);
        return this;
    }

    public IndexedInjector build() {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        return new IndexedInjectorImpl(mappersFinder, params.get());
    }

}
