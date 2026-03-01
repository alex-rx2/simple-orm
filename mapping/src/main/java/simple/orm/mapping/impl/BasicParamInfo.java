package simple.orm.mapping.impl;

import io.vavr.collection.Array;
import io.vavr.control.Option;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;

import java.util.Objects;

/**
 * Implementation of {@link ParamInfo}.
 */
public class BasicParamInfo<Jdbc, Java> implements ParamInfo<Jdbc, Java> {

    private final String mapperName;
    private final ParameterJdbcType<Jdbc> jdbcType;
    private final Class<Java> javaType;
    private final String mapperTag;

    private Option<Integer> hashCode = Option.none();

    public BasicParamInfo(String mapperName,
                          ParameterJdbcType<Jdbc> jdbcType,
                          Class<Java> javaType,
                          String mapperTag
    ) {
        this.mapperName = mapperName;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
        this.mapperTag = mapperTag;
    }

    @Override
    public String getMapperName() {
        return mapperName;
    }

    @Override
    public ParameterJdbcType<Jdbc> getJdbcType() {
        return jdbcType;
    }

    @Override
    public Class<Java> getJavaType() {
        return javaType;
    }

    @Override
    public String getMapperTag() {
        return mapperTag;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParamInfo<?, ?> pi
                && Objects.equals(mapperName, pi.getMapperName())
                && Objects.equals(jdbcType, pi.getJdbcType())
                && Objects.equals(javaType, pi.getJavaType())
                && Objects.equals(mapperTag, pi.getMapperTag())
                ;
    }

    @Override
    public int hashCode() {
        if (!hashCode.isDefined()) {
            hashCode = Option.of(Objects.hash(mapperName, jdbcType, javaType, mapperTag));
        }
        return hashCode.get();
    }

    @Override
    public String toString() {
        return Array.of(
                        mapperName == null ? "" : mapperName,
                        jdbcType == null ? "" : jdbcType.toString(),
                        javaType == null ? "" : javaType.getName(),
                        mapperTag == null ? "" : mapperTag
                )
                .mkString("ParamInfo(", ",", ")");
    }

}
