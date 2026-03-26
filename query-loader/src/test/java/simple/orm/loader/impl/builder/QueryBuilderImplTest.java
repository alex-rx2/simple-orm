package simple.orm.loader.impl.builder;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryFactory;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.TypesCollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static simple.orm.loader.builder.ParameterType.*;

/**
 * Tests on {@link QueryBuilderImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryBuilderImplTest {

    @Test
    public void testDDL() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Void> extStrat = ExtractionStrategy.noneDdl();
        final List<QueryParameter> params = List.empty();
        final Query<Void, Void> factoryQuery = mock();
        // behaviour
        when(mockQFactory.ddlQuery(anyString(), anyInt())).thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Void> query =
                queryBuilder.buildQuery(QueryType.DDL, "this is SQL", injStrat, extStrat, params, 111);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateDDL(same(injStrat), same(extStrat), same(params));
        verify(mockQFactory).ddlQuery(eq("this is SQL"), eq(111));
        verifyNoMoreInteractions(mockQFactory, mockQValidator);
        verifyNoInteractions(mockQIEBuilder, mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    public void testDDL_queryTimeout() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Void> extStrat = ExtractionStrategy.noneDdl();
        final List<QueryParameter> params = List.empty();
        final Query<Void, Void> factoryQuery = mock();
        // behaviour
        when(mockQFactory.ddlQuery(anyString(), anyInt())).thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Void> query =
                queryBuilder.buildQuery(QueryType.DDL, "this is SQL", injStrat, extStrat, params, 123);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateDDL(same(injStrat), same(extStrat), same(params));
        verify(mockQFactory).ddlQuery(eq("this is SQL"), eq(123));
        verifyNoMoreInteractions(mockQFactory, mockQValidator);
        verifyNoInteractions(mockQIEBuilder, mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    public void testDML_noParams() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Integer> extStrat = ExtractionStrategy.noneDml();
        final List<QueryParameter> params = List.empty();
        final Query<Void, Integer> factoryQuery = mock();
        // behaviour
        when(mockQFactory.iudQueryWithoutParameters(anyString(), anyInt())).thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Integer> query =
                queryBuilder.buildQuery(QueryType.DML, "this is SQL", injStrat, extStrat, params, 0);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateDML(same(injStrat), same(extStrat), same(params));
        verify(mockQFactory).iudQueryWithoutParameters(eq("this is SQL"), eq(0));
        verifyNoMoreInteractions(mockQFactory, mockQValidator);
        verifyNoInteractions(mockQIEBuilder, mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    public void testDML_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Seq<Object>> injStrat = InjectionStrategy.indexed();
        final ExtractionStrategy<Integer> extStrat = ExtractionStrategy.noneDml();
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final IndexedInjector builtInjector = mock();
        final Query<Seq<Object>, Integer> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.buildIndexedInjector(any(), any(), any())).thenReturn(builtInjector);
        when(mockQFactory.iudQuery(anyString(), anyInt(), any(IndexedInjector.class))).thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Seq<Object>, Integer> query =
                queryBuilder.buildQuery(QueryType.DML, "this is SQL", injStrat, extStrat, params, 321);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateDML(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildIndexedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(params.filter(qp -> qp.type() == INJECTION))
        );
        verify(mockQFactory).iudQuery(eq("this is SQL"), eq(321), same(builtInjector));
        verifyNoMoreInteractions(mockQFactory, mockQValidator, mockQIEBuilder);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDML_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<SomeComplexObject> injStrat = InjectionStrategy.named(SomeComplexObject.class);
        final ExtractionStrategy<Integer> extStrat = ExtractionStrategy.noneDml();
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final NamedInjector<SomeComplexObject> builtInjector = mock();
        final Query<SomeComplexObject, Integer> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.<SomeComplexObject>buildNamedInjector(any(), any(), any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQFactory.iudQuery(anyString(), anyInt(), any(NamedInjector.class))).thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<SomeComplexObject, Integer> query =
                queryBuilder.buildQuery(QueryType.DML, "this is SQL", injStrat, extStrat, params, 321);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateDML(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildNamedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(params.filter(qp -> qp.type() == INJECTION))
        );
        verify(mockQFactory).iudQuery(eq("this is SQL"), eq(321), same(builtInjector));
        verifyNoMoreInteractions(mockQFactory, mockQValidator, mockQIEBuilder);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    public void testSelect_indexed_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Seq<Object>> injStrat = InjectionStrategy.indexed();
        final ExtractionStrategy<Seq<Object>> extStrat = ExtractionStrategy.indexed();
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final IndexedInjector builtInjector = mock();
        final IndexedExtractor builtExtractor = mock();
        final Query<Seq<Object>, Seq<Object>> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.buildIndexedInjector(any(), any(), any())).thenReturn(builtInjector);
        when(mockQIEBuilder.buildIndexedExtractor(any(), any(), any())).thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(IndexedInjector.class), any(IndexedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Seq<Object>, Seq<Object>> query =
                queryBuilder.buildQuery(QueryType.SELECT, "this is SQL", injStrat, extStrat, params, 0);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildIndexedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(params.filter(qp -> qp.type() == INJECTION))
        );
        verify(mockQIEBuilder).buildIndexedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(params.filter(qp -> qp.type() == EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    public void testSelect_none_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Seq<Object>> extStrat = ExtractionStrategy.indexed();
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final IndexedExtractor builtExtractor = mock();
        final Query<Void, Seq<Object>> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.buildIndexedExtractor(any(), any(), any())).thenReturn(builtExtractor);
        when(mockQFactory.selectQueryWithoutParameters(anyString(), anyInt(), any(IndexedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Seq<Object>> query =
                queryBuilder.buildQuery(QueryType.SELECT, "this is SQL", injStrat, extStrat, params, 111);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildIndexedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(params.filter(qp -> qp.type() == EXTRACTION))
        );
        verify(mockQFactory).selectQueryWithoutParameters(eq("this is SQL"), eq(111), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_named_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<SomeComplexObject> injStrat = InjectionStrategy.named(SomeComplexObject.class);
        final ExtractionStrategy<SomeComplexObject> extStrat = ExtractionStrategy.named(SomeComplexObject.class);
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final NamedInjector<SomeComplexObject> builtInjector = mock();
        final NamedExtractor<SomeComplexObject> builtExtractor = mock();
        final Query<SomeComplexObject, SomeComplexObject> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.<SomeComplexObject>buildNamedInjector(any(), any(), any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedExtractor(any(), any(), any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(NamedInjector.class), any(NamedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<SomeComplexObject, SomeComplexObject> query =
                queryBuilder.buildQuery(QueryType.SELECT, "this is SQL", injStrat, extStrat, params, 0);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildNamedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(params.filter(qp -> qp.type() == INJECTION))
        );
        verify(mockQIEBuilder).buildNamedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(params.filter(qp -> qp.type() == EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_none_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<SomeComplexObject> extStrat = ExtractionStrategy.named(SomeComplexObject.class);
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final NamedExtractor<SomeComplexObject> builtExtractor = mock();
        final Query<Void, SomeComplexObject> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.<SomeComplexObject>buildNamedExtractor(any(), any(), any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQueryWithoutParameters(anyString(), anyInt(), any(NamedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, SomeComplexObject> query =
                queryBuilder.buildQuery(QueryType.SELECT, "this is SQL", injStrat, extStrat, params, 0);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildNamedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(params.filter(qp -> qp.type() == EXTRACTION))
        );
        verify(mockQFactory).selectQueryWithoutParameters(eq("this is SQL"), eq(0), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_indexed_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<Seq<Object>> injStrat = InjectionStrategy.indexed();
        final ExtractionStrategy<SomeComplexObject> extStrat = ExtractionStrategy.named(SomeComplexObject.class);
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final IndexedInjector builtInjector = mock();
        final NamedExtractor<SomeComplexObject> builtExtractor = mock();
        final Query<Seq<Object>, SomeComplexObject> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.buildIndexedInjector(any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedExtractor(any(), any(), any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(IndexedInjector.class), any(NamedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Seq<Object>, SomeComplexObject> query =
                queryBuilder.buildQuery(QueryType.SELECT, "this is SQL", injStrat, extStrat, params, 0);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildIndexedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(params.filter(qp -> qp.type() == INJECTION))
        );
        verify(mockQIEBuilder).buildNamedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(params.filter(qp -> qp.type() == EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_named_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        // data
        final InjectionStrategy<SomeComplexObject> injStrat = InjectionStrategy.named(SomeComplexObject.class);
        final ExtractionStrategy<Seq<Object>> extStrat = ExtractionStrategy.indexed();
        final List<QueryParameter> params =
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                );
        final NamedInjector<SomeComplexObject> builtInjector = mock();
        final IndexedExtractor builtExtractor = mock();
        final Query<SomeComplexObject, Seq<Object>> factoryQuery = mock();
        // behaviour
        when(mockQIEBuilder.<SomeComplexObject>buildNamedInjector(any(), any(), any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQIEBuilder.buildIndexedExtractor(any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(NamedInjector.class), any(IndexedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryBuilderImpl queryBuilder = new QueryBuilderImpl(
                mockQFactory, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<SomeComplexObject, Seq<Object>> query =
                queryBuilder.buildQuery(QueryType.SELECT, "this is SQL", injStrat, extStrat, params, 111);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(params));
        verify(mockQIEBuilder).buildNamedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(params.filter(qp -> qp.type() == INJECTION))
        );
        verify(mockQIEBuilder).buildIndexedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(params.filter(qp -> qp.type() == EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(111), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, factoryQuery);
    }

    private static class SomeComplexObject {
        // just to use something as class in named strategies
        // not some nonsense like Object.class
    }

}
