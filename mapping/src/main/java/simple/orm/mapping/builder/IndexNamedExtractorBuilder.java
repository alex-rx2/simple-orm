package simple.orm.mapping.builder;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.mapping.named.NamedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

import java.sql.ResultSet;

/**
 * Builder for {@link NamedExtractor}.
 * <br>
 * Parameters are extracted from {@link ResultSet} by index and then assigned to properties of newly created object.
 */
public class IndexNamedExtractorBuilder<T> extends AbstractNamedExtractorBuilder<T, IndexNamedExtractorBuilder<T>> {

    public static <T> IndexNamedExtractorBuilder<T> builder(MappersCollection mappers,
                                                            Class<T> targetClass) {
        return new IndexNamedExtractorBuilder<T>()
                .withDefaultMappersFinder(mappers)
                .withDefaultReflectionsFinder()
                .targetClass(targetClass);
    }

    public static <T> IndexNamedExtractorBuilder<T> builder(MappersFinder mappersFinder,
                                                            ReflectionsFinder reflectionsFinder,
                                                            Class<T> targetClass) {
        return new IndexNamedExtractorBuilder<T>()
                .withCustomMappersFinder(mappersFinder)
                .withCustomReflectionsFinder(reflectionsFinder)
                .targetClass(targetClass);
    }

    public IndexNamedExtractorBuilder<T> param(int index, String propertyName, TypeMapper<?, ?> mapper) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must be positive");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return param(NamedParameter.of(index, propertyName, mapper));
    }

    public IndexNamedExtractorBuilder<T> param(int index, String propertyName, ParamInfo<?, ?> param) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must be positive");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        return param(NamedParameter.of(index, propertyName, param));
    }

    public IndexNamedExtractorBuilder<T> param(int index,
                                               String propertyName,
                                               String mapperName,
                                               ParameterJdbcType<?> jdbcType,
                                               Class<?> javaType,
                                               String tag
    ) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must be positive");
        }
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        return param(NamedParameter.of(index, propertyName, ParamInfo.of(mapperName, jdbcType, javaType, tag)));
    }

}
