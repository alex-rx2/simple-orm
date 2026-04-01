package simple.orm.mapping.builder;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

/**
 * Builder for {@link IndexedExtractor}.
 * <br>
 * Objects are extracted by index and are returned in the same order as parameters were added to this builder.
 */
public class IndexIndexedExtractorBuilder extends AbstractIndexedExtractorBuilder<IndexIndexedExtractorBuilder> {

    public static IndexIndexedExtractorBuilder builder(MappersCollection mappers) {
        return new IndexIndexedExtractorBuilder().withDefaultMappersFinder(mappers);
    }

    public static IndexIndexedExtractorBuilder builder(MappersFinder mappersFinder) {
        return new IndexIndexedExtractorBuilder().withCustomMappersFinder(mappersFinder);
    }

    public IndexIndexedExtractorBuilder param(int index, TypeMapper<?, ?> mapper) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must be positive");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return param(IndexedParameter.of(index, mapper));
    }

    public IndexIndexedExtractorBuilder param(int index, ParamInfo<?, ?> param) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must be positive");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        return param(IndexedParameter.of(index, param));
    }

    public IndexIndexedExtractorBuilder param(int index,
                                              String mapperName,
                                              String tag,
                                              ParameterJdbcType<?> jdbcType,
                                              Class<?> javaType
    ) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must be positive");
        }
        return param(IndexedParameter.of(index, ParamInfo.of(mapperName, tag, jdbcType, javaType)));
    }

}
