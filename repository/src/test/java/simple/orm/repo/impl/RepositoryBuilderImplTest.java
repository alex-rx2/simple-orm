package simple.orm.repo.impl;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.repo.ImplementationStyle;
import simple.orm.repo.SQLLoader;
import simple.orm.repo.anno.ParameterStrategy;
import simple.orm.repo.anno.RepoType;
import simple.orm.repo.impl.meta.MetadataCollector;
import simple.orm.repo.impl.meta.QueryMethodMeta;
import simple.orm.repo.impl.meta.RepositoryMeta;
import simple.orm.repo.impl.proxy.ProxyHandlerBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link RepositoryBuilderImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepositoryBuilderImplTest {

    private MockedStatic<QuerySource> mockedStaticQuerySource;

    @BeforeEach
    void setUp() {
        mockedStaticQuerySource = mockStatic(QuerySource.class);
    }

    @AfterEach
    void tearDown() {
        mockedStaticQuerySource.close();
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    public void testBuildProxy() {
        final SQLLoader mockSQLLoader = mock();
        final QueryParser mockQParser = mock();
        final QueryBuilder mockQBuilder = mock();
        final MetadataCollector mockMetaCollector = mock();
        final ProxyHandlerBuilder mockHandlerBuilder = mock();
        // data
        final List<QueryParser.QueryParam> method2InjectionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, "int", null, null, null)
        );
        final List<QueryParser.QueryParam> method2ExtractionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, "id", false, "id", "int", null, null, null),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 2, "name", false, "name", "varchar", null, null, null)
        );
        final List<QueryParser.QueryParam> method3InjectionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, "uid", "uuid", null, null, null)
        );
        final List<QueryParser.QueryParam> method3ExtractionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, "uuid", null, null, null),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 2, null, false, null, "timestamptz", "h2str", null, null)
        );
        final RepositoryMeta repoMeta = new RepositoryMeta(
                RepoType.QUERY,
                List.of(
                        new QueryMethodMeta(
                                "methodOne",
                                QueryType.DML,
                                "DML SQL",
                                null,
                                "UTF-8",
                                ParameterStrategy.PARSE_QUERY,
                                null,
                                null,
                                List.empty(),
                                List.empty(),
                                -1
                        ),
                        new QueryMethodMeta(
                                "methodTwo",
                                QueryType.SELECT,
                                null,
                                "ftp://usa.gov/test/query/select",
                                "UTF-88",
                                ParameterStrategy.PROVIDED,
                                null,
                                SomeComplexObject.class,
                                method2InjectionParams,
                                method2ExtractionParams,
                                123
                        ),
                        new QueryMethodMeta(
                                "methodThree",
                                QueryType.SELECT,
                                "SELECT-3 SQL",
                                null,
                                "UTF-99",
                                ParameterStrategy.PROVIDED,
                                SomeComplexObject.class,
                                null,
                                method3InjectionParams,
                                method3ExtractionParams,
                                0
                        )
                ),
                321
        );
        final List<QueryParser.QueryParam> parsedDMLParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, "bigint", null, null, null),
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, null, "clob", null, null, null)
        );
        final QueryParser.ParsedQuery parsedDML = new QueryParser.ParsedQuery("parsed DML SQL", parsedDMLParams);
        final QuerySource querySourceMethod2 = mock();
        final Query<Seq<Object>, Void> queryDML = mock();
        final Query<Seq<Object>, SomeComplexObject> querySelect2 = mock();
        final Query<Seq<Object>, SomeComplexObject> querySelect3 = mock();
        final InvocationHandler proxyHandler = mock();
        // behavior
        mockedStaticQuerySource.when(() -> QuerySource.of(anyString())).thenReturn(querySourceMethod2);
        when(mockMetaCollector.collectMetadata(any())).thenReturn(repoMeta);
        when(mockSQLLoader.loadFromURI(anyString(), anyString())).thenReturn("SELECT SQL");
        when(mockQParser.parse(any())).thenReturn(parsedDML);
        when(mockQBuilder.buildQuery(
                eq(QueryType.DML), anyString(), any(), any(), any(), anyInt()
        )).thenReturn((Query) queryDML);
        when(mockQBuilder.buildQuery(
                eq(QueryType.SELECT), eq("SELECT SQL"), any(), any(), any(), anyInt()
        )).thenReturn((Query) querySelect2);
        when(mockQBuilder.buildQuery(
                eq(QueryType.SELECT), eq("SELECT-3 SQL"), any(), any(), any(), anyInt()
        )).thenReturn((Query) querySelect3);
        when(mockHandlerBuilder.build(any(), any())).thenReturn(proxyHandler);
        // test
        final RepositoryBuilderImpl repoBuilder = new RepositoryBuilderImpl(
                mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        final TestInterface repository = repoBuilder.buildRepository(TestInterface.class, ImplementationStyle.JAVA_PROXY);
        assertThat(repository).isInstanceOf(TestInterface.class);
        assertThat(repository).isInstanceOf(Proxy.class);
        assertThat(Proxy.getInvocationHandler(repository)).isSameAs(proxyHandler);
        // verify
        verify(mockMetaCollector).collectMetadata(eq(TestInterface.class));
        // - first method
        mockedStaticQuerySource.verify(() -> QuerySource.of(eq("DML SQL")));
        verify(mockQParser).parse(same(querySourceMethod2));
        verify(mockQBuilder).buildQuery(
                eq(QueryType.DML),
                eq("parsed DML SQL"),
                eq(InjectionStrategy.indexed()),
                eq(ExtractionStrategy.noneDml()),
                eq(parsedDMLParams), // will be new list with only these parameters
                eq(321)
        );
        // - second method
        verify(mockSQLLoader).loadFromURI(eq("ftp://usa.gov/test/query/select"), eq("UTF-88"));
        verify(mockQBuilder).buildQuery(
                eq(QueryType.SELECT),
                eq("SELECT SQL"),
                eq(InjectionStrategy.indexed()),
                eq(ExtractionStrategy.named(SomeComplexObject.class)),
                eq(method2InjectionParams.appendAll(method2ExtractionParams)),
                eq(123)
        );
        // - third method
        verify(mockQBuilder).buildQuery(
                eq(QueryType.SELECT),
                eq("SELECT-3 SQL"),
                eq(InjectionStrategy.named(SomeComplexObject.class)),
                eq(ExtractionStrategy.indexed()),
                eq(method3InjectionParams.appendAll(method3ExtractionParams)),
                eq(0)
        );
        // - handler
        verify(mockHandlerBuilder).build(
                eq(TestInterface.class),
                eq(HashMap.of(
                        "methodOne", queryDML,
                        "methodTwo", querySelect2,
                        "methodThree", querySelect3
                ))
        );
        // - no more interactions
        mockedStaticQuerySource.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        verifyNoInteractions(querySourceMethod2, queryDML, querySelect2, querySelect3, proxyHandler);
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    public void testBuildProxyLazy() {
        final SQLLoader mockSQLLoader = mock();
        final QueryParser mockQParser = mock();
        final QueryBuilder mockQBuilder = mock();
        final MetadataCollector mockMetaCollector = mock();
        final ProxyHandlerBuilder mockHandlerBuilder = mock();
        // data
        final List<QueryParser.QueryParam> method2InjectionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, "int", null, null, null)
        );
        final List<QueryParser.QueryParam> method2ExtractionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, "id", false, "id", "int", null, null, null),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 2, "name", false, "name", "varchar", null, null, null)
        );
        final List<QueryParser.QueryParam> method3InjectionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, "uid", "uuid", null, null, null)
        );
        final List<QueryParser.QueryParam> method3ExtractionParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 1, null, false, null, "uuid", null, null, null),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION, 2, null, false, null, "timestamptz", "h2str", null, null)
        );
        final RepositoryMeta repoMeta = new RepositoryMeta(
                RepoType.QUERY,
                List.of(
                        new QueryMethodMeta(
                                "methodOneLazy",
                                QueryType.DML,
                                "DML SQL",
                                null,
                                "UTF-8",
                                ParameterStrategy.PARSE_QUERY,
                                null,
                                null,
                                List.empty(),
                                List.empty(),
                                -1
                        ),
                        new QueryMethodMeta(
                                "methodTwoLazy",
                                QueryType.SELECT,
                                null,
                                "ftp://usa.gov/test/query/select",
                                "UTF-88",
                                ParameterStrategy.PROVIDED,
                                null,
                                SomeComplexObject.class,
                                method2InjectionParams,
                                method2ExtractionParams,
                                123
                        ),
                        new QueryMethodMeta(
                                "methodThreeLazy",
                                QueryType.SELECT,
                                "SELECT-3 SQL",
                                null,
                                "UTF-99",
                                ParameterStrategy.PROVIDED,
                                SomeComplexObject.class,
                                null,
                                method3InjectionParams,
                                method3ExtractionParams,
                                0
                        )
                ),
                321
        );
        final List<QueryParser.QueryParam> parsedDMLParams = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, "bigint", null, null, null),
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, null, "clob", null, null, null)
        );
        final QueryParser.ParsedQuery parsedDML = new QueryParser.ParsedQuery("parsed DML SQL", parsedDMLParams);
        final QuerySource querySourceMethod2 = mock();
        final Query<Seq<Object>, Void> queryDML = mock();
        final Query<Seq<Object>, SomeComplexObject> querySelect2 = mock();
        final Query<Seq<Object>, SomeComplexObject> querySelect3 = mock();
        final InvocationHandler proxyHandler = mock();
        // behavior
        mockedStaticQuerySource.when(() -> QuerySource.of(anyString())).thenReturn(querySourceMethod2);
        when(mockMetaCollector.collectMetadata(any())).thenReturn(repoMeta);
        when(mockSQLLoader.loadFromURI(anyString(), anyString())).thenReturn("SELECT SQL");
        when(mockQParser.parse(any())).thenReturn(parsedDML);
        when(mockQBuilder.buildQuery(
                eq(QueryType.DML), anyString(), any(), any(), any(), anyInt()
        )).thenReturn((Query) queryDML);
        when(mockQBuilder.buildQuery(
                eq(QueryType.SELECT), eq("SELECT SQL"), any(), any(), any(), anyInt()
        )).thenReturn((Query) querySelect2);
        when(mockQBuilder.buildQuery(
                eq(QueryType.SELECT), eq("SELECT-3 SQL"), any(), any(), any(), anyInt()
        )).thenReturn((Query) querySelect3);
        when(mockHandlerBuilder.buildLazy(any(), any())).thenReturn(proxyHandler);
        // test
        final RepositoryBuilderImpl repoBuilder = new RepositoryBuilderImpl(
                mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        final TestInterface repository = repoBuilder.buildRepository(TestInterface.class, ImplementationStyle.JAVA_PROXY_LAZY);
        assertThat(repository).isInstanceOf(TestInterface.class);
        assertThat(repository).isInstanceOf(Proxy.class);
        assertThat(Proxy.getInvocationHandler(repository)).isSameAs(proxyHandler);
        // verify
        verify(mockMetaCollector).collectMetadata(eq(TestInterface.class));
        // - handler
        ArgumentCaptor<Map<String, Supplier<Query<?,?>>>> supplierCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockHandlerBuilder).buildLazy(
                eq(TestInterface.class),
                supplierCaptor.capture()
                // eq(HashMap.of(
                //         "methodOne", queryDML,
                //         "methodTwo", querySelect2,
                //         "methodThree", querySelect3
                // ))
        );
        // - no more interactions (queries will be build only on methods first invocation!)
        mockedStaticQuerySource.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        verifyNoInteractions(querySourceMethod2, queryDML, querySelect2, querySelect3, proxyHandler);
        // check suppliers
        Map<String, Supplier<Query<?, ?>>> suppliers = supplierCaptor.getValue();
        assertThat(suppliers.keySet()).containsExactlyInAnyOrder("methodOneLazy","methodTwoLazy","methodThreeLazy");
        // - first method
        assertThat(suppliers.get("methodOneLazy").get().get()).isSameAs(queryDML);
        mockedStaticQuerySource.verify(() -> QuerySource.of(eq("DML SQL")));
        verify(mockQParser).parse(same(querySourceMethod2));
        verify(mockQBuilder).buildQuery(
                eq(QueryType.DML),
                eq("parsed DML SQL"),
                eq(InjectionStrategy.indexed()),
                eq(ExtractionStrategy.noneDml()),
                eq(parsedDMLParams), // will be new list with only these parameters
                eq(321)
        );
        mockedStaticQuerySource.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        verifyNoInteractions(querySourceMethod2, queryDML, querySelect2, querySelect3, proxyHandler);
        // - second method
        assertThat(suppliers.get("methodTwoLazy").get().get()).isSameAs(querySelect2);
        verify(mockSQLLoader).loadFromURI(eq("ftp://usa.gov/test/query/select"), eq("UTF-88"));
        verify(mockQBuilder).buildQuery(
                eq(QueryType.SELECT),
                eq("SELECT SQL"),
                eq(InjectionStrategy.indexed()),
                eq(ExtractionStrategy.named(SomeComplexObject.class)),
                eq(method2InjectionParams.appendAll(method2ExtractionParams)),
                eq(123)
        );
        mockedStaticQuerySource.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        verifyNoInteractions(querySourceMethod2, queryDML, querySelect2, querySelect3, proxyHandler);
        // - third method
        assertThat(suppliers.get("methodThreeLazy").get().get()).isSameAs(querySelect3);
        verify(mockQBuilder).buildQuery(
                eq(QueryType.SELECT),
                eq("SELECT-3 SQL"),
                eq(InjectionStrategy.named(SomeComplexObject.class)),
                eq(ExtractionStrategy.indexed()),
                eq(method3InjectionParams.appendAll(method3ExtractionParams)),
                eq(0)
        );
        mockedStaticQuerySource.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockSQLLoader, mockQParser, mockQBuilder, mockMetaCollector, mockHandlerBuilder);
        verifyNoInteractions(querySourceMethod2, queryDML, querySelect2, querySelect3, proxyHandler);
    }

    public interface TestInterface {
    }

    public static class SomeComplexObject {
        // just to use something as class in named strategies
        // not some nonsense like Object.class
    }

}
