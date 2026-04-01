package simple.orm.mapping.builder;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.mapping.named.NamedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

import java.sql.ResultSet;

import static simple.orm.util.StringUtils.empty;

/**
 * Builder for {@link NamedExtractor}.
 * <br>
 * Parameters are extracted from {@link ResultSet} by label and then assigned to properties of newly created object.
 */
public class LabelNamedExtractorBuilder<T> extends AbstractNamedExtractorBuilder<T, LabelNamedExtractorBuilder<T>> {

    public static <T> LabelNamedExtractorBuilder<T> builder(MappersCollection mappers,
                                                            Class<T> targetClass) {
        return new LabelNamedExtractorBuilder<T>()
                .withDefaultMappersFinder(mappers)
                .withDefaultReflectionsFinder()
                .targetClass(targetClass);
    }

    public static <T> LabelNamedExtractorBuilder<T> builder(MappersFinder mappersFinder,
                                                            ReflectionsFinder reflectionsFinder,
                                                            Class<T> targetClass) {
        return new LabelNamedExtractorBuilder<T>()
                .withCustomMappersFinder(mappersFinder)
                .withCustomReflectionsFinder(reflectionsFinder)
                .targetClass(targetClass);
    }

    public LabelNamedExtractorBuilder<T> param(String label, String propertyName, TypeMapper<?, ?> mapper) {
        if (empty(label)) {
            throw new IllegalArgumentException("label is empty or null");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return param(NamedParameter.of(propertyName, label, mapper));
    }

    public LabelNamedExtractorBuilder<T> param(String label, String propertyName, ParamInfo<?, ?> param) {
        if (empty(label)) {
            throw new IllegalArgumentException("label is empty or null");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        return param(NamedParameter.of(propertyName, label, param));
    }

    public LabelNamedExtractorBuilder<T> param(String label,
                                               String propertyName,
                                               String mapperName,
                                               ParameterJdbcType<?> jdbcType,
                                               Class<?> javaType,
                                               String tag
    ) {
        if (empty(label)) {
            throw new IllegalArgumentException("label is empty or null");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        return param(NamedParameter.of(propertyName, label, ParamInfo.of(mapperName, jdbcType, javaType, tag)));
    }

}
