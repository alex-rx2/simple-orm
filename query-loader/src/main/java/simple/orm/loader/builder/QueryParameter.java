package simple.orm.loader.builder;

import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.TypeMapper;

/**
 * Parameter information for {@link QueryBuilder}.
 */
public record QueryParameter(
        ParameterType type,
        int indexWithinType, // starting with 1 as per JDBC API
        String label,
        boolean labelGuessed, // label was guessed from SQL query parsing (not directly specified by user input) // todo does it really matter?
        String propName,
        TypeMapper<?, ?> mapper, // takes precedence over mapperName,tag,jdbcType,jdbcTypeName,javaType,javaTypeClassName
        String mapperName,
        String tag,
        ParameterJdbcType<?> jdbcType, // takes precedence over jdbcTypeName
        String jdbcTypeName,
        Class<?> javaType, // takes precedence over javaTypeClassName
        String javaTypeClassName
) {

    public QueryParameter reindex(int newIndex) {
        return new QueryParameter(
                type,
                newIndex,
                label,
                labelGuessed,
                propName,
                mapper,
                mapperName,
                tag,
                jdbcType,
                jdbcTypeName,
                javaType,
                javaTypeClassName
        );
    }

}
