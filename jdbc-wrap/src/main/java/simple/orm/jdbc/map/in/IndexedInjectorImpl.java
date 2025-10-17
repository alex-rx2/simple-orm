package simple.orm.jdbc.map.in;

import io.vavr.Tuple;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.param.ParameterType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Objects;
import java.util.function.Function;

/**
 * IndexedInjector implementation.
 */
public class IndexedInjectorImpl implements IndexedInjector {

    private static final Seq<ParameterSetter<?>> DEFAULT_SETTERS = Array.of(
            new ParameterSetterImpl<>(Boolean.class, PreparedStatement::setBoolean),
            new ParameterSetterImpl<>(Integer.class, PreparedStatement::setInt),
            new ParameterSetterImpl<>(Long.class, PreparedStatement::setLong),
            new ParameterSetterImpl<>(Float.class, PreparedStatement::setFloat),
            new ParameterSetterImpl<>(Double.class, PreparedStatement::setDouble),
            new ParameterSetterImpl<>(BigDecimal.class, PreparedStatement::setBigDecimal),
            new ParameterSetterImpl<>(String.class, PreparedStatement::setString),
            new ParameterSetterImpl<>(Date.class, PreparedStatement::setDate),
            new ParameterSetterImpl<>(Time.class, PreparedStatement::setTime),
            new ParameterSetterImpl<>(Timestamp.class, PreparedStatement::setTimestamp)
    );

    private static final Map<Class<?>, ParameterSetter<?>> DEFAULT_SETTERS_MAP =
            DEFAULT_SETTERS.toMap(ParameterSetter::getJdbcClass, Function.identity());

    protected Seq<ParameterType<?, ?>> types;
    protected Map<Class<?>, ParameterSetter<?>> setters;

    public IndexedInjectorImpl(ParameterType<?, ?>... types) {
        this(Array.of(types));
    }

    public IndexedInjectorImpl(Map<Class<?>, ParameterSetter<?>> setters, ParameterType<?, ?>... types) {
        this(setters, Array.of(types));
    }

    public IndexedInjectorImpl(Seq<ParameterType<?, ?>> types) {
        this(DEFAULT_SETTERS_MAP, types);
    }

    public IndexedInjectorImpl(Map<Class<?>, ParameterSetter<?>> setters, Seq<ParameterType<?, ?>> types) {
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

    public Seq<ParameterType<?, ?>> getTypes() {
        return types;
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

    protected void doInjectParameters(PreparedStatement stmt, Seq<Object> params) {
        types.zipWithIndex((pt, idx) -> Tuple.of(idx + 1, pt))
                .zipWith(params, (t2, p) -> Tuple.of(t2._1, t2._2, p))
                .forEach(t3 -> inject(stmt, t3._1, t3._2, t3._3));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void inject(PreparedStatement stmt, int index, ParameterType type, Object value) throws JdbcException {
        try {
            if (value == null) {
                stmt.setNull(index, type.getJDBCType().getVendorTypeNumber());
                return;
            }
            if (!type.getJavaTypeClass().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("for parameter no" + index + " of type " + type + " provided value class is " + value.getClass());
            }
            Object jdbcValue = type.fromJava(value);
            ParameterSetter setter = (ParameterSetter) setters.get(type.getJavaTypeClass());
            if (setter == null) {
                stmt.setObject(index, jdbcValue, type.getJDBCType().getVendorTypeNumber());
            } else {
                setter.inject(stmt, index, value);
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
