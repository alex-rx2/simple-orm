package simple.orm.mapping.builder;

import simple.orm.mapping.ManyMappersFoundException;
import simple.orm.mapping.NoMapperFoundException;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.TypeMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * An interface for utility class capable to find {@link TypeMapper}s for actual injectors and extractors.
 */
public interface MappersFinder {

    /**
     * Finds {@link TypeMapper} for the parameter.
     *
     * @return found {@link TypeMapper}.
     * @throws NoMapperFoundException    if mapper not found.
     * @throws ManyMappersFoundException if more than one mapper found.
     */
    TypeMapper<?, ?> findMapper(int columnIndex, ParamInfo<?, ?> param, Class<?> valueClass, PreparedStatement stmt);

    /**
     * Finds {@link TypeMapper} for the parameter.
     *
     * @return found {@link TypeMapper}.
     * @throws NoMapperFoundException    if mapper not found.
     * @throws ManyMappersFoundException if more than one mapper found.
     */
    TypeMapper<?, ?> findMapper(int columnIndex, ParamInfo<?, ?> param, ResultSet rs);

    /**
     * Finds {@link TypeMapper} for the parameter.
     *
     * @return found {@link TypeMapper}.
     * @throws NoMapperFoundException    if mapper not found.
     * @throws ManyMappersFoundException if more than one mapper found.
     */
    TypeMapper<?, ?> findMapper(String columnLabel, ParamInfo<?, ?> param, ResultSet rs);

    /**
     * Find {@link java.sql.SQLType#getVendorTypeNumber()} for parameter
     * (required for {@link PreparedStatement#setNull(int, int)}).
     *
     * @return found type or <code>null</code>.
     */
    Integer findSQLType(int columnIndex, ParamInfo<?, ?> param, PreparedStatement stmt);

    /**
     * Factory method for default {@link MappersFinder} implementation.
     *
     * @param mappers {@link MappersCollection} to search in.
     * @return new default {@link MappersFinder} implementation.
     */
    static MappersFinder defaultFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        return new FoundMappersCache(mappers);
    }

}
