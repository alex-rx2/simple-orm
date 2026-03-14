package simple.orm.mapping.impl.cache;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import simple.orm.mapping.ManyMappersFoundException;
import simple.orm.mapping.NoMapperFoundException;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.util.Mutable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;

import static simple.orm.util.RefUtils.isAssignableFrom;
import static simple.orm.util.StringUtils.qnn;

/**
 * {@link MappersFinder} implementation with internal caching mechanism.
 */
public class FoundMappersCache implements MappersFinder {

    // helper class to represent Either<column index, column label>
    // to make code more verbose (not overfilled with tons of no-sense Either<Integer,String>, isleft(), isRight() etc.)
    private static class Designator {

        public final int index;
        public final String label;

        private Option<Integer> hashCode = Option.none();

        private Designator(int index, String label) {
            this.index = index;
            this.label = label;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Designator d
                    && index == d.index
                    && Objects.equals(label, d.label)
                    ;
        }

        @Override
        public int hashCode() {
            if (!hashCode.isDefined()) {
                hashCode = Option.of(Objects.hash(index, label));
            }
            return hashCode.get();
        }

        @Override
        public String toString() {
            if (label == null) {
                return "Designator(" + index + ")";
            } else {
                return "Designator(" + qnn(label) + ')';
            }
        }

        public String toShortString() {
            if (label == null) {
                return "#" + index;
            } else {
                return "#" + qnn(label);
            }
        }

    }

    // todo make private
    static Designator designator(int index) {
        return new Designator(index, null);
    }

    // todo make private
    static Designator designator(String label) {
        return new Designator(-1, label);
    }

    private final MappersCollection mappers;
    private final Mutable<Map<Designator, Map<Class<?>, TypeMapper<?, ?>>>> cachedMappers;
    private final Mutable<Map<Designator, Integer>> cachedSQLTypes;
    private final Mutable<Map<String, Integer>> columnIndexes;

    public FoundMappersCache(MappersCollection mappers) {
        this.mappers = mappers;
        this.cachedMappers = Mutable.of(HashMap.empty());
        this.cachedSQLTypes = Mutable.of(HashMap.empty());
        this.columnIndexes = Mutable.of(HashMap.empty());
    }

    @Override
    public TypeMapper<?, ?> findMapper(int columnIndex,
                                       ParamInfo<?, ?> param,
                                       Class<?> valueClass,
                                       PreparedStatement stmt
    ) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        if (stmt == null) {
            throw new NullPointerException("stmt is null");
        }
        return findMapper(designator(columnIndex), param, valueClass, stmt, null);
    }

    @Override
    public TypeMapper<?, ?> findMapper(int columnIndex,
                                       ParamInfo<?, ?> param,
                                       ResultSet rs
    ) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        if (rs == null) {
            throw new NullPointerException("rs is null");
        }
        return findMapper(designator(columnIndex), param, null, null, rs);
    }

    @Override
    public TypeMapper<?, ?> findMapper(String columnLabel,
                                       ParamInfo<?, ?> param,
                                       ResultSet rs
    ) {
        if (columnLabel == null) {
            throw new NullPointerException("columnLabel is null");
        }
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        if (rs == null) {
            throw new NullPointerException("rs is null");
        }
        return findMapper(designator(columnLabel), param, null, null, rs);
    }

    // todo make private? will have to rewrite a lot of tests
    TypeMapper<?, ?> findMapper(Designator designator,
                                ParamInfo<?, ?> param,
                                Class<?> valueClass,
                                PreparedStatement stmt,
                                ResultSet rs
    ) {
        // provided info
        String mapperName = param.getMapperName();
        ParameterJdbcType<?> jdbcType = param.getJdbcType();
        Class<?> javaType = param.getJavaType();
        String mapperTag = param.getMapperTag();
        // merge javaType and valueClass
        if (javaType == null) {
            javaType = valueClass;
        } else if (valueClass != null) {
            if (!isAssignableFrom(javaType, valueClass)) {
                throw new IllegalArgumentException("provided value class " + valueClass.getName() +
                        " for parameter " + designator.toShortString() +
                        " is incompatible with java class specified in parameter description " + param);
            } else {
                // valueClass is either same, either more specific
                javaType = valueClass;
            }
        }
        // check cache
        TypeMapper<?, ?> mapper = checkMapperCache(designator, javaType);
        boolean cacheHit = mapper != null;
        // if jdbcType not provided - try to use metadata
        if (mapper == null && jdbcType == null && (javaType != null || mapperName != null)) {
            Integer sqlType = null;
            try {
                if (stmt != null) {
                    sqlType = stmt.getParameterMetaData().getParameterType(designator.index);
                } else if (rs != null) {
                    sqlType = findColumnType(designator, rs);
                }
            } catch (SQLException e) {
                // ignore
            }
            if (sqlType != null) {
                Traversable<TypeMapper<?, ?>> found = findWithParents(sqlType, mapperName, javaType, mapperTag);
                if (found.size() > 1) {
                    throw new ManyMappersFoundException(found.size() + " mappers" +
                            " found for parameter " + designator.toShortString() +
                            " with provided info " + param +
                            " and metadata SQL type " + sqlType +
                            " and value class " + qnn(valueClass == null ? null : valueClass.getName()));
                } else if (found.size() == 1) {
                    mapper = found.head();
                }
            }
        }
        // try to use provided info if name or jdbcType is provided
        if (mapper == null && (mapperName != null || jdbcType != null)) {
            Traversable<TypeMapper<?, ?>> found = findWithParents(mapperName, jdbcType, javaType, mapperTag);
            if (found.size() > 1) {
                throw new ManyMappersFoundException(found.size() + " mappers" +
                        " found for parameter " + designator.toShortString() +
                        " with provided info " + param +
                        " and value class " + qnn(valueClass == null ? null : valueClass.getName()));
            } else if (found.size() == 1) {
                mapper = found.head();
            }
        }
        // if not found
        if (mapper == null) {
            throw new NoMapperFoundException("no mapper found for parameter " + designator.toShortString() +
                    " with provided info " + param +
                    " and value class " + qnn(valueClass == null ? null : valueClass.getName()));
        }
        // update cache
        if (!cacheHit) {
            updateCache(designator, javaType, mapper);
        }
        return mapper;
    }

    private Traversable<TypeMapper<?, ?>> findWithParents(String mapperName,
                                                          ParameterJdbcType<?> jdbcType,
                                                          Class<?> javaType,
                                                          String mapperTag
    ) {
        Traversable<TypeMapper<?, ?>> found = mappers.findMappers(mapperName, jdbcType, javaType, mapperTag);
        if (!found.isEmpty()) {
            return found; // error for size>1 will be processed outside this method
        } else if (javaType != null && javaType != Objects.class) {
            // find mapper for parent classes / implemented interfaces
            // omit Object.class cause we use it for special case of javaType==null in caching
            return findOnlyParents(mapperName, jdbcType, javaType, mapperTag);
        } else {
            return found;
        }
    }

    private Traversable<TypeMapper<?, ?>> findOnlyParents(String mapperName,
                                                          ParameterJdbcType<?> jdbcType,
                                                          Class<?> javaType,
                                                          String mapperTag) {
        Traversable<TypeMapper<?, ?>> found = List.empty();
        // first check actual superclasses
        Class<?> superclass = javaType.getSuperclass();
        if (superclass != null) {
            found = findWithParents(mapperName, jdbcType, superclass, mapperTag);
        }
        // then check implemented interfaces
        if (found.isEmpty()) {
            for (Class<?> anInterface : javaType.getInterfaces()) {
                found = findWithParents(mapperName, jdbcType, anInterface, mapperTag);
                if (!found.isEmpty()) {
                    break; // found
                }
            }
        }
        return found;
    }

    private Traversable<TypeMapper<?, ?>> findWithParents(int sqlType,
                                                          String mapperName,
                                                          Class<?> javaType,
                                                          String mapperTag
    ) {
        Traversable<TypeMapper<?, ?>> found = mappers.findMappers(sqlType, mapperName, javaType, mapperTag);
        if (!found.isEmpty()) {
            return found; // error for size>1 will be processed outside this method
        } else if (javaType != null && javaType != Objects.class) {
            // find mapper for parent classes / implemented interfaces
            // omit Object.class cause we use it for special case of javaType==null in caching
            return findOnlyParents(sqlType, mapperName, javaType, mapperTag);
        } else {
            return found;
        }
    }

    private Traversable<TypeMapper<?, ?>> findOnlyParents(int sqlType,
                                                          String mapperName,
                                                          Class<?> javaType,
                                                          String mapperTag) {
        Traversable<TypeMapper<?, ?>> found = List.empty();
        // first check actual superclasses
        Class<?> superclass = javaType.getSuperclass();
        if (superclass != null) {
            found = findWithParents(sqlType, mapperName, superclass, mapperTag);
        }
        // then check implemented interfaces
        if (found.isEmpty()) {
            for (Class<?> anInterface : javaType.getInterfaces()) {
                found = findWithParents(sqlType, mapperName, anInterface, mapperTag);
                if (!found.isEmpty()) {
                    break; // found
                }
            }
        }
        return found;
    }

    private TypeMapper<?, ?> checkMapperCache(Designator designator, Class<?> javaType) {
        // if no target javaType provided - search cache for Object.class
        // (there might be cached mapper found in same case of javaType==null)
        final Class<?> useClass = javaType == null ? Object.class : javaType;
        Option<Map<Class<?>, TypeMapper<?, ?>>> cacheOpt = cachedMappers.get().get(designator);
        if (cacheOpt.isEmpty()) {
            return null;
        }
        final Map<Class<?>, TypeMapper<?, ?>> cache = cacheOpt.get();
        TypeMapper<?, ?> mapper;
        // check javaType
        mapper = cache.getOrElse(useClass, null);
        // check javaType parents
        if (mapper == null) {
            mapper = checkOnlyParents(cache, useClass);
            if (mapper != null) {
                // update cache
                updateCache(designator, useClass, mapper);
            }
        }
        return mapper;
    }

    private TypeMapper<?, ?> checkWithParents(Map<Class<?>, TypeMapper<?, ?>> cache, Class<?> aClass) {
        if (aClass == Objects.class) {
            // Object.class is used to cache cases when target javaType is not provided
            // so skip it
            return null;
        }
        // check cache
        TypeMapper<?, ?> mapper = cache.getOrElse(aClass, null);
        if (mapper == null) {
            // if missed - check parents
            mapper = checkOnlyParents(cache, aClass);
        }
        return mapper;
    }

    private TypeMapper<?, ?> checkOnlyParents(Map<Class<?>, TypeMapper<?, ?>> cache, Class<?> aClass) {
        TypeMapper<?, ?> mapper = null;
        // first check actual superclasses
        Class<?> superclass = aClass.getSuperclass();
        if (superclass != null) {
            mapper = checkWithParents(cache, superclass);
        }
        // then check implemented interfaces
        if (mapper == null) {
            for (Class<?> anInterface : aClass.getInterfaces()) {
                mapper = checkWithParents(cache, anInterface);
                if (mapper != null) {
                    break; // found
                }
            }
        }
        return mapper;
    }

    private void updateCache(Designator designator, Class<?> javaType, TypeMapper<?, ?> mapper) {
        if (javaType == null) {
            // if no target javaType was provided
            // - cache mapper for Object.class (to search in similar cases of javaType==null)
            // - and cache mapper for mapper.getJavaType()
            updateCache(designator, Object.class, mapper);
            updateCache(designator, mapper.getJavaType(), mapper);
        } else {
            cachedMappers.apply(cache ->
                    cache.put(designator,
                            cache.get(designator)
                                    .map(byClass -> byClass.put(javaType, mapper))
                                    .getOrElse(() -> HashMap.of(javaType, mapper))
                    )
            );
            if (javaType != mapper.getJavaType()) {
                cachedMappers.apply(cache ->
                        cache.put(designator,
                                cache.get(designator)
                                        .map(byClass -> byClass.put(mapper.getJavaType(), mapper))
                                        .getOrElse(() -> HashMap.of(mapper.getJavaType(), mapper))
                        )
                );
            }
            if (!cachedSQLTypes.get().containsKey(designator)) {
                updateCache(designator, mapper.getJdbcType().getSQLType().getVendorTypeNumber());
            }
        }
    }

    @Override
    public Integer findSQLType(int columnIndex,
                               ParamInfo<?, ?> param,
                               PreparedStatement stmt
    ) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        if (stmt == null) {
            throw new NullPointerException("stmt is null");
        }
        return findSQLType(designator(columnIndex), param, stmt);
    }

    private Integer findSQLType(Designator designator,
                                ParamInfo<?, ?> param,
                                PreparedStatement stmt
    ) {
        // check cache
        Integer type = cachedSQLTypes.get().get(designator).getOrNull();
        final boolean cacheHit = type != null;
        // check provided info
        if (type == null && param.getJdbcType() != null) {
            type = param.getJdbcType().getSQLType().getVendorTypeNumber();
        }
        // check JDBC PreparedStatement metadata
        if (type == null) {
            try {
                int pstmtType = stmt.getParameterMetaData().getParameterType(designator.index);
                type = pstmtType;
            } catch (SQLException e) {
                // ignore
            }
        }
        // try to find mapper
        if (type == null) {
            final TypeMapper<?, ?> mapper = findMapper(designator, param, null, stmt, null);
            type = mapper.getJdbcType().getSQLType().getVendorTypeNumber();
        }
        // update cache
        if (!cacheHit) {
            updateCache(designator, type);
        }
        return type;
    }

    private int findColumnType(Designator designator, ResultSet rs) throws SQLException {
        if (designator.label == null) {
            return rs.getMetaData().getColumnType(designator.index);
        } else {
            int index = findColumnIndex(rs, designator.label);
            return rs.getMetaData().getColumnType(index);
        }
    }

    private int findColumnIndex(ResultSet rs, String label) throws SQLException {
        Option<Integer> cachedIndex = columnIndexes.get().get(label);
        if (cachedIndex.isDefined()) {
            return cachedIndex.get();
        } else {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (label.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                    final int index = i;
                    columnIndexes.apply(cache -> cache.put(label, index));
                    return index;
                }
            }
            throw new IllegalArgumentException("no column found in ResultSet for label " + qnn(label));
        }
    }

    private void updateCache(Designator designator, Integer type) {
        cachedSQLTypes.apply(cache -> cache.put(designator, type));
    }

}
