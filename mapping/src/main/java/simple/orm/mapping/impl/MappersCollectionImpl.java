package simple.orm.mapping.impl;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.TypeMapper;

import java.sql.JDBCType;
import java.util.Objects;

import static simple.orm.mapping.impl.MappersCollectionUtil.*;

/**
 * Implementation of {@link MappersCollection}.
 */
public class MappersCollectionImpl implements MappersCollection {

    private final Map<String, List<TypeMapperReg>> byName;
    private final Map<ParameterJdbcType<?>, List<TypeMapperReg>> byType;

    public MappersCollectionImpl() {
        this(HashMap.empty(), HashMap.empty());
    }

    public MappersCollectionImpl(Map<String, List<TypeMapperReg>> byName,
                                 Map<ParameterJdbcType<?>, List<TypeMapperReg>> byType
    ) {
        this.byName = byName;
        this.byType = byType;
    }

    @Override
    public <Jdbc, Java> MappersCollection addMapper(String name, TypeMapper<Jdbc, Java> mapper) {
        return addMapper(name, mapper, null);
    }

    @Override
    public <Jdbc, Java> MappersCollection addMapper(String name, TypeMapper<Jdbc, Java> mapper, String tag) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        if (name == null) {
            throw new NullPointerException("mapper name is null");
        }
        if (mapper.getJdbcType() == null) {
            throw new NullPointerException("mapper jdbcType is null");
        }
        return addMapper(new TypeMapperReg(name, mapper, tag));
    }

    private MappersCollection addMapper(TypeMapperReg mapper) {
        if (byName.get(mapper.name())
                .map(types -> types.exists(mappersEqualPredicate(mapper)))
                .getOrElse(Boolean.FALSE)) {
            throw new IllegalArgumentException("same mapper is already registered");
        }
        return new MappersCollectionImpl(addByName(byName, mapper), addByType(byType, mapper));
    }

    @Override
    public <Jdbc, Java> MappersCollection replaceMapper(String name, TypeMapper<Jdbc, Java> mapper) {
        return replaceMapper(name, mapper, null);
    }

    @Override
    public <Jdbc, Java> MappersCollection replaceMapper(String name, TypeMapper<Jdbc, Java> mapper, String tag) {
        if (mapper == null) {
            throw new NullPointerException("mapper is null");
        }
        if (name == null) {
            throw new NullPointerException("mapper name is null");
        }
        if (mapper.getJdbcType() == null) {
            throw new NullPointerException("mapper jdbcType is null");
        }
        return replaceMapper(new TypeMapperReg(name, mapper, tag));
    }

    private MappersCollection replaceMapper(TypeMapperReg mapper) {
        if (!byName.get(mapper.name())
                .map(types -> types.exists(mappersEqualPredicate(mapper)))
                .getOrElse(Boolean.FALSE)) {
            throw new IllegalArgumentException("nothing to replace, no same mapper registered");
        }
        return new MappersCollectionImpl(replaceByName(byName, mapper), replaceByType(byType, mapper));
    }

    @Override
    public Traversable<TypeMapper<?, ?>> findMappers(String name,
                                                     ParameterJdbcType<?> jdbcType,
                                                     Class<?> javaType,
                                                     String tag
    ) {
        if (name != null) {
            return byName.getOrElse(name, List.empty())
                    .filter(reg -> matches(reg, jdbcType, javaType, tag))
                    .map(TypeMapperReg::mapper);
        } else if (jdbcType != null) {
            return byType.getOrElse(jdbcType, List.empty())
                    .filter(reg -> matches(reg, name, javaType, tag))
                    .map(TypeMapperReg::mapper);
        } else {
            throw new IllegalArgumentException("no name or jdbcType provided");
        }
    }

    private boolean matches(TypeMapperReg reg, ParameterJdbcType<?> jdbcType, Class<?> javaType, String tag) {
        return (jdbcType == null || Objects.equals(jdbcType, reg.jdbcType()))
                && (javaType == null || Objects.equals(javaType, reg.javaType()))
                && Objects.equals(tag, reg.tag());
    }

    private boolean matches(TypeMapperReg reg, String name, Class<?> javaType, String tag) {
        return (name == null || Objects.equals(name, reg.name()))
                && (javaType == null || Objects.equals(javaType, reg.javaType()))
                && Objects.equals(tag, reg.tag());
    }

    @Override
    public Traversable<TypeMapper<?, ?>> findMappers(JDBCType jdbcType,
                                                     String name,
                                                     Class<?> javaType,
                                                     String tag
    ) {
        if (jdbcType != null) {
            return byType.keySet()
                    .filter(type -> type.getJDBCType() == jdbcType)
                    .flatMap(type -> findMappers(name, type, javaType, tag))
                    .toList();
        } else {
            throw new NullPointerException("jdbcType is null");
        }
    }

    @Override
    public Traversable<Tuple3<String, TypeMapper<?, ?>, String>> allMappers() {
        return byName.foldLeft(List.empty(), (list, t2) -> list.appendAll(t2._2.map(this::tupelize)));
    }

    private Tuple3<String, TypeMapper<?, ?>, String> tupelize(TypeMapperReg reg) {
        return Tuple.of(reg.name(), reg.mapper(), reg.tag());
    }

}
