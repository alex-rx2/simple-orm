package simple.orm.mapping.impl;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import io.vavr.collection.TreeMap;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;

import java.sql.SQLType;
import java.util.Comparator;
import java.util.Objects;

/**
 * Implementation of {@link TypesCollection}.
 */
public class TypesCollectionImpl implements TypesCollection {

    private final boolean caseSensitive;
    private final Map<String, ParameterJdbcType<?>> types;

    public TypesCollectionImpl(boolean caseSensitive) {
        this(caseSensitive, HashMap.empty());
    }

    public TypesCollectionImpl(boolean caseSensitive, Map<String, ParameterJdbcType<?>> types) {
        this.caseSensitive = caseSensitive;
        this.types = TreeMap.<String, ParameterJdbcType<?>>empty(comparator(caseSensitive)).merge(types);
        if (this.types.size() != types.size()) {
            throw new IllegalArgumentException("types contains duplicate keys (caseSensitive=" + caseSensitive + ")");
        }
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
        return new TypesCollectionImpl(caseSensitive, types.put(name, type));
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
        return new TypesCollectionImpl(caseSensitive, types.put(name, type));
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
    public Traversable<Tuple2<String, ParameterJdbcType<?>>> findTypes(SQLType sqlType) {
        if (sqlType == null) {
            throw new NullPointerException("sqlType is null");
        }
        return types.filter(t2 -> Objects.equals(t2._2.getSQLType(), sqlType)); // todo compare only getVendorTypeNumber() ???
    }

    @Override
    public Traversable<Tuple2<String, ParameterJdbcType<?>>> allTypes() {
        return types.iterator();
    }

    @Override
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public TypesCollection caseSensitive() {
        return caseSensitive ? this : new TypesCollectionImpl(true, types);
    }

    @Override
    public TypesCollection caseInsensitive() {
        return caseSensitive ? new TypesCollectionImpl(false, types) : this;
    }

    private static Comparator<String> comparator(boolean caseSensitive) {
        return caseSensitive ? String::compareTo : String.CASE_INSENSITIVE_ORDER;
    }

}
