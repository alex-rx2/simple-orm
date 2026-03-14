package simple.orm.mapping.impl.cache;

import io.vavr.Function1;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.mapping.NoMapperFoundException;
import simple.orm.mapping.impl.ParameterJdbcTypeImpl;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static simple.orm.mapping.impl.cache.FoundMappersCache.designator;

/**
 * {@link FoundMappersCache} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// TODO remove testing of internal method
// TODO rewrite to test proper use cases with PreparedStatement or ResultSet always present
public class FoundMappersCacheTest {

    @Test
    public void testFindMapper_byName_internal() {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final TypeMapper<Integer, Integer> result = new SimpleTypeMapper<>(jdbcType, Integer.class, Function1.identity(), Function1.identity());
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(eq("someMapper"), isNull(), isNull(), isNull())).thenReturn(List.of(result));
        // find by name
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), null, null, null);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), isNull(), isNull());
            verifyNoMoreInteractions(mappers);
        }
        // find by name (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name and types (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", jdbcType, null, null), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", jdbcType, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name for other column (no cached value)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(11), ParamInfo.of("someMapper", null, null, null), null, null, null);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(2)).findMappers(eq("someMapper"), isNull(), isNull(), isNull()); // +1
            verifyNoMoreInteractions(mappers);
        }
    }

    @Test
    public void testFindMapper_byJdbcType_internal() {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final TypeMapper<Integer, Integer> result = new SimpleTypeMapper<>(jdbcType, Integer.class, Function1.identity(), Function1.identity());
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(isNull(), same(jdbcType), isNull(), eq("someTag"))).thenReturn(List.of(result));
        // find by jdbcType
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, null, "someTag"), null, null, null);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(1)).findMappers(isNull(), same(jdbcType), isNull(), eq("someTag"));
            verifyNoMoreInteractions(mappers);
        }
        // find by jdbcType (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, null, "someTag"), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name and types (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", jdbcType, null, "someTag"), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, Integer.class, "someTag"), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", jdbcType, Integer.class, "someTag"), null, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by jdbcType for other column (no cached value)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(3), ParamInfo.of(null, jdbcType, null, "someTag"), null, null, null);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(2)).findMappers(isNull(), same(jdbcType), isNull(), eq("someTag")); // +1
            verifyNoMoreInteractions(mappers);
        }
    }

    @Test
    public void testFindMapper_byNameJavaType_internal() {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final TypeMapper<Integer, Integer> result1 = new SimpleTypeMapper<>(jdbcType, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<Integer, Double> result2 = new SimpleTypeMapper<>(jdbcType, Double.class, Double::intValue, Integer::doubleValue);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull())).thenReturn(List.of(result1));
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(String.class), isNull())).thenReturn(List.empty());
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Double.class), isNull())).thenReturn(List.of(result2));
        when(mappers.findMappers(
                eq("someMapper"),
                isNull(),
                argThat(aClass -> notA(aClass, Integer.class, String.class, Double.class)),
                isNull())
        ).thenReturn(List.empty());
        // find by name and javaType
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result1);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
        // find by name and javaType (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result1);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name and wrong javaType
        {
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, String.class, null), null, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(String.class), isNull());
            verify(mappers, times(6)) // String superclass and interfaces: Object, Serializable, Comparable, CharSequence, Constable, ConstantDesc
                    .findMappers(
                            eq("someMapper"),
                            isNull(),
                            argThat(aClass -> notA(aClass, Integer.class, String.class, Double.class)),
                            isNull()
                    );
            verifyNoMoreInteractions(mappers);
        }
        // find by name and other javaType
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Double.class, null), null, null, null);
            assertThat(mapper).isSameAs(result2);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Double.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
    }

    @Test
    public void testFindMapper_byTypes_internal() {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final TypeMapper<Integer, Integer> result1 = new SimpleTypeMapper<>(jdbcType, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<Integer, Double> result2 = new SimpleTypeMapper<>(jdbcType, Double.class, Double::intValue, Integer::doubleValue);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(isNull(), eq(jdbcType), eq(Integer.class), isNull())).thenReturn(List.of(result1));
        when(mappers.findMappers(isNull(), eq(jdbcType), eq(String.class), isNull())).thenReturn(List.empty());
        when(mappers.findMappers(isNull(), eq(jdbcType), eq(Double.class), isNull())).thenReturn(List.of(result2));
        when(mappers.findMappers(
                isNull(),
                eq(jdbcType),
                argThat(aClass -> notA(aClass, Integer.class, String.class, Double.class)),
                isNull())
        ).thenReturn(List.empty());
        // find by name and javaType
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result1);
            verify(mappers, times(1)).findMappers(isNull(), eq(jdbcType), eq(Integer.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
        // find by name and javaType (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result1);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name and wrong javaType
        {
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, String.class, null), null, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            verify(mappers, times(1)).findMappers(isNull(), eq(jdbcType), eq(String.class), isNull());
            verify(mappers, times(6)) // String superclass and interfaces: Object, Serializable, Comparable, CharSequence, Constable, ConstantDesc
                    .findMappers(
                            isNull(),
                            eq(jdbcType),
                            argThat(aClass -> notA(aClass, Integer.class, String.class, Double.class)),
                            isNull()
                    );
            verifyNoMoreInteractions(mappers);
        }
        // find by name and other javaType
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of(null, jdbcType, Double.class, null), null, null, null);
            assertThat(mapper).isSameAs(result2);
            verify(mappers, times(1)).findMappers(isNull(), eq(jdbcType), eq(Double.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
    }

    @Test
    public void testFindMapper_noNameNoJdbcTypeNoMetadata_internal() {
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        // find without proper info provided
        {
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of(null, null, null, null), null, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of(null, null, null, "someTag"), null, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of(null, null, Integer.class, null), null, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of(null, null, Integer.class, "someTag"), null, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            verifyNoInteractions(mappers); // cache actually doesn't even call mappers.findMapper(...)
        }
    }

    @Test
    public void testFindMapper_severalCachedValues_internal() {
        final ParameterJdbcType<String> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.VARCHAR, String.class, mock(), mock());
        final TypeMapper<String, Integer> result1 = new SimpleTypeMapper<>(jdbcType, Integer.class, String::valueOf, Integer::valueOf);
        final TypeMapper<String, Double> result2 = new SimpleTypeMapper<>(jdbcType, Double.class, String::valueOf, Double::valueOf);
        final TypeMapper<String, BigDecimal> result3 = new SimpleTypeMapper<>(jdbcType, BigDecimal.class, BigDecimal::toString, BigDecimal::new);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull())).thenReturn(List.of(result1));
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Double.class), isNull())).thenReturn(List.of(result2));
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(BigDecimal.class), isNull())).thenReturn(List.of(result3));
        // find them all
        {
            TypeMapper<?, ?> mapper1 = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Integer.class, null), null, null, null);
            TypeMapper<?, ?> mapper2 = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Double.class, null), null, null, null);
            TypeMapper<?, ?> mapper3 = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, BigDecimal.class, null), null, null, null);
            assertThat(mapper1).isSameAs(result1);
            assertThat(mapper2).isSameAs(result2);
            assertThat(mapper3).isSameAs(result3);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull());
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Double.class), isNull());
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(BigDecimal.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
        // find from cache now
        {
            TypeMapper<?, ?> mapper1 = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Integer.class, null), null, null, null);
            TypeMapper<?, ?> mapper2 = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Double.class, null), null, null, null);
            TypeMapper<?, ?> mapper3 = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, BigDecimal.class, null), null, null, null);
            assertThat(mapper1).isSameAs(result1);
            assertThat(mapper2).isSameAs(result2);
            assertThat(mapper3).isSameAs(result3);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
    }

    @Test
    public void testFindMapper_byNameValueClass_internal() {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final TypeMapper<Integer, Integer> result1 = new SimpleTypeMapper<>(jdbcType, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<Integer, Double> result2 = new SimpleTypeMapper<>(jdbcType, Double.class, Double::intValue, Integer::doubleValue);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull())).thenReturn(List.of(result1));
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(String.class), isNull())).thenReturn(List.empty());
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Double.class), isNull())).thenReturn(List.of(result2));
        when(mappers.findMappers(
                eq("someMapper"),
                isNull(),
                argThat(aClass -> notA(aClass, Integer.class, String.class, Double.class)),
                isNull())
        ).thenReturn(List.empty());
        // find by name and valueClass
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), Integer.class, null, null);
            assertThat(mapper).isSameAs(result1);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
        // find by name and valueClass (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), Integer.class, null, null);
            assertThat(mapper).isSameAs(result1);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name and using javaType (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Integer.class, null), null, null, null);
            assertThat(mapper).isSameAs(result1);
            verifyNoMoreInteractions(mappers); // no new interaction, found in cache
        }
        // find by name and wrong valueClass
        {
            assertThatCode(() -> cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), String.class, null, null))
                    .isInstanceOf(NoMapperFoundException.class);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(String.class), isNull());
            verify(mappers, times(6)) // String superclass and interfaces: Object, Serializable, Comparable, CharSequence, Constable, ConstantDesc
                    .findMappers(
                            eq("someMapper"),
                            isNull(),
                            argThat(aClass -> notA(aClass, Integer.class, String.class, Double.class)),
                            isNull()
                    );
            verifyNoMoreInteractions(mappers);
        }
        // find by name and other valueClass
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), Double.class, null, null);
            assertThat(mapper).isSameAs(result2);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Double.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
    }

    @Test
    public void testFindMapper_byNameValueClass_inheritance_internal() {
        final ParameterJdbcType<String> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.VARCHAR, String.class, mock(), mock());
        final TypeMapper<String, Number> result = new SimpleTypeMapper<>(jdbcType, Number.class, Number::toString, Integer::valueOf);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Number.class), isNull())).thenReturn(List.of(result));
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull())).thenReturn(List.empty());
        // find by name and Integer.class
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), Integer.class, null, null);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Integer.class), isNull());
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Number.class), isNull());
            verifyNoMoreInteractions(mappers);
        }
        // find by name and Integer.class (from cache now)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, null, null), Integer.class, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers);
        }
        // find by name and Double.class (from cache, cause result is cached for Number.class)
        {
            TypeMapper<?, ?> mapper = cache.findMapper(
                    designator(13), ParamInfo.of("someMapper", null, Number.class, null), Double.class, null, null);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers);
        }
    }

    @Test
    public void testFindMapper_javaTypeWithPStmtMetadata() throws Exception {
        final ParameterJdbcType<String> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.VARCHAR, String.class, mock(), mock());
        final TypeMapper<String, Integer> result = new SimpleTypeMapper<>(jdbcType, Integer.class, String::valueOf, Integer::valueOf);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        // no findMappers will be called without name and jdbcType provided
        when(mappers.findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Integer.class), isNull()))
                .thenReturn(List.of(result));
        final PreparedStatement pstmt = mock();
        final ParameterMetaData pmeta = mock();
        when(pstmt.getParameterMetaData()).thenReturn(pmeta);
        when(pmeta.getParameterType(eq(13))).thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        // find by Integer.class with PreparedStatement metadata
        {
            TypeMapper<?, ?> mapper = cache.findMapper(13, ParamInfo.of(null, null, null, null), Integer.class, pstmt);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(1)).findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Integer.class), isNull());
            verify(pstmt, times(1)).getParameterMetaData();
            verify(pmeta, times(1)).getParameterType(eq(13));
            verifyNoMoreInteractions(mappers);
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
        // find by Integer.class with PreparedStatement metadata, now from cache
        {
            TypeMapper<?, ?> mapper = cache.findMapper(13, ParamInfo.of(null, null, null, null), Integer.class, pstmt);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers);
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
    }

    @Test
    public void testFindMapper_nameJavaTypeWithRSMetadata() throws Exception {
        final ParameterJdbcType<String> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.VARCHAR, String.class, mock(), mock());
        final TypeMapper<String, Integer> result = new SimpleTypeMapper<>(jdbcType, Integer.class, String::valueOf, Integer::valueOf);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        // no findMappers will be called without name and jdbcType provided
        when(mappers.findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Integer.class), isNull()))
                .thenReturn(List.of(result));
        final ResultSet rs = mock();
        final ResultSetMetaData rsmeta = mock();
        when(rs.getMetaData()).thenReturn(rsmeta);
        when(rsmeta.getColumnType(eq(13))).thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        // find by Integer.class with ResultSet metadata
        {
            TypeMapper<?, ?> mapper = cache.findMapper(13, ParamInfo.of(null, null, Integer.class, null), rs);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(1)).findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Integer.class), isNull());
            verify(rs, times(1)).getMetaData();
            verify(rsmeta, times(1)).getColumnType(eq(13));
            verifyNoMoreInteractions(mappers);
            verifyNoMoreInteractions(rs);
            verifyNoMoreInteractions(rsmeta);
        }
        // find by Integer.class with ResultSet metadata, now from cache
        {
            TypeMapper<?, ?> mapper = cache.findMapper(13, ParamInfo.of(null, null, Integer.class, null), rs);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers);
            verifyNoMoreInteractions(rs);
            verifyNoMoreInteractions(rsmeta);
        }
    }

    @Test
    public void testFindMapper_javaTypeWithPStmtMetadata_inheritance() throws Exception {
        final ParameterJdbcType<String> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.VARCHAR, String.class, mock(), mock());
        final TypeMapper<String, Number> result = new SimpleTypeMapper<>(jdbcType, Number.class, String::valueOf, Integer::valueOf);
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        // no findMappers will be called without name and jdbcType provided
        when(mappers.findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Integer.class), isNull()))
                .thenReturn(List.empty());
        when(mappers.findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Number.class), isNull()))
                .thenReturn(List.of(result));
        final PreparedStatement pstmt = mock();
        final ParameterMetaData pmeta = mock();
        when(pstmt.getParameterMetaData()).thenReturn(pmeta);
        when(pmeta.getParameterType(eq(13))).thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        // find by Integer.class with PreparedStatement metadata
        {
            TypeMapper<?, ?> mapper = cache.findMapper(13, ParamInfo.of(null, null, null, null), Integer.class, pstmt);
            assertThat(mapper).isSameAs(result);
            verify(mappers, times(1)).findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Integer.class), isNull());
            verify(mappers, times(1)).findMappers(eq(JDBCType.VARCHAR.getVendorTypeNumber()), isNull(), eq(Number.class), isNull());
            verify(pstmt, times(1)).getParameterMetaData();
            verify(pmeta, times(1)).getParameterType(eq(13));
            verifyNoMoreInteractions(mappers);
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
        // find by Integer.class with PreparedStatement metadata, now from cache
        {
            TypeMapper<?, ?> mapper = cache.findMapper(13, ParamInfo.of(null, null, null, null), Integer.class, pstmt);
            assertThat(mapper).isSameAs(result);
            verifyNoMoreInteractions(mappers);
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
    }

    @Test
    public void testFindSQLType_byName() throws SQLException {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final TypeMapper<Integer, Integer> mapper = new SimpleTypeMapper<>(jdbcType, Integer.class, Function1.identity(), Function1.identity());
        final PreparedStatement pstmt = mock();
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(pstmt.getParameterMetaData()).thenThrow(new SQLException());
        when(mappers.findMappers(eq("someMapper"), isNull(), isNull(), isNull())).thenReturn(List.of(mapper));
        // find by name
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), isNull(), isNull());
            verify(pstmt, times(2)).getParameterMetaData();  // 1, +1 in findMappers when searching for TypeMapper
            verifyNoMoreInteractions(mappers, pstmt);
        }
        // find by name (from cache now)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoMoreInteractions(mappers, pstmt); // no new interaction, found in cache
        }
        // find by name and types (from cache now)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, Integer.class, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoMoreInteractions(mappers, pstmt); // no new interaction, found in cache
        }
        // find by name for other column (no cached value)
        {
            Integer type = cache.findSQLType(11, ParamInfo.of("someMapper", null, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verify(mappers, times(2)).findMappers(eq("someMapper"), isNull(), isNull(), isNull()); // +1
            verify(pstmt, times(4)).getParameterMetaData(); // +2
            verifyNoMoreInteractions(mappers, pstmt);
        }
    }

    @Test
    public void testFindSQLType_bySQLType() {
        final ParameterJdbcType<Integer> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.INTEGER, Integer.class, mock(), mock());
        final PreparedStatement pstmt = mock();
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        // find by jdbcType
        {
            Integer type = cache.findSQLType(13, ParamInfo.of(null, jdbcType, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers, pstmt); // ParameterJdbcType actually contains required info, no other calls required
        }
        // find by jdbcType (from cache now, but indistinguishable for us)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of(null, jdbcType, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers, pstmt); // ParameterJdbcType actually contains required info, no other calls required
        }
        // find by name and types (from cache now, but indistinguishable for us)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of(null, jdbcType, Integer.class, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers, pstmt); // ParameterJdbcType actually contains required info, no other calls required
        }
    }

    @Test
    public void testFindSQLType_byNameJavaTypeTag_inheritance() throws SQLException {
        final ParameterJdbcType<String> jdbcType = new ParameterJdbcTypeImpl<>(JDBCType.VARCHAR, String.class, mock(), mock());
        final TypeMapper<String, Number> mapper = new SimpleTypeMapper<>(jdbcType, Number.class, Number::toString, Integer::valueOf);
        final PreparedStatement pstmt = mock();
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        when(pstmt.getParameterMetaData()).thenThrow(new SQLException());
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Integer.class), eq("tagg"))).thenReturn(List.empty());
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(Number.class), eq("tagg"))).thenReturn(List.of(mapper));
        when(mappers.findMappers(eq("someMapper"), isNull(), eq(String.class), eq("tagg"))).thenReturn(List.empty());
        when(mappers.findMappers(
                eq("someMapper"),
                isNull(),
                argThat(aClass -> notA(aClass, Integer.class, Number.class, String.class)),
                eq("tagg"))
        ).thenReturn(List.empty());
        // find by name, javaType and tag
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, Integer.class, "tagg"), pstmt);
            assertThat(type).isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Integer.class), eq("tagg"));
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(Number.class), eq("tagg"));
            verify(pstmt, times(2)).getParameterMetaData(); // 1, +1 called when searching for TypeMapper
            verifyNoMoreInteractions(mappers, pstmt);
        }
        // find by name, javaType and tag (from cache now)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, Integer.class, "tagg"), pstmt);
            assertThat(type).isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
            verifyNoMoreInteractions(mappers, pstmt); // no new interaction, found in cache
        }
        // find by name, other javaType and tag (from cache actually, cause of inheritance)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, Double.class, "tagg"), pstmt);
            assertThat(type).isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
            verifyNoMoreInteractions(mappers, pstmt); // no new interaction, found in cache
        }
        // find by name and wrong javaType (traverse all parents/interfaces and find nothing)
        {
            // have to search with other designator cause SQLType is cashed per designator
            // (query can't have different SQLType for same column/parameter).
            assertThatCode(() -> cache.findSQLType(11, ParamInfo.of("someMapper", null, String.class, "tagg"), pstmt))
                    .isInstanceOf(NoMapperFoundException.class);
            verify(mappers, times(1)).findMappers(eq("someMapper"), isNull(), eq(String.class), eq("tagg"));
            verify(mappers, times(6)) // String superclass and interfaces: Object, Serializable, Comparable, CharSequence, Constable, ConstantDesc
                    .findMappers(
                            eq("someMapper"),
                            isNull(),
                            argThat(aClass -> notA(aClass, Integer.class, Number.class, String.class)),
                            eq("tagg")
                    );
            verify(pstmt, times(4)).getParameterMetaData(); // +2
            verifyNoMoreInteractions(mappers, pstmt);
        }
    }

    @Test
    public void testFindSQLType_byPStmtMetadata() throws Exception {
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        final PreparedStatement pstmt = mock();
        final ParameterMetaData pmeta = mock();
        when(pstmt.getParameterMetaData()).thenReturn(pmeta);
        when(pmeta.getParameterType(eq(13))).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        // find by name
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers); // with JDBC metadata available no MappersCollection calls needed
            verify(pstmt, times(1)).getParameterMetaData();
            verify(pmeta, times(1)).getParameterType(eq(13));
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
        // find by name (from cache now)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, null, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers); // with JDBC metadata available no MappersCollection calls needed
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
        // find by name and types (from cache anyway)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of("someMapper", null, Integer.class, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers); // with JDBC metadata available no MappersCollection calls needed
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
    }

    @Test
    public void testFindSQLType_byPStmtMetadata_inheritance() throws Exception {
        final MappersCollection mappers = mock();
        final FoundMappersCache cache = new FoundMappersCache(mappers);
        final PreparedStatement pstmt = mock();
        final ParameterMetaData pmeta = mock();
        when(pstmt.getParameterMetaData()).thenReturn(pmeta);
        when(pmeta.getParameterType(eq(13))).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        // find by javaType
        {
            Integer type = cache.findSQLType(13, ParamInfo.of(null, null, Number.class, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers); // with JDBC metadata available no MappersCollection calls needed
            verify(pstmt, times(1)).getParameterMetaData();
            verify(pmeta, times(1)).getParameterType(eq(13));
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
        // find by javaType (from cache now)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of(null, null, Number.class, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers); // with JDBC metadata available no MappersCollection calls needed
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
        // find by child javaType (from cache, with inheritance)
        {
            Integer type = cache.findSQLType(13, ParamInfo.of(null, null, Integer.class, null), pstmt);
            assertThat(type).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
            verifyNoInteractions(mappers); // with JDBC metadata available no MappersCollection calls needed
            verifyNoMoreInteractions(pstmt);
            verifyNoMoreInteractions(pmeta);
        }
    }

    private boolean notA(Class<?> aClass, Class<?>... otherClasses) {
        for (Class<?> oClass : otherClasses) {
            if (aClass == oClass) {
                return false;
            }
        }
        return true;
    }

}
