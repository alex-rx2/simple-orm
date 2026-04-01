package simple.orm.mapping.impl;

import io.vavr.collection.Array;
import io.vavr.control.Option;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;

import java.util.Objects;

import static simple.orm.util.StringUtils.qnn;

/**
 * Implementation of {@link ParamInfo}.
 */
public class BasicParamInfo<Jdbc, Java> implements ParamInfo<Jdbc, Java> {

    private final String mapperName;
    private final String mapperTag;
    private final ParameterJdbcType<Jdbc> jdbcType;
    private final Class<Java> javaType;

    private Option<Integer> hashCode = Option.none();

    public BasicParamInfo(String mapperName,
                          String mapperTag,
                          ParameterJdbcType<Jdbc> jdbcType,
                          Class<Java> javaType
    ) {
        this.mapperName = mapperName;
        this.mapperTag = mapperTag;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
    }

    @Override
    public String getMapperName() {
        return mapperName;
    }

    @Override
    public String getMapperTag() {
        return mapperTag;
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
                        qnn(mapperName),
                        qnn(mapperTag),
                        qnn(jdbcType == null ? null : jdbcType.toString()),
                        qnn(javaType == null ? null : javaType.getName())
                )
                .mkString("ParamInfo(", ",", ")");
    }

}
