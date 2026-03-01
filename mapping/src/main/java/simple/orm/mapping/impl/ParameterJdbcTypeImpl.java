package simple.orm.mapping.impl;

import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetter;

import java.sql.JDBCType;
import java.util.Objects;

/**
 * {@link ParameterJdbcType} implementation.
 */
public final class ParameterJdbcTypeImpl<Jdbc> implements ParameterJdbcType<Jdbc> {

    private final JDBCType jdbcType;
    private final Class<Jdbc> jdbcClass;
    private final ParameterGetter<Jdbc> getter;
    private final ParameterSetter<Jdbc> setter;

    public ParameterJdbcTypeImpl(JDBCType jdbcType,
                                 Class<Jdbc> jdbcClass,
                                 ParameterGetter<Jdbc> getter,
                                 ParameterSetter<Jdbc> setter
    ) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        if (jdbcClass == null) {
            throw new NullPointerException("jdbcClass is null");
        }
        if (getter == null) {
            throw new NullPointerException("getter is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        this.jdbcType = jdbcType;
        this.jdbcClass = jdbcClass;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public JDBCType getJDBCType() {
        return jdbcType;
    }

    @Override
    public Class<Jdbc> getJDBCTypeClass() {
        return jdbcClass;
    }

    @Override
    public ParameterGetter<Jdbc> getGetter() {
        return getter;
    }

    @Override
    public ParameterSetter<Jdbc> getSetter() {
        return setter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ParameterJdbcType<?> that))
            return false;
        return jdbcType == that.getJDBCType()
                && jdbcClass == that.getJDBCTypeClass()
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jdbcType, jdbcClass);
    }

    @Override
    public String toString() {
        return "JdbcType(" + jdbcType + "->" + jdbcClass.getName() + ")";
    }

}
