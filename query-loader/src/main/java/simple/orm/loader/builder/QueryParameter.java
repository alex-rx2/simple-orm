package simple.orm.loader.builder;

import simple.orm.loader.QueryParser;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.TypeMapper;

/**
 * Parameter information for {@link QueryBuilder}.
 */
public record QueryParameter(
        ParameterType type,
        int indexWithinType, // starting with 1 as per JDBC API
        String label,
        String propName,
        TypeMapper<?, ?> mapper, // takes precedence over mapperName,tag,jdbcType,jdbcTypeName,javaType,javaTypeClassName
        String mapperName,
        String tag,
        ParameterJdbcType<?> jdbcType, // takes precedence over jdbcTypeName
        String jdbcTypeName,
        Class<?> javaType, // takes precedence over javaTypeClassName
        String javaTypeClassName
) {

    @Deprecated
    public static QueryParameter of(QueryParser.QueryParam qParam, boolean dropGuessedLabel) {
        return new QueryParameter(
                ParameterType.of(qParam.type()),
                qParam.indexWithinType(),
                qParam.labelGuessed() && dropGuessedLabel ? null : qParam.label(),
                qParam.propName(),
                null,
                qParam.mapperName(),
                qParam.tag(),
                null,
                qParam.jdbcTypeName(),
                null,
                qParam.javaClassName()
        );
    }

}
