package simple.orm.loader.impl.loader;

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
import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.param.TypesCollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link QueryLoaderImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryLoaderImplTest {

    @Test
    public void testDDL() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Void> extStrat = ExtractionStrategy.noneDdl();
        final List<QueryParser.QueryParam> parsedParams = List.empty();
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final Query<Void, Void> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQFactory.ddlQuery(anyString(), anyInt())).thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Void> query = queryLoader.loadQuery(QueryType.DDL, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateDDL(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQFactory).ddlQuery(eq("this is SQL"), eq(0)); // no timeout by default
        verifyNoMoreInteractions(mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockQIEBuilder, mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    public void testDDL_queryTimeout() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Void> extStrat = ExtractionStrategy.noneDdl();
        final List<QueryParser.QueryParam> parsedParams = List.empty();
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final Query<Void, Void> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQFactory.ddlQuery(anyString(), anyInt())).thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Void> query = queryLoader.loadQuery(QueryType.DDL, querySource, injStrat, extStrat, 123);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateDDL(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQFactory).ddlQuery(eq("this is SQL"), eq(123)); // timeout passed down
        verifyNoMoreInteractions(mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockQIEBuilder, mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    public void testDML_noParams() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Integer> extStrat = ExtractionStrategy.noneDml();
        final List<QueryParser.QueryParam> parsedParams = List.empty();
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final Query<Void, Integer> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQFactory.iudQueryWithoutParameters(anyString(), anyInt())).thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Integer> query = queryLoader.loadQuery(QueryType.DML, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateDML(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQFactory).iudQueryWithoutParameters(eq("this is SQL"), eq(0));
        verifyNoMoreInteractions(mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockQIEBuilder, mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    public void testDML_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Seq<Object>> injStrat = InjectionStrategy.indexed();
        final ExtractionStrategy<Integer> extStrat = ExtractionStrategy.noneDml();
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final IndexedInjector builtInjector = mock();
        final Query<Seq<Object>, Integer> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.buildIndexedInjector(any(), any(), any())).thenReturn(builtInjector);
        when(mockQFactory.iudQuery(anyString(), anyInt(), any(IndexedInjector.class))).thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Seq<Object>, Integer> query = queryLoader.loadQuery(QueryType.DML, querySource, injStrat, extStrat, 321);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateDML(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildIndexedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.INJECTION))
        );
        verify(mockQFactory).iudQuery(eq("this is SQL"), eq(321), same(builtInjector));
        verifyNoMoreInteractions(mockQParser, mockQFactory, mockQValidator, mockQIEBuilder);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDML_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<SomeComplexObject> injStrat = InjectionStrategy.named(SomeComplexObject.class);
        final ExtractionStrategy<Integer> extStrat = ExtractionStrategy.noneDml();
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final NamedInjector<SomeComplexObject> builtInjector = mock();
        final Query<SomeComplexObject, Integer> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedInjector(any(), any(), any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQFactory.iudQuery(anyString(), anyInt(), any(NamedInjector.class))).thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<SomeComplexObject, Integer> query = queryLoader.loadQuery(QueryType.DML, querySource, injStrat, extStrat, 321);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateDML(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildNamedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.INJECTION))
        );
        verify(mockQFactory).iudQuery(eq("this is SQL"), eq(321), same(builtInjector));
        verifyNoMoreInteractions(mockQParser, mockQFactory, mockQValidator, mockQIEBuilder);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    public void testSelect_indexed_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Seq<Object>> injStrat = InjectionStrategy.indexed();
        final ExtractionStrategy<Seq<Object>> extStrat = ExtractionStrategy.indexed();
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final IndexedInjector builtInjector = mock();
        final IndexedExtractor builtExtractor = mock();
        final Query<Seq<Object>, Seq<Object>> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.buildIndexedInjector(any(), any(), any())).thenReturn(builtInjector);
        when(mockQIEBuilder.buildIndexedExtractor(any(), any(), any())).thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(IndexedInjector.class), any(IndexedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Seq<Object>, Seq<Object>> query = queryLoader.loadQuery(QueryType.SELECT, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildIndexedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.INJECTION))
        );
        verify(mockQIEBuilder).buildIndexedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    public void testSelect_none_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<Seq<Object>> extStrat = ExtractionStrategy.indexed();
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final IndexedExtractor builtExtractor = mock();
        final Query<Void, Seq<Object>> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.buildIndexedExtractor(any(), any(), any())).thenReturn(builtExtractor);
        when(mockQFactory.selectQueryWithoutParameters(anyString(), anyInt(), any(IndexedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, Seq<Object>> query = queryLoader.loadQuery(QueryType.SELECT, querySource, injStrat, extStrat, 111);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildIndexedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.EXTRACTION))
        );
        verify(mockQFactory).selectQueryWithoutParameters(eq("this is SQL"), eq(111), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_named_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<SomeComplexObject> injStrat = InjectionStrategy.named(SomeComplexObject.class);
        final ExtractionStrategy<SomeComplexObject> extStrat = ExtractionStrategy.named(SomeComplexObject.class);
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final NamedInjector<SomeComplexObject> builtInjector = mock();
        final NamedExtractor<SomeComplexObject> builtExtractor = mock();
        final Query<SomeComplexObject, SomeComplexObject> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedInjector(any(), any(), any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedExtractor(any(), any(), any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(NamedInjector.class), any(NamedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<SomeComplexObject, SomeComplexObject> query = queryLoader.loadQuery(QueryType.SELECT, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildNamedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.INJECTION))
        );
        verify(mockQIEBuilder).buildNamedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_none_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Void> injStrat = InjectionStrategy.none();
        final ExtractionStrategy<SomeComplexObject> extStrat = ExtractionStrategy.named(SomeComplexObject.class);
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final NamedExtractor<SomeComplexObject> builtExtractor = mock();
        final Query<Void, SomeComplexObject> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedExtractor(any(), any(), any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQueryWithoutParameters(anyString(), anyInt(), any(NamedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Void, SomeComplexObject> query = queryLoader.loadQuery(QueryType.SELECT, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildNamedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.EXTRACTION))
        );
        verify(mockQFactory).selectQueryWithoutParameters(eq("this is SQL"), eq(0), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_indexed_named() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<Seq<Object>> injStrat = InjectionStrategy.indexed();
        final ExtractionStrategy<SomeComplexObject> extStrat = ExtractionStrategy.named(SomeComplexObject.class);
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final IndexedInjector builtInjector = mock();
        final NamedExtractor<SomeComplexObject> builtExtractor = mock();
        final Query<Seq<Object>, SomeComplexObject> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.buildIndexedInjector(any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedExtractor(any(), any(), any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(IndexedInjector.class), any(NamedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<Seq<Object>, SomeComplexObject> query = queryLoader.loadQuery(QueryType.SELECT, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildIndexedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.INJECTION))
        );
        verify(mockQIEBuilder).buildNamedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelect_named_indexed() {
        // mocks
        final QueryFactory mockQFactory = mock();
        final QueryParser mockQParser = mock();
        final QueryValidator mockQValidator = mock();
        final QueryInjectorExtractorBuilder mockQIEBuilder = mock();
        final TypesCollection mockTypesCollection = mock();
        final MappersFinder mockMappersFinder = mock();
        final ReflectionsFinder mockReflectionFinder = mock();
        final QuerySource querySource = mock();
        // data
        final InjectionStrategy<SomeComplexObject> injStrat = InjectionStrategy.named(SomeComplexObject.class);
        final ExtractionStrategy<Seq<Object>> extStrat = ExtractionStrategy.indexed();
        final List<QueryParser.QueryParam> parsedParams =
                List.of(
                        new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, null, null, null, null)
                );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", parsedParams);
        final NamedInjector<SomeComplexObject> builtInjector = mock();
        final IndexedExtractor builtExtractor = mock();
        final Query<SomeComplexObject, Seq<Object>> factoryQuery = mock();
        // behaviour
        when(mockQParser.parse(any())).thenReturn(parsedQuery);
        when(mockQIEBuilder.<SomeComplexObject>buildNamedInjector(any(), any(), any(), any(), any()))
                .thenReturn(builtInjector);
        when(mockQIEBuilder.buildIndexedExtractor(any(), any(), any()))
                .thenReturn(builtExtractor);
        when(mockQFactory.selectQuery(anyString(), anyInt(), any(NamedInjector.class), any(IndexedExtractor.class)))
                .thenReturn(factoryQuery);
        // test
        final QueryLoaderImpl queryLoader = new QueryLoaderImpl(
                mockQFactory, mockQParser, mockQValidator, mockQIEBuilder,
                mockTypesCollection, () -> mockMappersFinder, () -> mockReflectionFinder
        );
        final Query<SomeComplexObject, Seq<Object>> query = queryLoader.loadQuery(QueryType.SELECT, querySource, injStrat, extStrat);
        // assert
        assertThat(query).isSameAs(factoryQuery);
        // verify
        verify(mockQParser).parse(same(querySource));
        verify(mockQValidator).validateSelect(same(injStrat), same(extStrat), same(parsedParams));
        verify(mockQIEBuilder).buildNamedInjector(
                same(mockTypesCollection),
                same(mockMappersFinder),
                same(mockReflectionFinder),
                eq(SomeComplexObject.class),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.INJECTION))
        );
        verify(mockQIEBuilder).buildIndexedExtractor(
                same(mockTypesCollection),
                same(mockMappersFinder),
                eq(parsedParams.filter(qp -> qp.type() == QueryParser.ParamType.EXTRACTION))
        );
        verify(mockQFactory).selectQuery(eq("this is SQL"), eq(0), same(builtInjector), same(builtExtractor));
        verifyNoMoreInteractions(mockQIEBuilder, mockQParser, mockQFactory, mockQValidator);
        verifyNoInteractions(mockTypesCollection, mockMappersFinder, mockReflectionFinder, querySource, factoryQuery);
    }

    private static class SomeComplexObject {
        // just to use something as class in named strategies
        // not some nonsense like Object.class
    }

}
