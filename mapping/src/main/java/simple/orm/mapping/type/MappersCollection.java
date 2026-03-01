package simple.orm.mapping.type;

import io.vavr.Tuple3;
import io.vavr.collection.Traversable;
import simple.orm.mapping.impl.MappersCollectionImpl;
import simple.orm.mapping.param.ParameterJdbcType;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Immutable collection of {@link TypeMapper}s, allowing to find required mappers on the fly.
 * <br>
 * Mappers are registered under a name and with an optional tag.
 * Mappers without a tag are considered to have <code>tag==null</code>.
 * While it is expected that mappers might be uniquely identified either by name or by types they map,
 * it is not expected from the tag info.
 * Tag is expected to be used to distinguish mappers with same name (and/or same types).
 * Nevertheless, there should be exactly one mapper with same name, types and tag.
 * The collection is expected to be used in parameters injection (into {@link PreparedStatement}) and extraction
 * (from {@link ResultSet}) when no exact mapper is set, and mapper is expected to be inferred with the help of
 * information provided in a query (hence usage of names/tags to simplify referencing mappers)
 * and information obtained from available JDBC metadata.
 * <br>
 * E.g. there might be two mappers to map date into string with different format patterns.
 * They would have same mapping types. They are allowed to have same name and be registered with different tags to denote
 * the difference (the format patterns they are working with).
 * Though this is not a strict requirement, different names might be used instead.
 */
public interface MappersCollection {

    /**
     * Adds the mapper to the collection.
     *
     * @param name   mapper name.
     * @param mapper type mapper.
     * @throws IllegalArgumentException if there is a mapper with same types, name and tag already registered.
     */
    <Jdbc, Java> MappersCollection addMapper(String name, TypeMapper<Jdbc, Java> mapper);

    /**
     * Adds the mapper to the collection.
     *
     * @param name   mapper name.
     * @param mapper type mapper.
     * @param tag    mapper tag (allowed to be <code>null</code>).
     * @throws IllegalArgumentException if there is a mapper with same types, name and tag already registered.
     */
    <Jdbc, Java> MappersCollection addMapper(String name, TypeMapper<Jdbc, Java> mapper, String tag);

    /**
     * Replaces the mapper in the collection.
     *
     * @param name   mapper name.
     * @param mapper type mapper.
     * @throws IllegalArgumentException if there is no mapper with same types, name and tag already registered.
     */
    <Jdbc, Java> MappersCollection replaceMapper(String name, TypeMapper<Jdbc, Java> mapper);

    /**
     * Replaces the mapper in the collection.
     *
     * @param name   mapper name.
     * @param mapper type mapper.
     * @param tag    mapper tag (allowed to be <code>null</code>).
     * @throws IllegalArgumentException if there is no mapper with same types, name and tag already registered.
     */
    <Jdbc, Java> MappersCollection replaceMapper(String name, TypeMapper<Jdbc, Java> mapper, String tag);

    /**
     * Search collection for mappers satisfying provided criteria. If neither <code>name</code> or <code>jdbcType</code>
     * is provided {@link IllegalArgumentException} is thrown.
     * <br>
     * Note: mapper registered without a tag is considered to have <code>tag=null</code>.
     *
     * @param name     desired registration name.
     * @param jdbcType desired {@link ParameterJdbcType}.
     * @param javaType desired application java type.
     * @param tag      desired registration tag.
     * @return found mappers.
     * @throws IllegalArgumentException if both name and jdbcType are null.
     */
    Traversable<TypeMapper<?, ?>> findMappers(String name, ParameterJdbcType<?> jdbcType, Class<?> javaType, String tag);

    /**
     * Auxiliary method to search collection for mappers if {@link ParameterJdbcType} is not provided, but
     * {@link JDBCType} was obtained through JDBC API (e.g. {@link PreparedStatement} or {@link ResultSet} metadata).
     * <br>
     * Note: mapper registered without a tag is considered to have <code>tag=null</code>.
     *
     * @param jdbcType JDBC API SQL type.
     * @param name     desired registration name.
     * @param javaType desired application java type.
     * @param tag      desired registration tag.
     * @return found mappers.
     * @throws NullPointerException if jdbcType is null.
     */
    Traversable<TypeMapper<?, ?>> findMappers(JDBCType jdbcType, String name, Class<?> javaType, String tag);

    /**
     * Returns all registered mappers.
     *
     * @return all registered mappers.
     */
    Traversable<Tuple3<String, TypeMapper<?, ?>, String>> allMappers();

    /**
     * Factory method to create new empty collection.
     *
     * @return new empty collection.
     */
    static MappersCollection empty() {
        return new MappersCollectionImpl();
    }

    /**
     * Factory method to create a new collection of specified mappers.
     *
     * @param mappers mappers to be added to collection.
     * @return new collection containing specified mappers.
     */
    static MappersCollection of(Traversable<Tuple3<String, TypeMapper<?, ?>, String>> mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        return mappers.foldLeft(empty(), (coll, t3) -> coll.addMapper(t3._1, t3._2, t3._3));
    }

}
