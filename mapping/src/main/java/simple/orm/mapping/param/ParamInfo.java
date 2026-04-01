package simple.orm.mapping.param;

import simple.orm.mapping.impl.BasicParamInfo;
import simple.orm.mapping.type.TypeMapper;

/**
 * Information externally provided on query parameters (or result set columns).
 * This information is used to find {@link TypeMapper} to perform conversion
 * of value from JDBC API type to application type (and vice versa).
 * <br>
 * Note: object of this class should properly implement
 * {@link Object#hashCode()}, {@link Object#equals(Object)} and {@link Object#toString()}.
 */
// TODO move tag after mapper name everywhere
public interface ParamInfo<Jdbc, Java> {

    /**
     * Provided type mapper name (if any).
     *
     * @return type mapper name.
     */
    String getMapperName();

    /**
     * Provided JDBC API type {@link ParameterJdbcType}.
     *
     * @return type used in JDBC API.
     */
    ParameterJdbcType<Jdbc> getJdbcType();

    /**
     * Provided java application type.
     *
     * @return type used in java application.
     */
    Class<Java> getJavaType();

    /**
     * Provided type mapper tag name.
     *
     * @return type mapper tag name.
     */
    String getMapperTag();

    /**
     * Factory method to create empty {@link ParamInfo} (no information provided).
     *
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> none() {
        return new BasicParamInfo<>(null, null, null, null);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param name mapper name.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(String name) {
        return new BasicParamInfo<>(name, null, null, null);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param name mapper name.
     * @param tag  mapper tag.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(String name, String tag) {
        return new BasicParamInfo<>(name, null, null, tag);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param jdbcType JDBC API type.
     * @param javaType application java type.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(ParameterJdbcType<Jdbc> jdbcType, Class<Java> javaType) {
        return new BasicParamInfo<>(null, jdbcType, javaType, null);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param jdbcType JDBC API type.
     * @param javaType application java type.
     * @param tag      mapper tag.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(ParameterJdbcType<Jdbc> jdbcType, Class<Java> javaType, String tag) {
        return new BasicParamInfo<>(null, jdbcType, javaType, tag);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param name     mapper name.
     * @param javaType application java type.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(String name, Class<Java> javaType) {
        return new BasicParamInfo<>(name, null, javaType, null);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param name     mapper name.
     * @param javaType application java type.
     * @param tag      mapper tag.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(String name, Class<Java> javaType, String tag) {
        return new BasicParamInfo<>(name, null, javaType, tag);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param javaType application java type.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(Class<Java> javaType) {
        return new BasicParamInfo<>(null, null, javaType, null);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param javaType application java type.
     * @param tag      mapper tag.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(Class<Java> javaType, String tag) {
        return new BasicParamInfo<>(null, null, javaType, tag);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param name     mapper name.
     * @param jdbcType JDBC API type.
     * @param javaType application java type.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(String name, ParameterJdbcType<Jdbc> jdbcType, Class<Java> javaType) {
        return new BasicParamInfo<>(name, jdbcType, javaType, null);
    }

    /**
     * Factory method to create {@link ParamInfo} with information provided.
     *
     * @param name     mapper name.
     * @param jdbcType JDBC API type.
     * @param javaType application java type.
     * @param tag      mapper tag.
     * @return new {@link ParamInfo}.
     */
    static <Jdbc, Java> ParamInfo<Jdbc, Java> of(String name, ParameterJdbcType<Jdbc> jdbcType, Class<Java> javaType, String tag) {
        return new BasicParamInfo<>(name, jdbcType, javaType, tag);
    }

}
