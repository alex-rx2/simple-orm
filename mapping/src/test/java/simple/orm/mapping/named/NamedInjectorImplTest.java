package simple.orm.mapping.named;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetterImpl;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link NamedInjectorImpl} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NamedInjectorImplTest {

    private Either<Method, Field> someClassOne_ref1;
    private Either<Method, Field> someClassOne_ref2;
    private Either<Method, Field> someClassOne_ref3;
    private Either<Method, Field> someClassTwo_ref1;
    private Either<Method, Field> someClassTwo_ref2;
    private Either<Method, Field> someClassThree_ref1;
    private Either<Method, Field> someClassThree_ref2;

    @BeforeAll
    void setUpAll() throws NoSuchFieldException, NoSuchMethodException {
        {
            final Class<SomeClassOne> scoClass = SomeClassOne.class;
            someClassOne_ref1 = Either.right(scoClass.getDeclaredField("fieldInteger"));
            someClassOne_ref2 = Either.right(scoClass.getDeclaredField("fieldStringOne"));
            someClassOne_ref3 = Either.right(scoClass.getDeclaredField("fieldStringTwo"));
            someClassOne_ref1.get().trySetAccessible();
            someClassOne_ref2.get().trySetAccessible();
            someClassOne_ref3.get().trySetAccessible();
        }
        {
            final Class<SomeClassTwo> sctClass = SomeClassTwo.class;
            someClassTwo_ref1 = Either.left(sctClass.getDeclaredMethod("getFieldInteger"));
            someClassTwo_ref2 = Either.left(sctClass.getDeclaredMethod("getClassOne"));
            someClassTwo_ref1.getLeft().trySetAccessible();
            someClassTwo_ref2.getLeft().trySetAccessible();
        }
        {
            final Class<SomeClassThree> sctClass = SomeClassThree.class;
            someClassThree_ref1 = Either.left(sctClass.getDeclaredMethod("getFieldInteger"));
            someClassThree_ref2 = Either.left(sctClass.getDeclaredMethod("getClassTwo"));
            someClassThree_ref1.getLeft().trySetAccessible();
            someClassThree_ref2.getLeft().trySetAccessible();
        }
    }

    @Test
    public void testInjectParameters_SomeClassOne_mapperProvided() throws SQLException {
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

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(anyInt(), anyInt());
        doNothing().when(pstmt).setString(anyInt(), any());

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findGetter(eq("fieldStringOne"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref2);
        when(reflectionsFinder.findGetter(eq("fieldStringTwo"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref3);

        final NamedInjectorImpl<SomeClassOne> injector = new NamedInjectorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", mapperInt),
                        NamedParameter.of(2, "fieldStringOne", mapperString),
                        NamedParameter.of(3, "fieldStringTwo", mapperString)),
                SomeClassOne.class,
                List.empty()
        );

        // do test
        injector.injectParameters(pstmt, new SomeClassOne(123, "some string", null));

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, reflectionsFinder, pstmt);
        // param 1
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassOne.class));
        inOrder.verify(pstmt).setInt(eq(1), eq(123));
        // param 2
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldStringOne"), eq(SomeClassOne.class));
        inOrder.verify(pstmt).setString(eq(2), eq("some string"));
        // param 3
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldStringTwo"), eq(SomeClassOne.class));
        inOrder.verify(pstmt).setString(eq(3), isNull());

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, pstmt);
        verifyNoInteractions(getterTypeInt, getterTypeString);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testInjectParameters_SomeClassOne_paramInfoProvided() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("stringMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("stringMapper", null, null, null);

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

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(anyInt(), anyInt());
        doNothing().when(pstmt).setString(anyInt(), any());

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findGetter(eq("fieldStringOne"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref2);
        when(reflectionsFinder.findGetter(eq("fieldStringTwo"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref3);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                eq(Integer.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                eq(String.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperString);
        when(mappersFinder.findSQLType(
                eq(3),
                same(paramInfo3),
                same(pstmt)
        )).thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());

        final NamedInjectorImpl<SomeClassOne> injector = new NamedInjectorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "fieldStringOne", paramInfo2),
                        NamedParameter.of(3, "fieldStringTwo", paramInfo3)),
                SomeClassOne.class,
                List.empty()
        );

        // do test
        injector.injectParameters(pstmt, new SomeClassOne(123, "some string", null));

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, reflectionsFinder, pstmt);
        // param 1 - has value - use its class when searching for mapper
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassOne.class));
        inOrder.verify(mappersFinder).findMapper(eq(1), same(paramInfo1), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(1), eq(123));
        // param 2 - has value - use its class when searching for mapper
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldStringOne"), eq(SomeClassOne.class));
        inOrder.verify(mappersFinder).findMapper(eq(2), same(paramInfo2), eq(String.class), same(pstmt));
        inOrder.verify(pstmt).setString(eq(2), eq("some string"));
        // param 3 - null value, no value class
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldStringTwo"), eq(SomeClassOne.class));
        inOrder.verify(mappersFinder).findSQLType(eq(3), same(paramInfo3), same(pstmt)); // direct null injection
        inOrder.verify(pstmt).setNull(eq(3), eq(JDBCType.VARCHAR.getVendorTypeNumber()));

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, pstmt);
        verifyNoInteractions(getterTypeInt, getterTypeString);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testInjectParameters_SomeClassThree_paramInfoProvided_complexProps_and_caching() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("intMapper", null, null, null);

        final ParameterGetter<Integer> getterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                getterTypeInt,
                new ParameterSetterImpl<>(
                        PreparedStatement::setInt,
                        (stmt, index) -> stmt.setNull(index, JDBCType.INTEGER.getVendorTypeNumber())
                )
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(anyInt(), anyInt());

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref1);
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref1);
        when(reflectionsFinder.findGetter(eq("classOne"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref2);
        when(reflectionsFinder.findGetter(eq("classTwo"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref2);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                eq(Integer.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                eq(Integer.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(3),
                same(paramInfo3),
                eq(Integer.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperInt);

        final NamedInjectorImpl<SomeClassThree> injector = new NamedInjectorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "classTwo.fieldInteger", paramInfo2),
                        NamedParameter.of(3, "classTwo.classOne.fieldInteger", paramInfo3)),
                SomeClassThree.class,
                List.empty()
        );

        // do test
        injector.injectParameters(pstmt, new SomeClassThree(123, new SomeClassTwo(-123, new SomeClassOne(321, null, null))));

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, reflectionsFinder, pstmt);
        // param 1
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassThree.class));
        inOrder.verify(mappersFinder).findMapper(eq(1), same(paramInfo1), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(1), eq(123));
        // param 2
        inOrder.verify(reflectionsFinder).findGetter(eq("classTwo"), eq(SomeClassThree.class));
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassTwo.class));
        inOrder.verify(mappersFinder).findMapper(eq(2), same(paramInfo2), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(2), eq(-123));
        // param 3
        // extractor for classTwo is cashed already - so no call to reflectionsFinder
        inOrder.verify(reflectionsFinder).findGetter(eq("classOne"), eq(SomeClassTwo.class));
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassOne.class));
        inOrder.verify(mappersFinder).findMapper(eq(3), same(paramInfo3), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(3), eq(321));

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, pstmt);
        verifyNoInteractions(getterTypeInt);

        // test once more to verify all extractors are cached
        injector.injectParameters(pstmt, new SomeClassThree(333, new SomeClassTwo(222, new SomeClassOne(111, null, null))));
        inOrder.verify(mappersFinder).findMapper(eq(1), same(paramInfo1), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(1), eq(333));
        inOrder.verify(mappersFinder).findMapper(eq(2), same(paramInfo2), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(2), eq(222));
        inOrder.verify(mappersFinder).findMapper(eq(3), same(paramInfo3), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(3), eq(111));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, pstmt, reflectionsFinder);
        verifyNoInteractions(getterTypeInt);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testInjectParameters_SomeClassThree_nullComplexProperty() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("intMapper", null, null, null);

        final ParameterGetter<Integer> getterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                getterTypeInt,
                new ParameterSetterImpl<>(
                        PreparedStatement::setInt,
                        (stmt, index) -> stmt.setNull(index, JDBCType.INTEGER.getVendorTypeNumber())
                )
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(anyInt(), anyInt());

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref1);
        when(reflectionsFinder.findGetter(eq("fieldInteger"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref1);
        when(reflectionsFinder.findGetter(eq("classOne"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref2);
        when(reflectionsFinder.findGetter(eq("classTwo"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref2);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                eq(Integer.class),
                same(pstmt)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findSQLType(
                eq(2),
                same(paramInfo2),
                same(pstmt)
        )).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        when(mappersFinder.findSQLType(
                eq(3),
                same(paramInfo3),
                same(pstmt)
        )).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());

        final NamedInjectorImpl<SomeClassThree> injector = new NamedInjectorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "classTwo.fieldInteger", paramInfo2),
                        NamedParameter.of(3, "classTwo.classOne.fieldInteger", paramInfo3)),
                SomeClassThree.class,
                List.empty()
        );

        // do test
        injector.injectParameters(pstmt, new SomeClassThree(123, null));

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, reflectionsFinder, pstmt);
        // param 1
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassThree.class));
        inOrder.verify(mappersFinder).findMapper(eq(1), same(paramInfo1), eq(Integer.class), same(pstmt));
        inOrder.verify(pstmt).setInt(eq(1), eq(123));
        // param 2
        inOrder.verify(reflectionsFinder).findGetter(eq("classTwo"), eq(SomeClassThree.class));
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassTwo.class));
        inOrder.verify(mappersFinder).findSQLType(eq(2), same(paramInfo2), same(pstmt));
        inOrder.verify(pstmt).setNull(eq(2), eq(JDBCType.INTEGER.getVendorTypeNumber()));
        // param 3
        inOrder.verify(reflectionsFinder).findGetter(eq("classOne"), eq(SomeClassTwo.class));
        inOrder.verify(reflectionsFinder).findGetter(eq("fieldInteger"), eq(SomeClassOne.class));
        inOrder.verify(mappersFinder).findSQLType(eq(3), same(paramInfo3), same(pstmt));
        inOrder.verify(pstmt).setNull(eq(3), eq(JDBCType.INTEGER.getVendorTypeNumber()));

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, pstmt);
        verifyNoInteractions(getterTypeInt);
    }

    @Test
    public void testInjectParameters_providedExtractors() throws SQLException {
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

        final PreparedStatement pstmt = mock();
        doNothing().when(pstmt).setInt(anyInt(), anyInt());
        doNothing().when(pstmt).setString(anyInt(), any());

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();

        final PropertyExtractor<SomeClassOne, ?> pe1 =
                PropertyExtractor.of(SomeClassOne.class, Integer.class, "fieldInteger", o -> 112233);
        final PropertyExtractor<SomeClassOne, ?> pe2 =
                PropertyExtractor.of(SomeClassOne.class, String.class, "fieldStringOne", o -> "some other string");
        final PropertyExtractor<SomeClassOne, ?> pe3 =
                PropertyExtractor.of(SomeClassOne.class, String.class, "fieldStringTwo", o -> null);

        final NamedInjectorImpl<SomeClassOne> injector = new NamedInjectorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", mapperInt),
                        NamedParameter.of(2, "fieldStringOne", mapperString),
                        NamedParameter.of(3, "fieldStringTwo", mapperString)),
                SomeClassOne.class,
                List.of(pe1, pe2, pe3)
        );

        // do test
        injector.injectParameters(pstmt, new SomeClassOne(123, "some string", "not null"));

        // verify mocks
        final InOrder inOrder = inOrder(mappersFinder, reflectionsFinder, pstmt);
        // param 1
        inOrder.verify(pstmt).setInt(eq(1), eq(112233));
        // param 2
        inOrder.verify(pstmt).setString(eq(2), eq("some other string"));
        // param 3
        inOrder.verify(pstmt).setString(eq(3), isNull());

        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mappersFinder, pstmt);
        verifyNoInteractions(reflectionsFinder, getterTypeInt, getterTypeString);
    }

    private static class SomeClassOne {
        private Integer fieldInteger;
        private String fieldStringOne;
        private String fieldStringTwo;

        public SomeClassOne(Integer fieldInteger, String fieldStringOne, String fieldStringTwo) {
            this.fieldInteger = fieldInteger;
            this.fieldStringOne = fieldStringOne;
            this.fieldStringTwo = fieldStringTwo;
        }
    }

    private static class SomeClassTwo {
        private Integer fieldInteger;
        private SomeClassOne classOne;

        public SomeClassTwo(Integer fieldInteger, SomeClassOne classOne) {
            this.fieldInteger = fieldInteger;
            this.classOne = classOne;
        }

        public Integer getFieldInteger() {
            return fieldInteger;
        }

        public SomeClassOne getClassOne() {
            return classOne;
        }
    }

    private static class SomeClassThree {
        private Integer fieldInteger;
        private SomeClassTwo classTwo;

        public SomeClassThree(Integer fieldInteger, SomeClassTwo classTwo) {
            this.fieldInteger = fieldInteger;
            this.classTwo = classTwo;
        }

        public Integer getFieldInteger() {
            return fieldInteger;
        }

        public SomeClassTwo getClassTwo() {
            return classTwo;
        }
    }

}
