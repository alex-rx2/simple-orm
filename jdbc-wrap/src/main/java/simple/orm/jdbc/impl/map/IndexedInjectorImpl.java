package simple.orm.jdbc.impl.map;

import io.vavr.Tuple;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.ParameterSetter;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link IndexedInjector} implementation.
 */
public class IndexedInjectorImpl implements IndexedInjector {

    private final Seq<ParameterType<?, ?>> types;
    private final Map<ParameterJdbcType<?>, ParameterSetter<?>> setters;

    public IndexedInjectorImpl(Seq<ParameterType<?, ?>> types) {
        this(ParameterSetterImpl.DEFAULT_SETTERS_MAP, types);
    }

    public IndexedInjectorImpl(Map<ParameterJdbcType<?>, ParameterSetter<?>> setters, Seq<ParameterType<?, ?>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        if (setters == null) {
            throw new NullPointerException("setters is null");
        }
        this.types = types;
        this.setters = setters;
    }

    @Override
    public void injectParameters(PreparedStatement stmt, Object... params) {
        injectParameters(stmt, Array.of(params));
    }

    @Override
    public void injectParameters(PreparedStatement stmt, Seq<Object> params) {
        if (stmt == null) {
            throw new NullPointerException("stmt is null");
        }
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        if (types.size() != params.size()) {
            throw new IllegalArgumentException(types.size() + " parameter(s) expected, but " + params.size() + " provided");
        }
        doInjectParameters(stmt, params);
    }

    private void doInjectParameters(PreparedStatement stmt, Seq<Object> params) {
        types.zipWithIndex((pt, idx) -> Tuple.of(idx + 1, pt))
                .zipWith(params, (t2, p) -> Tuple.of(t2._1, t2._2, p))
                .forEach(t3 -> inject(stmt, t3._1, t3._2, t3._3));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void inject(PreparedStatement stmt, int index, ParameterType type, Object value) throws JdbcException {
        try {
            if (value == null) {
                stmt.setNull(index, type.getJDBCType().getVendorTypeNumber());
                return;
            }
            if (!type.getJavaTypeClass().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("for parameter no" + index + " of type " + type + " provided value of incompatible class " + value.getClass());
            }
            Object jdbcValue = type.fromJava(value);
            ParameterSetter setter = setters.get(type.getParameterJdbcType()).getOrNull();
            if (setter == null) {
                stmt.setObject(index, jdbcValue, type.getJDBCType().getVendorTypeNumber());
            } else {
                setter.setValue(stmt, index, jdbcValue);
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
