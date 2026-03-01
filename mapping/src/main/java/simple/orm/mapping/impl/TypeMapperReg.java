package simple.orm.mapping.impl;

import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.TypeMapper;

/**
 * TypeMapper registration record in MappersCollectionImpl (the actual object it internally works with).
 */
public record TypeMapperReg(
        String name,
        TypeMapper<?, ?> mapper,
        String tag
) {

    public ParameterJdbcType<?> jdbcType() {
        return mapper.getJdbcType();
    }

    public Class<?> javaType() {
        return mapper.getJavaType();
    }

}
