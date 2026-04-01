package simple.orm.mapping.named;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.named.ObjectConstructor.CreatedObject;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterGetterImpl;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetter;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static simple.orm.mapping.param.ParameterGetterImpl.wrapCheckWasNull;

/**
 * {@link NamedExtractorImpl} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NamedExtractorImplTest {

    private Constructor<SomeClassOne> someClassOne_con;
    private Constructor<SomeClassTwo> someClassTwo_con;
    private Constructor<SomeClassThree> someClassThree_con;
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
            someClassOne_con = SomeClassOne.class.getConstructor();
            someClassTwo_con = SomeClassTwo.class.getConstructor();
            someClassThree_con = SomeClassThree.class.getConstructor();
            someClassOne_con.trySetAccessible();
            someClassTwo_con.trySetAccessible();
            someClassThree_con.trySetAccessible();
        }
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
            someClassTwo_ref1 = Either.left(sctClass.getDeclaredMethod("setFieldInteger", Integer.class));
            someClassTwo_ref2 = Either.left(sctClass.getDeclaredMethod("setClassOne", SomeClassOne.class));
            someClassTwo_ref1.getLeft().trySetAccessible();
            someClassTwo_ref2.getLeft().trySetAccessible();
        }
        {
            final Class<SomeClassThree> sctClass = SomeClassThree.class;
            someClassThree_ref1 = Either.left(sctClass.getDeclaredMethod("setFieldInteger", Integer.class));
            someClassThree_ref2 = Either.left(sctClass.getDeclaredMethod("setClassTwo", SomeClassTwo.class));
            someClassThree_ref1.getLeft().trySetAccessible();
            someClassThree_ref2.getLeft().trySetAccessible();
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow_SomeClassOne_mapperProvided() throws SQLException {
        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final ParameterSetter<String> setterTypeString = mock();
        final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class,
                new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
                setterTypeString
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<String, String> mapperString = new SimpleTypeMapper<>(typeString, String.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getString(eq(2))).thenReturn("some string");
        when(rs.getString(eq(3))).thenReturn(null);

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassOne.class))).thenReturn((Constructor) someClassOne_con);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findSetter(eq("fieldStringOne"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref2);
        when(reflectionsFinder.findSetter(eq("fieldStringTwo"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref3);

        final NamedExtractorImpl<SomeClassOne> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", mapperInt),
                        NamedParameter.of(2, "fieldStringOne", mapperString),
                        NamedParameter.of(3, "fieldStringTwo", mapperString)),
                SomeClassOne.class,
                List.empty(),
                List.empty()
        );

        // do test
        final SomeClassOne result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.fieldStringOne).isEqualTo("some string");
        assertThat(result.fieldStringTwo).isNull();

        // verify mocks, can't use InOrder cause props for injections are passed through HashMap
        // get all values
        verify(rs).getInt(eq(1));
        verify(rs).getString(eq(2));
        verify(rs).getString(eq(3));
        // construct object
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldStringOne"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldStringTwo"), eq(SomeClassOne.class));

        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs);
        verifyNoInteractions(setterTypeInt, setterTypeString);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow_SomeClassOne_paramInfoProvided() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("stringMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("stringMapper", null, null, null);

        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final ParameterSetter<String> setterTypeString = mock();
        final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class,
                new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
                setterTypeString
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<String, String> mapperString = new SimpleTypeMapper<>(typeString, String.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getString(eq(2))).thenReturn("some string");
        when(rs.getString(eq(3))).thenReturn(null);

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassOne.class))).thenReturn((Constructor) someClassOne_con);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findSetter(eq("fieldStringOne"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref2);
        when(reflectionsFinder.findSetter(eq("fieldStringTwo"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref3);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                same(rs)
        )).thenReturn((TypeMapper) mapperString);
        when(mappersFinder.findMapper(
                eq(3),
                same(paramInfo3),
                same(rs)
        )).thenReturn((TypeMapper) mapperString);

        final NamedExtractorImpl<SomeClassOne> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "fieldStringOne", paramInfo2),
                        NamedParameter.of(3, "fieldStringTwo", paramInfo3)),
                SomeClassOne.class,
                List.empty(),
                List.empty()
        );

        // do test
        final SomeClassOne result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.fieldStringOne).isEqualTo("some string");
        assertThat(result.fieldStringTwo).isNull();

        // verify mocks, can't use InOrder cause props for injections are passed through HashMap
        // get all values
        verify(rs).getInt(eq(1));
        verify(rs).getString(eq(2));
        verify(rs).getString(eq(3));
        verify(mappersFinder).findMapper(eq(1), same(paramInfo1), same(rs));
        verify(mappersFinder).findMapper(eq(2), same(paramInfo2), same(rs));
        verify(mappersFinder).findMapper(eq(3), same(paramInfo3), same(rs));
        // construct object
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldStringOne"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldStringTwo"), eq(SomeClassOne.class));

        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs);
        verifyNoInteractions(setterTypeInt, setterTypeString);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow_SomeClassThree_paramInfoProvided_complexProps_and_caching() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("intMapper", null, null, null);

        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getInt(eq(2))).thenReturn(-123);
        when(rs.getInt(eq(3))).thenReturn(321);

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassOne.class))).thenReturn((Constructor) someClassOne_con);
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassTwo.class))).thenReturn((Constructor) someClassTwo_con);
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassThree.class))).thenReturn((Constructor) someClassThree_con);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref1);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref1);
        when(reflectionsFinder.findSetter(eq("classOne"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref2);
        when(reflectionsFinder.findSetter(eq("classTwo"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref2);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(3),
                same(paramInfo3),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);

        final NamedExtractorImpl<SomeClassThree> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "classTwo.fieldInteger", paramInfo2),
                        NamedParameter.of(3, "classTwo.classOne.fieldInteger", paramInfo3)),
                SomeClassThree.class,
                List.empty(),
                List.empty()
        );

        // do test
        final SomeClassThree result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.classTwo.fieldInteger).isEqualTo(-123);
        assertThat(result.classTwo.classOne.fieldInteger).isEqualTo(321);
        assertThat(result.classTwo.classOne.fieldStringOne).isNull();
        assertThat(result.classTwo.classOne.fieldStringTwo).isNull();

        // verify mocks, can't use InOrder cause props for injections are passed through HashMap
        // get all values
        verify(rs).getInt(eq(1));
        verify(rs).getInt(eq(2));
        verify(rs).getInt(eq(3));
        verify(mappersFinder).findMapper(eq(1), same(paramInfo1), same(rs));
        verify(mappersFinder).findMapper(eq(2), same(paramInfo2), same(rs));
        verify(mappersFinder).findMapper(eq(3), same(paramInfo3), same(rs));
        // construct object
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassOne.class));
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassTwo.class));
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("classOne"), eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("classTwo"), eq(SomeClassThree.class));

        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs);
        verifyNoInteractions(setterTypeInt);

        // test once more to verify all constructors/injectors are cached
        when(rs.getInt(eq(1))).thenReturn(333);
        when(rs.getInt(eq(2))).thenReturn(222);
        when(rs.getInt(eq(3))).thenReturn(111);
        final SomeClassThree result2 = extractor.extractRow(rs);
        assertThat(result2).isNotNull();
        assertThat(result2.fieldInteger).isEqualTo(333);
        assertThat(result2.classTwo.fieldInteger).isEqualTo(222);
        assertThat(result2.classTwo.classOne.fieldInteger).isEqualTo(111);
        assertThat(result2.classTwo.classOne.fieldStringOne).isNull();
        assertThat(result2.classTwo.classOne.fieldStringTwo).isNull();
        verify(rs, times(2)).getInt(eq(1)); // +1
        verify(rs, times(2)).getInt(eq(2)); // +1
        verify(rs, times(2)).getInt(eq(3)); // +1
        verify(mappersFinder, times(2)).findMapper(eq(1), same(paramInfo1), same(rs)); // +1
        verify(mappersFinder, times(2)).findMapper(eq(2), same(paramInfo2), same(rs)); // +1
        verify(mappersFinder, times(2)).findMapper(eq(3), same(paramInfo3), same(rs)); // +1
        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs);
        verifyNoInteractions(setterTypeInt);
    }

    @Test
    public void testExtractRow_extractByLabel_providedConstructorsInjectors() throws SQLException {
        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final ParameterSetter<String> setterTypeString = mock();
        final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class,
                new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
                setterTypeString
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<String, String> mapperString = new SimpleTypeMapper<>(typeString, String.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq("int01"))).thenReturn(123);
        when(rs.getInt(eq("int02"))).thenReturn(321);
        when(rs.getInt(eq("int03"))).thenReturn(-321);
        when(rs.getString(eq("str01"))).thenReturn("some string");
        when(rs.getString(eq("str02"))).thenReturn("some other string");

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();

        final NamedExtractorImpl<SomeClassThree> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of("int01", "fieldInteger", mapperInt),
                        NamedParameter.of("int02", "classTwo.fieldInteger", mapperInt),
                        NamedParameter.of("int03", "classTwo.classOne.fieldInteger", mapperInt),
                        NamedParameter.of("str01", "classTwo.classOne.fieldStringOne", mapperString),
                        NamedParameter.of("str02", "classTwo.classOne.fieldStringTwo", mapperString)),
                SomeClassThree.class,
                List.of(
                        ObjectConstructor.of(SomeClassOne.class, props -> CreatedObject.of(new SomeClassOne())),
                        ObjectConstructor.of(SomeClassTwo.class, props -> CreatedObject.of(new SomeClassTwo())),
                        ObjectConstructor.of(SomeClassThree.class, props -> CreatedObject.of(new SomeClassThree()))
                ),
                List.of(
                        PropertyInjector.of(SomeClassOne.class, Integer.class, "fieldInteger", (sco, i) -> sco.fieldInteger = i),
                        PropertyInjector.of(SomeClassTwo.class, Integer.class, "fieldInteger", SomeClassTwo::setFieldInteger),
                        PropertyInjector.of(SomeClassThree.class, Integer.class, "fieldInteger", SomeClassThree::setFieldInteger),
                        PropertyInjector.of(SomeClassOne.class, String.class, "fieldStringOne", (sco, s) -> sco.fieldStringOne = s),
                        PropertyInjector.of(SomeClassOne.class, String.class, "fieldStringTwo", (sco, s) -> sco.fieldStringTwo = s),
                        PropertyInjector.of(SomeClassTwo.class, SomeClassOne.class, "classOne", SomeClassTwo::setClassOne),
                        PropertyInjector.of(SomeClassThree.class, SomeClassTwo.class, "classTwo", SomeClassThree::setClassTwo)
                )
        );

        // do test
        final SomeClassThree result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.classTwo.fieldInteger).isEqualTo(321);
        assertThat(result.classTwo.classOne.fieldInteger).isEqualTo(-321);
        assertThat(result.classTwo.classOne.fieldStringOne).isEqualTo("some string");
        assertThat(result.classTwo.classOne.fieldStringTwo).isEqualTo("some other string");

        // verify mocks (only ResultSet is used)
        verify(rs).getInt(eq("int01"));
        verify(rs).getInt(eq("int02"));
        verify(rs).getInt(eq("int03"));
        verify(rs).getString(eq("str01"));
        verify(rs).getString(eq("str02"));

        verifyNoMoreInteractions(rs);
        verifyNoInteractions(setterTypeInt, setterTypeString, mappersFinder, reflectionsFinder);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow_SomeClassThree_nullComplexProperty() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("intMapper", null, null, null);

        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getInt(eq(2))).thenReturn(0);
        when(rs.getInt(eq(3))).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassThree.class))).thenReturn((Constructor) someClassThree_con);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref1);
        when(reflectionsFinder.findSetter(eq("classTwo"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref2);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(3),
                same(paramInfo3),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);

        final NamedExtractorImpl<SomeClassThree> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "classTwo.fieldInteger", paramInfo2),
                        NamedParameter.of(3, "classTwo.classOne.fieldInteger", paramInfo3)),
                SomeClassThree.class,
                List.of(ObjectConstructor.of(SomeClassTwo.class, () -> null)),
                List.empty()
        );

        // do test
        final SomeClassThree result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.classTwo).isNull();

        // verify mocks, can't use InOrder cause props for injections are passed through HashMap
        // get all values
        verify(rs).getInt(eq(1));
        verify(rs).getInt(eq(2));
        verify(rs).getInt(eq(3));
        verify(rs, times(2)).wasNull();
        verify(mappersFinder).findMapper(eq(1), same(paramInfo1), same(rs));
        verify(mappersFinder).findMapper(eq(2), same(paramInfo2), same(rs));
        verify(mappersFinder).findMapper(eq(3), same(paramInfo3), same(rs));
        // construct object
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("classTwo"), eq(SomeClassThree.class));

        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs);
        verifyNoInteractions(setterTypeInt);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow_SomeClassThree_paramInfoProvided_propsConsumed() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of("intMapper", null, null, null);
        final ParamInfo<Object, Object> paramInfo4 = ParamInfo.of("intMapper", null, null, null);

        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getInt(eq(2))).thenReturn(-123);
        when(rs.getInt(eq(3))).thenReturn(321);
        when(rs.getInt(eq(4))).thenReturn(666);

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassOne.class))).thenReturn((Constructor) someClassOne_con);
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassTwo.class))).thenReturn((Constructor) someClassTwo_con);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref1);
        when(reflectionsFinder.findSetter(eq("classOne"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref2);
        when(reflectionsFinder.findSetter(eq("classTwo"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref2);
        when(mappersFinder.findMapper(
                eq(1),
                same(paramInfo1),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                same(paramInfo2),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(3),
                same(paramInfo3),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(4),
                same(paramInfo4),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);

        final NamedExtractorImpl<SomeClassThree> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "classTwo.fieldInteger", paramInfo2),
                        NamedParameter.of(3, "classTwo.classOne.fieldInteger", paramInfo3),
                        NamedParameter.of(4, "numberOfTheBeast", paramInfo4)),
                SomeClassThree.class,
                List.of(ObjectConstructor.of(
                        SomeClassThree.class,
                        props -> CreatedObject.of(
                                new SomeClassThree((Integer) props.get("fieldInteger").getOrNull()),
                                List.of("numberOfTheBeast", "fieldInteger")
                        )
                )),
                List.empty()
        );

        // do test
        final SomeClassThree result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.classTwo.fieldInteger).isEqualTo(-123);
        assertThat(result.classTwo.classOne.fieldInteger).isEqualTo(321);
        assertThat(result.classTwo.classOne.fieldStringOne).isNull();
        assertThat(result.classTwo.classOne.fieldStringTwo).isNull();

        // verify mocks, can't use InOrder cause props for injections are passed through HashMap
        // get all values
        verify(rs).getInt(eq(1));
        verify(rs).getInt(eq(2));
        verify(rs).getInt(eq(3));
        verify(rs).getInt(eq(4));
        verify(mappersFinder).findMapper(eq(1), same(paramInfo1), same(rs));
        verify(mappersFinder).findMapper(eq(2), same(paramInfo2), same(rs));
        verify(mappersFinder).findMapper(eq(3), same(paramInfo3), same(rs));
        verify(mappersFinder).findMapper(eq(4), same(paramInfo4), same(rs));
        // construct object
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassOne.class));
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("classOne"), eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("classTwo"), eq(SomeClassThree.class));

        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs);
        verifyNoInteractions(setterTypeInt);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testExtractRow_SomeClassThree_paramInfoEmpty_classDerivedReflections() throws SQLException {
        final ParamInfo<Object, Object> paramInfo1 = ParamInfo.of(null, null, null, null);
        final ParamInfo<Object, Object> paramInfo2 = ParamInfo.of(null, null, null, null);
        final ParamInfo<Object, Object> paramInfo3 = ParamInfo.of(null, null, null, null);
        final ParamInfo<Object, Object> paramInfo4 = ParamInfo.of(null, null, null, null);
        final ParamInfo<Object, Object> paramInfo5 = ParamInfo.of(null, null, null, null);
        final ParamInfo<Object, Integer> derivedParamInfo1 = ParamInfo.of(null, null, Integer.class, null);
        final ParamInfo<Object, Integer> derivedParamInfo2 = ParamInfo.of(null, null, Integer.class, null);
        final ParamInfo<Object, Integer> derivedParamInfo3 = ParamInfo.of(null, null, Integer.class, null);
        final ParamInfo<Object, String> derivedParamInfo4 = ParamInfo.of(null, null, String.class, null);
        final ParamInfo<Object, String> derivedParamInfo5 = ParamInfo.of(null, null, String.class, null);

        final ParameterSetter<Integer> setterTypeInt = mock();
        final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class,
                new ParameterGetterImpl<>(
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                        (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0)
                ),
                setterTypeInt
        );
        final ParameterSetter<String> setterTypeString = mock();
        final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class,
                new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
                setterTypeString
        );
        final TypeMapper<Integer, Integer> mapperInt = new SimpleTypeMapper<>(typeInt, Integer.class, Function1.identity(), Function1.identity());
        final TypeMapper<String, String> mapperString = new SimpleTypeMapper<>(typeString, String.class, Function1.identity(), Function1.identity());

        final ResultSet rs = mock();
        when(rs.getInt(eq(1))).thenReturn(123);
        when(rs.getInt(eq(2))).thenReturn(-123);
        when(rs.getInt(eq(3))).thenReturn(321);
        when(rs.getString(eq(4))).thenReturn("ONE");
        when(rs.getString(eq(5))).thenReturn("TWO");
        final ResultSetMetaData rsMeta = mock();
        when(rs.getMetaData()).thenReturn(rsMeta);
        when(rsMeta.getColumnType(eq(1))).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        when(rsMeta.getColumnType(eq(2))).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        when(rsMeta.getColumnType(eq(3))).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        when(rsMeta.getColumnType(eq(4))).thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        when(rsMeta.getColumnType(eq(5))).thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());

        final MappersFinder mappersFinder = mock();
        final ReflectionsFinder reflectionsFinder = mock();
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassOne.class))).thenReturn((Constructor) someClassOne_con);
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassTwo.class))).thenReturn((Constructor) someClassTwo_con);
        when(reflectionsFinder.findDefaultConstructor(eq(SomeClassThree.class))).thenReturn((Constructor) someClassThree_con);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref1);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref1);
        when(reflectionsFinder.findSetter(eq("fieldInteger"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref1);
        when(reflectionsFinder.findSetter(eq("classOne"), eq(SomeClassTwo.class))).thenReturn(someClassTwo_ref2);
        when(reflectionsFinder.findSetter(eq("classTwo"), eq(SomeClassThree.class))).thenReturn(someClassThree_ref2);
        when(reflectionsFinder.findSetter(eq("fieldStringOne"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref2);
        when(reflectionsFinder.findSetter(eq("fieldStringTwo"), eq(SomeClassOne.class))).thenReturn(someClassOne_ref3);
        when(mappersFinder.findMapper(
                eq(1),
                eq(derivedParamInfo1),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(2),
                eq(derivedParamInfo2),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(3),
                eq(derivedParamInfo3),
                same(rs)
        )).thenReturn((TypeMapper) mapperInt);
        when(mappersFinder.findMapper(
                eq(4),
                eq(derivedParamInfo4),
                same(rs)
        )).thenReturn((TypeMapper) mapperString);
        when(mappersFinder.findMapper(
                eq(5),
                eq(derivedParamInfo5),
                same(rs)
        )).thenReturn((TypeMapper) mapperString);

        final NamedExtractorImpl<SomeClassThree> extractor = new NamedExtractorImpl<>(
                mappersFinder,
                reflectionsFinder,
                List.of(NamedParameter.of(1, "fieldInteger", paramInfo1),
                        NamedParameter.of(2, "classTwo.fieldInteger", paramInfo2),
                        NamedParameter.of(3, "classTwo.classOne.fieldInteger", paramInfo3),
                        NamedParameter.of(4, "classTwo.classOne.fieldStringOne", paramInfo4),
                        NamedParameter.of(5, "classTwo.classOne.fieldStringTwo", paramInfo5)),
                SomeClassThree.class,
                List.empty(),
                List.empty()
        );

        // do test
        final SomeClassThree result = extractor.extractRow(rs);
        assertThat(result).isNotNull();
        assertThat(result.fieldInteger).isEqualTo(123);
        assertThat(result.classTwo.fieldInteger).isEqualTo(-123);
        assertThat(result.classTwo.classOne.fieldInteger).isEqualTo(321);
        assertThat(result.classTwo.classOne.fieldStringOne).isEqualTo("ONE");
        assertThat(result.classTwo.classOne.fieldStringTwo).isEqualTo("TWO");

        // verify mocks, can't use InOrder cause props for injections are passed through HashMap
        // get all values
        verify(rs).getInt(eq(1));
        verify(rs).getInt(eq(2));
        verify(rs).getInt(eq(3));
        verify(rs).getString(eq(4));
        verify(rs).getString(eq(5));
        verify(mappersFinder).findMapper(eq(1), eq(derivedParamInfo1), same(rs));
        verify(mappersFinder).findMapper(eq(2), eq(derivedParamInfo2), same(rs));
        verify(mappersFinder).findMapper(eq(3), eq(derivedParamInfo3), same(rs));
        verify(mappersFinder).findMapper(eq(4), eq(derivedParamInfo4), same(rs));
        verify(mappersFinder).findMapper(eq(5), eq(derivedParamInfo5), same(rs));
        // construct object
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassOne.class));
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassTwo.class));
        verify(reflectionsFinder).findDefaultConstructor(eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("fieldInteger"), eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("classOne"), eq(SomeClassTwo.class));
        verify(reflectionsFinder).findSetter(eq("classTwo"), eq(SomeClassThree.class));
        verify(reflectionsFinder).findSetter(eq("fieldStringOne"), eq(SomeClassOne.class));
        verify(reflectionsFinder).findSetter(eq("fieldStringTwo"), eq(SomeClassOne.class));

        verifyNoMoreInteractions(mappersFinder, reflectionsFinder, rs, rsMeta);
        verifyNoInteractions(setterTypeInt, setterTypeString);
    }

    private static class SomeClassOne {
        private Integer fieldInteger;
        private String fieldStringOne;
        private String fieldStringTwo;

        public SomeClassOne() {
        }
    }

    private static class SomeClassTwo {
        private Integer fieldInteger;
        private SomeClassOne classOne;

        public SomeClassTwo() {
        }

        public void setFieldInteger(Integer fieldInteger) {
            this.fieldInteger = fieldInteger;
        }

        public void setClassOne(SomeClassOne classOne) {
            this.classOne = classOne;
        }
    }

    private static class SomeClassThree {
        private Integer fieldInteger;
        private SomeClassTwo classTwo;

        public SomeClassThree() {
        }

        public SomeClassThree(Integer fieldInteger) {
            this.fieldInteger = fieldInteger;
        }

        public void setFieldInteger(Integer fieldInteger) {
            this.fieldInteger = fieldInteger;
        }

        public void setClassTwo(SomeClassTwo classTwo) {
            this.classTwo = classTwo;
        }
    }

}
