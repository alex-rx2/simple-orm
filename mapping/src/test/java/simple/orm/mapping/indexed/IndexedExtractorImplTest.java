package simple.orm.mapping.indexed;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.impl.cache.FoundMappersCacheTest;
import simple.orm.mapping.indexed.IndexedExtractorImpl;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterGetterImpl;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetter;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static simple.orm.mapping.param.ParameterGetterImpl.wrapCheckWasNull;

/**
 * {@link IndexedExtractorImpl} tests.
 * <br>
 * With current class design heavily relies on proper work of {@link FoundMappersCache}.
 *
 * @see FoundMappersCacheTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IndexedExtractorImplTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow() throws SQLException {
        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final ParameterSetter<String> setterTypeString = mock();
        final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class,
                new ParameterGetterImpl<>(
                        ResultSet::getString,
                        ResultSet::getString
                ),
                setterTypeString
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<String, String> mapperString = new SimpleTypeMapper<>(typeString, String.class, Function1.identity(), Function1.identity());

        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("param2", "tag2");
        final ParamInfo<String, String> paramInfo3 = ParamInfo.of(typeString, String.class);

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getInt(eq(2))).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);
        when(rs.getString(eq("somelabel"))).thenReturn("some string");

        final MappersFinder mappersFinder = mock();
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq("somelabel"),
                same(paramInfo3),
                same(rs)
        )).thenReturn((TypeMapper) mapperString);

        final IndexedExtractorImpl extractor = new IndexedExtractorImpl(
                mappersFinder,
                List.of(IndexedParameter.of(1, mapperInt),
                        IndexedParameter.of(2, paramInfo2),
                        IndexedParameter.of("somelabel", paramInfo3))
        );

        // do test
        final Seq<Object> row = extractor.extractRow(rs);
        assertThat(row).containsExactly(123, null, "some string");

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, rs);
        // param 1 - TypeMapper is known, only get from ResultSet
        inOrder.verify(rs).getInt(eq(1));
        // param 2 - find mapper, then get (and check for null in typeInt getter)
        inOrder.verify(mappersFinder).findMapper(eq(2), same(paramInfo2), same(rs));
        inOrder.verify(rs).getInt(eq(2));
        inOrder.verify(rs).wasNull();
        // param 3 - find mapper, get from resultset
        inOrder.verify(mappersFinder).findMapper(eq("somelabel"), same(paramInfo3), same(rs));
        inOrder.verify(rs).getString(eq("somelabel"));

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, rs);
        verifyNoInteractions(setterTypeInt, setterTypeString);
    }

    @Test
    public void testExtractRow_mappings() throws SQLException {
        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(
                JDBCType.INTEGER,
                Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final TypeMapper<Integer, String> mapperIntStr = new SimpleTypeMapper<>(
                typeInt,
                String.class,
                s -> s == null ? null : Integer.valueOf(s.substring(1)),
                i -> i == null ? null : "s" + i
        );

        final MappersFinder mappersFinder = mock();

        final ResultSet rs = mock();
        when(rs.getInt(eq("a_label"))).thenReturn(123);
        when(rs.getInt(eq(2))).thenReturn(-321);
        when(rs.getInt(eq(3))).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);

        final IndexedExtractorImpl extractor = new IndexedExtractorImpl(
                mappersFinder,
                List.of(IndexedParameter.of("a_label", mapperIntStr),
                        IndexedParameter.of(2, mapperIntStr),
                        IndexedParameter.of(3, mapperIntStr))
        );

        // do test
        final Seq<Object> row = extractor.extractRow(rs);
        assertThat(row).containsExactly("s123", "s-321", null);

        // verify mocks
        final InOrder inOrder = inOrder(rs);
        // TypeMapper is always known, so only get value from ResultSet
        inOrder.verify(rs).getInt(eq("a_label"));
        inOrder.verify(rs).getInt(eq(2));
        inOrder.verify(rs).getInt(eq(3));
        inOrder.verify(rs).wasNull();

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(rs);
        verifyNoInteractions(setterTypeInt, mappersFinder);
    }

}
