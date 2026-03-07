package simple.orm.mapping.param;

import io.vavr.Tuple2;
import io.vavr.collection.Traversable;
import simple.orm.mapping.impl.TypesCollectionImpl;

import java.sql.JDBCType;

/**
 * Immutable collection of named {@link ParameterJdbcType}s, allowing to use them by name.
 */
public interface TypesCollection {

    /**
     * Adds named type to the collection. Name should be unique.
     *
     * @param name type name.
     * @param type the type.
     * @throws IllegalArgumentException if there is a type already registered with this name.
     */
    <Jdbc> TypesCollection addType(String name, ParameterJdbcType<Jdbc> type);

    /**
     * Replaces registered type in the collection with another one.
     *
     * @param name type name.
     * @param type the type.
     * @throws IllegalArgumentException if there is no type registered with this name (hence nothing to replace).
     */
    <Jdbc> TypesCollection replaceType(String name, ParameterJdbcType<Jdbc> type);

    /**
     * Find type by name.
     *
     * @param name type name.
     * @return the type registered with this name or <code>null</code> if no such type found.
     */
    <Jdbc> ParameterJdbcType<Jdbc> findType(String name);

    /**
     * Find types by JDBC SQL type.
     *
     * @param jdbcType JDBC SQL type.
     * @return the type registered with this name or <code>null</code> if no such type found.
     */
    Traversable<Tuple2<String, ParameterJdbcType<?>>> findTypes(JDBCType jdbcType);

    /**
     * Returns all the registered types with their respective names.
     *
     * @return all registered type with their names.
     */
    Traversable<Tuple2<String, ParameterJdbcType<?>>> allTypes();

    /**
     * Returns <code>true</code> if name comparison is performed in a case-sensitive way.
     * <br>
     * E.g. if type is registered under "SomeName" name, it can be found only under this exact name, and not "somename".
     *
     * @return <code>true</code> if name comparison is performed in a case-sensitive way.
     */
    boolean isCaseSensitive();

    /**
     * Returns a case-sensitive variant of this collection.
     * <br>
     * E.g. if type is registered under "SomeName" name, it can be found only under this exact name, and not "somename".
     *
     * @return a case-sensitive variant of this collection (or self if already case-sensitive).
     */
    TypesCollection caseSensitive();

    /**
     * Returns a case-insensitive variant of this collection.
     * <br>
     * E.g. if type is registered under "SomeName" name,
     * it can also be found under "somename" or "SOMENAME" names (and all other variants).
     *
     * @return a case-insensitive variant of this collection (or self if already case-insensitive).
     */
    TypesCollection caseInsensitive();

    /**
     * Factory method to create new empty collection (it will be case-sensitive).
     *
     * @return new empty collection.
     */
    static TypesCollection empty() {
        return new TypesCollectionImpl(true);
    }

    /**
     * Factory method to create a new collection of specified types (it will be case-sensitive).
     *
     * @param types types to be added to collection.
     * @return new collection containing specified types.
     */
    static TypesCollection of(Traversable<Tuple2<String, ParameterJdbcType<?>>> types) {
        if (types == null) {
            throw new NullPointerException("types is null");
        }
        return types.foldLeft(empty(), (coll, t2) -> coll.addType(t2._1, t2._2));
    }

}
