package simple.orm.jdbc.common.map;

import io.vavr.Tuple;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.ParameterGetter;
import simple.orm.jdbc.param.ParameterJdbcType;
import simple.orm.jdbc.param.ParameterType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link IndexedExtractor} implementation.
 */
@Deprecated
public class IndexedExtractorImpl implements IndexedExtractor {

    protected final Seq<ParameterType<?, ?>> types;
    protected final Map<ParameterJdbcType<?>, ParameterGetter<?>> getters;

    public IndexedExtractorImpl(Map<ParameterJdbcType<?>, ParameterGetter<?>> getters, Seq<ParameterType<?, ?>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        if (types.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("types contains nulls");
        }
        if (getters == null) {
            throw new NullPointerException("getters is null");
        }
        this.types = types;
        this.getters = getters;
    }

    @Override
    public Seq<Object> extractRow(ResultSet rs) {
        if (rs == null) {
            throw new NullPointerException("rs is null");
        }
        return doExtractRow(rs);
    }

    protected Seq<Object> doExtractRow(ResultSet rs) {
        return types.zipWithIndex((t, i) -> Tuple.of(t, i + 1))
                .map(t2 -> extract(rs, t2._2, t2._1));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object extract(ResultSet rs, int index, ParameterType type) {
        try {
            ParameterGetter<?> getter = getters.get(type.getParameterJdbcType()).getOrNull();
            Object jdbcValue;
            if (getter == null) {
                jdbcValue = rs.getObject(index, type.getJDBCTypeClass());
            } else {
                jdbcValue = getter.getValue(rs, index);
            }
            return type.fromJDBC(jdbcValue);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

}
