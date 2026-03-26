package simple.orm.mapping.indexed;

import io.vavr.Tuple;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.param.ParameterSetter;
import simple.orm.mapping.type.TypeMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link IndexedInjector} implementation.
 */
public class IndexedInjectorImpl implements IndexedInjector {

    protected final MappersFinder mappersFinder;
    protected final Seq<IndexedParameter> parameters;

    public IndexedInjectorImpl(MappersFinder mappersFinder,
                               Seq<IndexedParameter> parameters) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        if (parameters == null) {
            throw new NullPointerException("parameters is null");
        }
        if (parameters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("parameters contains nulls");
        }
        if (parameters.find(p -> p.index == null).isDefined()) {
            throw new IllegalArgumentException("all parameters must have index for injection");
        }
        this.mappersFinder = mappersFinder;
        this.parameters = parameters;
    }

    @Override
    public void injectParameters(PreparedStatement stmt, Object... values) {
        injectParameters(stmt, Array.of(values));
    }

    @Override
    public void injectParameters(PreparedStatement stmt, Seq<Object> values) {
        if (stmt == null) {
            throw new NullPointerException("stmt is null");
        }
        if (values == null) {
            throw new NullPointerException("values is null");
        }
        if (parameters.size() != values.size()) {
            throw new IllegalArgumentException(parameters.size() + " parameter(s) expected, but " + values.size() + " provided");
        }
        doInjectParameters(stmt, values);
    }

    protected void doInjectParameters(PreparedStatement stmt, Seq<Object> params) {
        parameters.zipWith(params, Tuple::of)
                .forEach(t2 -> doInject(stmt, t2._1, t2._2));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doInject(PreparedStatement stmt, IndexedParameter param, Object value) {
        final int index = param.index;
        TypeMapper mapper = param.mapper;
        if (value == null && mapper == null) {
            // "manual" null insertion
            final Integer sqlType = mappersFinder.findSQLType(index, param.info, stmt);
            try {
                if (sqlType != null) {
                    stmt.setNull(index, sqlType);
                } else {
                    stmt.setObject(index, null);
                }
            } catch (SQLException e) {
                throw new JdbcException(e);
            }
        } else {
            if (mapper == null) {
                mapper = mappersFinder.findMapper(index, param.info, value.getClass(), stmt);
            }
            final Object jdbcValue = mapper.javaToJdbc(value);
            final ParameterSetter setter = mapper.getJdbcType().getSetter();
            setter.setValue(stmt, index, jdbcValue);
        }
    }

}
