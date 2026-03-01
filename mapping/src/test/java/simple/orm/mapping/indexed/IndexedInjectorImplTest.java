package simple.orm.mapping.indexed;

import io.vavr.Function1;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.impl.cache.FoundMappersCacheTest;
import simple.orm.mapping.indexed.IndexedInjectorImpl;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetterImpl;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link IndexedInjectorImpl} tests.
 * <br>
 * With current class design heavily relies on proper work of {@link FoundMappersCache}.
 *
 * @see FoundMappersCacheTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IndexedInjectorImplTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testInjectParameters() throws SQLException {
        final ParameterGetter<Integer> getterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                getterTypeInt,
                new ParameterSetterImpl<>(
                        PreparedStatement::setInt,
                        (stmt, index) -> stmt.setNull(index, JDBCType.INTEGER.getVendorTypeNumber())
                )
        );
        final ParameterGetter<String> getterTypeString = mock();
        final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class,
                getterTypeString,
                new ParameterSetterImpl<>(PreparedStatement::setString)
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<String, String> mapperString = new SimpleTypeMapper<>(typeString, String.class, Function1.identity(), Function1.identity());

        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("param2", "tag2");
        final ParamInfo<String, String> paramInfo3 = ParamInfo.of(typeString, String.class);

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(eq(1), eq(123));
        doNothing().when(pstmt).setNull(eq(2), eq(JDBCType.VARCHAR.getVendorTypeNumber()));
        doNothing().when(pstmt).setString(eq(3), eq("some string"));

        final MappersFinder mappersFinder = mock();
        when(mappersFinder.findJDBCType(
                eq(2),
                same(paramInfo2),
                same(pstmt)
        )).thenReturn(JDBCType.VARCHAR);
        when(mappersFinder.findMapper(
                eq(3),
                same(paramInfo3),
                eq(String.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperString);

        final IndexedInjectorImpl injector = new IndexedInjectorImpl(
                mappersFinder,
                List.of(IndexedParameter.of(1, mapperInt),
                        IndexedParameter.of(2, paramInfo2),
                        IndexedParameter.of(3, paramInfo3))
        );

        // do test
        injector.injectParameters(pstmt, 123, null, "some string");

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, pstmt);
        // param 1 - TypeMapper is known, only set parameter
        inOrder.verify(pstmt).setInt(eq(1), eq(123));
        // param 2 - get JDBCType and set null directly (without mapper)
        inOrder.verify(mappersFinder).findJDBCType(eq(2), same(paramInfo2), same(pstmt));
        inOrder.verify(pstmt).setNull(eq(2), eq(JDBCType.VARCHAR.getVendorTypeNumber()));
        // param 3 - find mapper, set parameter
        inOrder.verify(mappersFinder).findMapper(eq(3), same(paramInfo3), eq(String.class), same(pstmt));
        inOrder.verify(pstmt).setString(eq(3), eq("some string"));

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, pstmt);
        verifyNoInteractions(getterTypeInt, getterTypeString);
    }

    @Test
    public void testInjectParameters_mappingJdbcToJavaAndBack() throws SQLException {
        final ParameterGetter<Integer> getterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                getterTypeInt,
                new ParameterSetterImpl<>(
                        PreparedStatement::setInt,
                        (stmt, index) -> stmt.setNull(index, JDBCType.INTEGER.getVendorTypeNumber())
                )
        );
        final TypeMapper<Integer, String> mapperIntStr = new SimpleTypeMapper<>(
                typeInt,
                String.class,
                s -> s == null ? null : Integer.valueOf(s.substring(1)),
                i -> i == null ? null : "s" + i
        );

        final MappersFinder mappersFinder = mock();

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(eq(1), eq(123));
        doNothing().when(pstmt).setInt(eq(2), eq(-321));
        doNothing().when(pstmt).setNull(eq(3), eq(JDBCType.INTEGER.getVendorTypeNumber()));

        final IndexedInjectorImpl injector = new IndexedInjectorImpl(
                mappersFinder,
                List.of(IndexedParameter.of(1, mapperIntStr),
                        IndexedParameter.of(2, mapperIntStr),
                        IndexedParameter.of(3, mapperIntStr))
        );

        // do test
        injector.injectParameters(pstmt, "s123", "s-321", null);

        // verify mocks
        final InOrder inOrder = inOrder(pstmt);
        // TypeMapper is always known, so only set value on PreparedStatement
        inOrder.verify(pstmt).setInt(eq(1), eq(123));
        inOrder.verify(pstmt).setInt(eq(2), eq(-321));
        inOrder.verify(pstmt).setNull(eq(3), eq(JDBCType.INTEGER.getVendorTypeNumber()));

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(pstmt);
        verifyNoInteractions(getterTypeInt, mappersFinder);
    }

}
