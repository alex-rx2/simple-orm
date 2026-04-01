package simple.orm.mapping.builder;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

import java.sql.ResultSet;

/**
 * Builder for {@link IndexedExtractor}.
 * <br>
 * Objects are extracted by index and are returned in the same order as parameters were added to this builder.
 * {@link ResultSet} column indexes are automatically assigned to parameters according to their position in the sequence
 * (first object in sequence is extracted from column #1, next is extracted from column #2 and so on).
 */
public class SimpleIndexedExtractorBuilder extends AbstractIndexedExtractorBuilder<SimpleIndexedExtractorBuilder> {

    public static SimpleIndexedExtractorBuilder builder(MappersCollection mappers) {
        return new SimpleIndexedExtractorBuilder().withDefaultMappersFinder(mappers);
    }

    public static SimpleIndexedExtractorBuilder builder(MappersFinder mappersFinder) {
        return new SimpleIndexedExtractorBuilder().withCustomMappersFinder(mappersFinder);
    }

    public SimpleIndexedExtractorBuilder param(TypeMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return param(IndexedParameter.of(params.get().size() + 1, mapper));
    }

    public SimpleIndexedExtractorBuilder param(ParamInfo<?, ?> param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        return param(IndexedParameter.of(params.get().size() + 1, param));
    }

    public SimpleIndexedExtractorBuilder param(String mapperName,
                                               ParameterJdbcType<?> jdbcType,
                                               Class<?> javaType,
                                               String tag
    ) {
        return param(IndexedParameter.of(params.get().size() + 1, ParamInfo.of(mapperName, jdbcType, javaType, tag)));
    }

}
