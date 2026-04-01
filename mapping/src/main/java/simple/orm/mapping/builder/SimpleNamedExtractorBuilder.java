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
 * {@link ResultSet} column indexes are automatically assigned to parameters according to the order they were added to builder
 * (first parameter is extracted from column #1, next is extracted from column #2 and so on).
 */
public class SimpleNamedExtractorBuilder<T> extends AbstractNamedExtractorBuilder<T, SimpleNamedExtractorBuilder<T>> {

    public static <T> SimpleNamedExtractorBuilder<T> builder(MappersCollection mappers,
                                                             Class<T> targetClass) {
        return new SimpleNamedExtractorBuilder<T>()
                .withDefaultMappersFinder(mappers)
                .withDefaultReflectionsFinder()
                .targetClass(targetClass);
    }

    public static <T> SimpleNamedExtractorBuilder<T> builder(MappersFinder mappersFinder,
                                                             ReflectionsFinder reflectionsFinder,
                                                             Class<T> targetClass) {
        return new SimpleNamedExtractorBuilder<T>()
                .withCustomMappersFinder(mappersFinder)
                .withCustomReflectionsFinder(reflectionsFinder)
                .targetClass(targetClass);
    }

    public SimpleNamedExtractorBuilder<T> param(String propertyName, TypeMapper<?, ?> mapper) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return param(NamedParameter.of(params.get().size() + 1, propertyName, mapper));
    }

    public SimpleNamedExtractorBuilder<T> param(String propertyName, ParamInfo<?, ?> param) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        return param(NamedParameter.of(params.get().size() + 1, propertyName, param));
    }

    public SimpleNamedExtractorBuilder<T> param(String propertyName,
                                                String mapperName,
                                                String tag,
                                                ParameterJdbcType<?> jdbcType,
                                                Class<?> javaType
    ) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName is null");
        }
        return param(NamedParameter.of(params.get().size() + 1, propertyName, ParamInfo.of(mapperName, tag, jdbcType, javaType)));
    }

}
