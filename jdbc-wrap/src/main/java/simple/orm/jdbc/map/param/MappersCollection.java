package simple.orm.jdbc.map.param;

import io.vavr.Tuple;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.sql.JDBCType;
import java.util.Objects;

/**
 * Collection of parameter mappers to search for candidates to map parameters in/out of query.
 * <br><br>
 * <b>Important:</b> MappersCollection is immutable, addition of new mappers creates a new MappersCollection.
 */
public class MappersCollection {

    private final Option<MappersCollection> underlyingMappers;
    private final Map<ParameterType<?, ?>, ParameterMapper<?, ?>> theseMappers;

    public MappersCollection() {
        this(Option.none(), HashMap.empty());
    }

    public MappersCollection(MappersCollection underlyingMappers) {
        this(Option.of(underlyingMappers), HashMap.empty());
    }

    public MappersCollection(MappersCollection underlyingMappers,
                             Map<ParameterType<?, ?>, ParameterMapper<?, ?>> mappers) {
        this(Option.of(underlyingMappers), mappers);
    }

    protected MappersCollection(
            Option<MappersCollection> underlyingMappers,
            Map<ParameterType<?, ?>, ParameterMapper<?, ?>> mappers) {
        this.underlyingMappers = underlyingMappers;
        this.theseMappers = mappers;
    }

    public MappersCollection addMapper(ParameterMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        return addMappers(mapper);
    }

    public MappersCollection addMappers(ParameterMapper<?, ?>... mappers) {
        return addMappers(Array.of(mappers));
    }

    public MappersCollection addMappers(Seq<ParameterMapper<?, ?>> mappers) {
        if (mappers.find(Objects::isNull).isDefined()) {
            throw new NullPointerException("one of mappers is null");
        }
        Seq<ParameterMapper<?, ?>> duplicates = mappers.filter(pm -> theseMappers.containsKey(pm.getType()));
        if (duplicates.nonEmpty()) {
            throw new IllegalArgumentException("this collection already contains mappers for the following types: " +
                    duplicates.map(ParameterMapper::getType).mkString(",")
            );
        }
        final Map<ParameterType<?, ?>, ParameterMapper<?, ?>> newMappers = theseMappers.merge(
                mappers.toMap(pm -> Tuple.of(pm.getType(), pm))
        );
        return new MappersCollection(underlyingMappers, newMappers);
    }

    public <T, I> Option<ParameterMapper<T, I>> findMapperFor(JDBCType jdbcType, Class<T> javaClass, Class<I> jdbcClass) {
        return findMapperFor(ParameterType.of(jdbcType, javaClass, jdbcClass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, I> Option<ParameterMapper<T, I>> findMapperFor(ParameterType<T, I> parameterType) {
        Option<ParameterMapper<?, ?>> mapper = theseMappers.get(parameterType)
                .orElse(() -> underlyingMappers.flatMap(mc -> mc.findMapperFor(parameterType)));
        return (Option<ParameterMapper<T, I>>) (Option) mapper;
    }

}
