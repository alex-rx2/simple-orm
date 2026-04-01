package simple.orm.mapping.builder;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

import static simple.orm.util.StringUtils.empty;

/**
 * Builder for {@link IndexedExtractor}.
 * <br>
 * Objects are extracted by label and are returned in the same order as parameters were added to this builder.
 */
public class LabelIndexedExtractorBuilder extends AbstractIndexedExtractorBuilder<LabelIndexedExtractorBuilder> {

    public static LabelIndexedExtractorBuilder builder(MappersCollection mappers) {
        return new LabelIndexedExtractorBuilder().withDefaultMappersFinder(mappers);
    }

    public static LabelIndexedExtractorBuilder builder(MappersFinder mappersFinder) {
        return new LabelIndexedExtractorBuilder().withCustomMappersFinder(mappersFinder);
    }

    public LabelIndexedExtractorBuilder param(String label, TypeMapper<?, ?> mapper) {
        if (empty(label)) {
            throw new IllegalArgumentException("label is empty or null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return param(IndexedParameter.of(label, mapper));
    }

    public LabelIndexedExtractorBuilder param(String label, ParamInfo<?, ?> param) {
        if (empty(label)) {
            throw new IllegalArgumentException("label is empty or null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        return param(IndexedParameter.of(label, param));
    }

    public LabelIndexedExtractorBuilder param(String label,
                                              String mapperName,
                                              String tag,
                                              ParameterJdbcType<?> jdbcType,
                                              Class<?> javaType
    ) {
        if (empty(label)) {
            throw new IllegalArgumentException("label is empty or null");
        }
        return param(IndexedParameter.of(label, ParamInfo.of(mapperName, tag, jdbcType, javaType)));
    }

}
