package simple.orm.mapping.impl;

import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetter;

import java.sql.SQLType;
import java.util.Objects;

/**
 * {@link ParameterJdbcType} implementation.
 */
public final class ParameterJdbcTypeImpl<Jdbc> implements ParameterJdbcType<Jdbc> {

    private final SQLType sqlType;
    private final Class<Jdbc> jdbcClass;
    private final ParameterGetter<Jdbc> getter;
    private final ParameterSetter<Jdbc> setter;

    public ParameterJdbcTypeImpl(SQLType sqlType,
                                 Class<Jdbc> jdbcClass,
                                 ParameterGetter<Jdbc> getter,
                                 ParameterSetter<Jdbc> setter
    ) {
        if (sqlType == null) {
            throw new NullPointerException("sqlType is null");
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
        this.sqlType = sqlType;
        this.jdbcClass = jdbcClass;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public SQLType getSQLType() {
        return sqlType;
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
        return sqlType == that.getSQLType()
                && jdbcClass == that.getJDBCTypeClass()
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlType, jdbcClass);
    }

    @Override
    public String toString() {
        return "JdbcType(" + sqlType + "->" + jdbcClass.getName() + ")";
    }

}
