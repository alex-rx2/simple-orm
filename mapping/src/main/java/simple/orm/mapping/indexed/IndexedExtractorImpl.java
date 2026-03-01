package simple.orm.mapping.indexed;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.type.TypeMapper;

import java.sql.ResultSet;
import java.util.Objects;


/**
 * {@link IndexedExtractor} implementation.
 */
public class IndexedExtractorImpl implements IndexedExtractor {

    protected final MappersFinder mappersFinder;
    protected final Seq<IndexedParameter> parameters;

    public IndexedExtractorImpl(MappersFinder mappersFinder, Seq<IndexedParameter> parameters) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        if (parameters == null) {
            throw new NullPointerException("parameters is null");
        }
        if (parameters.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("parameters contains nulls");
        }
        this.mappersFinder = mappersFinder;
        this.parameters = parameters;
    }

    @Override
    public Seq<Object> extractRow(ResultSet rs) {
        if (rs == null) {
            throw new NullPointerException("rs is null");
        }
        return doExtractRow(rs);
    }

    protected Seq<Object> doExtractRow(ResultSet rs) {
        return parameters
                .map(ip -> doExtract(rs, ip))
                .toArray() // for faster index based access
                ;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object doExtract(ResultSet rs, IndexedParameter param) {
        final int index = param.index;
        final TypeMapper mapper = param.mapper != null ?
                param.mapper :
                mappersFinder.findMapper(index, param.info, rs);
        final ParameterGetter getter = mapper.getJdbcType().getGetter();
        final Object jdbcValue = getter.getValue(rs, index);
        return mapper.jdbcToJava(jdbcValue);
    }

}
