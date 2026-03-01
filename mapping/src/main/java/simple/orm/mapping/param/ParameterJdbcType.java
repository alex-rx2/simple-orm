package simple.orm.mapping.param;

import io.vavr.Function1;
import simple.orm.mapping.impl.ParameterJdbcTypeImpl;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.sql.JDBCType;

/**
 * Type metadata for JDBC (query or result) parameter.
 * <br>
 * Note: {@link ParameterJdbcType} should be considered equal based on {@link #getJDBCType()}
 * and {@link #getJDBCTypeClass()} alone. Getter and setter should not play any role.
 *
 * @param <Jdbc> class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
 */
public interface ParameterJdbcType<Jdbc> {

    /**
     * Returns {@link JDBCType} type of parameter.
     *
     * @return {@link JDBCType} type of parameter.
     */
    JDBCType getJDBCType();

    /**
     * Returns class implementing parameter value in JDBC (used to inject parameters or extracting it from result set).
     *
     * @return class implementing parameter value in JDBC.
     */
    Class<Jdbc> getJDBCTypeClass();

    /**
     * Returns the {@link ParameterGetter} for this type.
     *
     * @return {@link ParameterGetter} for this type.
     */
    ParameterGetter<Jdbc> getGetter();

    /**
     * Returns the {@link ParameterSetter} for this type.
     *
     * @return {@link ParameterSetter} for this type.
     */
    ParameterSetter<Jdbc> getSetter();

    /**
     * Factory method to spawn simple mappers.
     *
     * @param javaClass java class used in java application.
     * @param toJdbc    mapper from application class to JDBC API class.
     * @param toJava    mapper from JDBC API class to application class.
     * @param <Java>    java class used in java application.
     * @return new {@link TypeMapper}.
     */
    default <Java> TypeMapper<Jdbc, Java> mappedTo(Class<Java> javaClass,
                                                   Function1<Java, Jdbc> toJdbc,
                                                   Function1<Jdbc, Java> toJava
    ) {
        return new SimpleTypeMapper<>(this, javaClass, toJdbc, toJava);
    }

    /**
     * Factory method to spawn a trivial mapper, mapping this type to application type as is
     * (meaning application is using same {@link #getJDBCTypeClass()} to represent this parameter).
     *
     * @return new trivial {@link TypeMapper}.
     */
    default TypeMapper<Jdbc, Jdbc> trivialMapper() {
        return new SimpleTypeMapper<>(this, getJDBCTypeClass(), Function1.identity(), Function1.identity());
    }

    /**
     * Factory method to create {@link ParameterJdbcType}.
     */
    static <Jdbc> ParameterJdbcType<Jdbc> of(JDBCType jdbcType,
                                             Class<Jdbc> jdbcClass,
                                             ParameterGetter<Jdbc> getter,
                                             ParameterSetter<Jdbc> setter
    ) {
        return new ParameterJdbcTypeImpl<>(jdbcType, jdbcClass, getter, setter);
    }

}
