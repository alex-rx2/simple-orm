package simple.orm.mapping.type;

import io.vavr.Function1;
import simple.orm.mapping.param.ParameterJdbcType;

/**
 * {@link TypeMapper} for cases when same java class is used to represent data both in JDBC API and java application.
 */
public class AsIsTypeMapper<T> extends SimpleTypeMapper<T, T> {

    public AsIsTypeMapper(ParameterJdbcType<T> jdbcType) {
        super(jdbcType, jdbcType.getJDBCTypeClass(), Function1.identity(), Function1.identity());
    }

}
