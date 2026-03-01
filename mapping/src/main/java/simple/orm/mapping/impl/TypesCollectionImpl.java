package simple.orm.mapping.impl;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import simple.orm.mapping.param.TypesCollection;
import simple.orm.mapping.param.ParameterJdbcType;

import java.sql.JDBCType;

/**
 * Implementation of {@link TypesCollection}.
 */
public class TypesCollectionImpl implements TypesCollection {

    private final Map<String, ParameterJdbcType<?>> types;

    public TypesCollectionImpl() {
        this(HashMap.empty());
    }

    public TypesCollectionImpl(Map<String, ParameterJdbcType<?>> types) {
        this.types = types;
    }

    @Override
    public <Jdbc> TypesCollection addType(String name, ParameterJdbcType<Jdbc> type) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (types.containsKey(name)) {
            throw new IllegalArgumentException("type with the name '" + name + "' already exists");
        }
        return new TypesCollectionImpl(types.put(name, type));
    }

    @Override
    public <Jdbc> TypesCollection replaceType(String name, ParameterJdbcType<Jdbc> type) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (!types.containsKey(name)) {
            throw new IllegalArgumentException("type with the name '" + name + "' doesn't exist");
        }
        return new TypesCollectionImpl(types.put(name, type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Jdbc> ParameterJdbcType<Jdbc> findType(String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return (ParameterJdbcType<Jdbc>) types.getOrElse(name, null);
    }

    @Override
    public Traversable<Tuple2<String, ParameterJdbcType<?>>> findTypes(JDBCType jdbcType) {
        if (jdbcType == null) {
            throw new NullPointerException("jdbcType is null");
        }
        return types.filter(t2 -> t2._2.getJDBCType() == jdbcType);
    }

    @Override
    public Traversable<Tuple2<String, ParameterJdbcType<?>>> allTypes() {
        return types.iterator();
    }

}
