package simple.orm.loader.impl.builder;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.loader.QueryParser;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.indexed.IndexedExtractorImpl;
import simple.orm.mapping.indexed.IndexedInjectorImpl;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.named.NamedExtractorImpl;
import simple.orm.mapping.named.NamedInjectorImpl;
import simple.orm.mapping.named.NamedParameter;
import simple.orm.mapping.param.ParamInfo;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link QueryInjectorExtractorBuilder}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryInjectorExtractorBuilderTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testIndexedInjector() throws Exception {
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ParameterJdbcType mockJdbcType = mock();

        when(mockTypesCollection.findType(anyString())).thenReturn(mockJdbcType);

        QueryInjectorExtractorBuilder qieBuilder = QueryInjectorExtractorBuilder.getInstance();
        IndexedInjector injector = qieBuilder.buildIndexedInjector(
                mockTypesCollection,
                mockMappersFinder,
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, "mapper", "tag", "jdbcType", "java.lang.String"),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, null, "mapper", "tag", null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 3, null, false, null, null, null, null, null)
                )
        );

        assertThat(injector).isInstanceOf(IndexedInjectorImpl.class);
        verify(mockTypesCollection).findType(eq("jdbcType"));
        verifyNoMoreInteractions(mockTypesCollection);
        verifyNoInteractions(mockMappersFinder, mockJdbcType);

        {
            Field field = injector.getClass().getDeclaredField("parameters");
            field.trySetAccessible();
            Seq<IndexedParameter> parameters = (Seq<IndexedParameter>) field.get(injector);
            assertThat(parameters).containsExactly(
                    IndexedParameter.of(1, ParamInfo.of("mapper", mockJdbcType, String.class, "tag")),
                    IndexedParameter.of(2, ParamInfo.of("mapper", null, null, "tag")),
                    IndexedParameter.of(3, ParamInfo.of(null, null, null, null))
            );
        }
        {
            Field field = injector.getClass().getDeclaredField("mappersFinder");
            field.trySetAccessible();
            MappersFinder mappersFinder = (MappersFinder) field.get(injector);
            assertThat(mappersFinder).isSameAs(mockMappersFinder);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testIndexedExtractor() throws Exception {
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ParameterJdbcType mockJdbcType = mock();

        when(mockTypesCollection.findType(anyString())).thenReturn(mockJdbcType);

        QueryInjectorExtractorBuilder qieBuilder = QueryInjectorExtractorBuilder.getInstance();
        IndexedExtractor extractor = qieBuilder.buildIndexedExtractor(
                mockTypesCollection,
                mockMappersFinder,
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, "mapper", "tag", "jdbcType", "java.lang.String"),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 2, null, false, null, "mapper", "tag", null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 3, null, false, null, null, null, null, null)
                )
        );

        assertThat(extractor).isInstanceOf(IndexedExtractorImpl.class);
        verify(mockTypesCollection).findType(eq("jdbcType"));
        verifyNoMoreInteractions(mockTypesCollection);
        verifyNoInteractions(mockMappersFinder, mockJdbcType);

        {
            Field field = extractor.getClass().getDeclaredField("parameters");
            field.trySetAccessible();
            Seq<IndexedParameter> parameters = (Seq<IndexedParameter>) field.get(extractor);
            assertThat(parameters).containsExactly(
                    IndexedParameter.of(1, ParamInfo.of("mapper", mockJdbcType, String.class, "tag")),
                    IndexedParameter.of(2, ParamInfo.of("mapper", null, null, "tag")),
                    IndexedParameter.of(3, ParamInfo.of(null, null, null, null))
            );
        }
        {
            Field field = extractor.getClass().getDeclaredField("mappersFinder");
            field.trySetAccessible();
            MappersFinder mappersFinder = (MappersFinder) field.get(extractor);
            assertThat(mappersFinder).isSameAs(mockMappersFinder);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testNamedInjector() throws Exception {
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionsFinder = mock();
        final ParameterJdbcType mockJdbcType = mock();

        when(mockTypesCollection.findType(anyString())).thenReturn(mockJdbcType);

        QueryInjectorExtractorBuilder qieBuilder = QueryInjectorExtractorBuilder.getInstance();
        NamedInjector injector = qieBuilder.buildNamedInjector(
                mockTypesCollection,
                mockMappersFinder,
                mockReflectionsFinder,
                SomeComplexObject.class,
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, "prop1", "mapper", "tag", "jdbcType", "java.lang.String"),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, "prop2", "mapper", "tag", null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 3, null, false, "prop3", null, null, null, null)
                )
        );

        assertThat(injector).isInstanceOf(NamedInjectorImpl.class);
        verify(mockTypesCollection).findType(eq("jdbcType"));
        verifyNoMoreInteractions(mockTypesCollection);
        verifyNoInteractions(mockMappersFinder, mockReflectionsFinder, mockJdbcType);

        {
            Field field = injector.getClass().getSuperclass().getDeclaredField("parameters");
            field.trySetAccessible();
            Seq<NamedParameter> parameters = (Seq<NamedParameter>) field.get(injector);
            assertThat(parameters).containsExactly(
                    NamedParameter.of("prop1", 1, ParamInfo.of("mapper", mockJdbcType, String.class, "tag")),
                    NamedParameter.of("prop2", 2, ParamInfo.of("mapper", null, null, "tag")),
                    NamedParameter.of("prop3", 3, ParamInfo.of(null, null, null, null))
            );
        }
        {
            Field field = injector.getClass().getSuperclass().getDeclaredField("sourceClass");
            field.trySetAccessible();
            Class<?> sourceClass = (Class<?>) field.get(injector);
            assertThat(sourceClass).isSameAs(SomeComplexObject.class);
        }
        {
            Field field = injector.getClass().getSuperclass().getDeclaredField("mappersFinder");
            field.trySetAccessible();
            MappersFinder mappersFinder = (MappersFinder) field.get(injector);
            assertThat(mappersFinder).isSameAs(mockMappersFinder);
        }
        {
            Field field = injector.getClass().getDeclaredField("reflectionsFinder");
            field.trySetAccessible();
            ReflectionsFinder reflectionsFinder = (ReflectionsFinder) field.get(injector);
            assertThat(reflectionsFinder).isSameAs(mockReflectionsFinder);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testNamedExtractor() throws Exception {
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionsFinder = mock();
        final ParameterJdbcType mockJdbcType = mock();

        when(mockTypesCollection.findType(anyString())).thenReturn(mockJdbcType);

        QueryInjectorExtractorBuilder qieBuilder = QueryInjectorExtractorBuilder.getInstance();
        NamedExtractor extractor = qieBuilder.buildNamedExtractor(
                mockTypesCollection,
                mockMappersFinder,
                mockReflectionsFinder,
                SomeComplexObject.class,
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, "label1", false, "prop1", "mapper", "tag", "jdbcType", "java.lang.String"),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 2, "label2", false, "prop2", "mapper", "tag", null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 3, "label3", false, "prop3", null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 4, "label4", false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 5, null, false, "prop5", null, null, null, null)
                )
        );

        assertThat(extractor).isInstanceOf(NamedExtractorImpl.class);
        verify(mockTypesCollection).findType(eq("jdbcType"));
        verifyNoMoreInteractions(mockTypesCollection);
        verifyNoInteractions(mockMappersFinder, mockReflectionsFinder, mockJdbcType);

        {
            Field field = extractor.getClass().getSuperclass().getDeclaredField("parameters");
            field.trySetAccessible();
            Seq<NamedParameter> parameters = (Seq<NamedParameter>) field.get(extractor);
            assertThat(parameters).containsExactly(
                    NamedParameter.of("prop1", "label1", ParamInfo.of("mapper", mockJdbcType, String.class, "tag")),
                    NamedParameter.of("prop2", "label2", ParamInfo.of("mapper", null, null, "tag")),
                    NamedParameter.of("prop3", "label3", ParamInfo.of(null, null, null, null)),
                    NamedParameter.of("label4", "label4", ParamInfo.of(null, null, null, null)),
                    NamedParameter.of("prop5", 5, ParamInfo.of(null, null, null, null))
            );
        }
        {
            Field field = extractor.getClass().getSuperclass().getDeclaredField("targetClass");
            field.trySetAccessible();
            Class<?> targetClass = (Class<?>) field.get(extractor);
            assertThat(targetClass).isSameAs(SomeComplexObject.class);
        }
        {
            Field field = extractor.getClass().getSuperclass().getDeclaredField("mappersFinder");
            field.trySetAccessible();
            MappersFinder mappersFinder = (MappersFinder) field.get(extractor);
            assertThat(mappersFinder).isSameAs(mockMappersFinder);
        }
        {
            Field field = extractor.getClass().getDeclaredField("reflectionsFinder");
            field.trySetAccessible();
            ReflectionsFinder reflectionsFinder = (ReflectionsFinder) field.get(extractor);
            assertThat(reflectionsFinder).isSameAs(mockReflectionsFinder);
        }
    }

    private static class SomeComplexObject {
        // just to use something as class in named injector/extractor
        // not some nonsense like Object.class
    }

}
