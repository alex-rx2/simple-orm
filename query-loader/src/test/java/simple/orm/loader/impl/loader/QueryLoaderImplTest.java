package simple.orm.loader.impl.loader;

import io.vavr.collection.List;
import io.vavr.collection.Traversable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;
import simple.orm.loader.builder.ParameterType;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.loader.builder.QueryParameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link QueryLoaderImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryLoaderImplTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testQueryLoader() {
        // query loader actually just passes arguments between parser and builder 乁( ͡° ͜ʖ ͡°)ㄏ
        final QueryParser mockParser = mock();
        final QueryBuilder mockBuilder = mock();
        // data
        final QueryType qType = mock();
        final QuerySource qSource = mock();
        final InjectionStrategy iStrat = InjectionStrategy.none();
        final ExtractionStrategy eStrat = ExtractionStrategy.noneDdl();
        final Traversable<QueryParser.QueryParam> params = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION,
                        11, null, null, "mapper", "tag", "jdbcType", "javaType"
                ),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION,
                        1, "labbel", "proppy1", null, null, null, null
                ),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION,
                        2, "babbel", "proppy2", null, null, null, null
                ),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION,
                        3, null, "proppy3", null, null, null, null
                )
        );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", params);
        final Query query = mock();
        // behaviour
        when(mockParser.parse(any())).thenReturn(parsedQuery);
        when(mockBuilder.buildQuery(any(), anyString(), any(), any(), any(), anyInt())).thenReturn(query);
        // test
        QueryLoaderImpl queryLoader = new QueryLoaderImpl(mockParser, mockBuilder);
        Query resultQuery = queryLoader.loadQuery(qType, qSource, iStrat, eStrat, 123123);
        assertThat(resultQuery).isSameAs(query);
        // verify
        InOrder inOrder = inOrder(mockParser, mockBuilder);
        inOrder.verify(mockParser).parse(same(qSource));
        inOrder.verify(mockBuilder).buildQuery(
                same(qType),
                eq("this is SQL"),
                same(iStrat),
                same(eStrat),
                eq(List.of(
                        new QueryParameter(ParameterType.INJECTION,
                                11, null, null, null, "mapper", "tag", null, "jdbcType", null, "javaType"
                        ),
                        new QueryParameter(ParameterType.EXTRACTION,
                                1, "labbel", "proppy1", null, null, null, null, null, null, null
                        ),
                        new QueryParameter(ParameterType.EXTRACTION,
                                2, "babbel", "proppy2", null, null, null, null, null, null, null
                        ),
                        new QueryParameter(ParameterType.EXTRACTION,
                                3, null, "proppy3", null, null, null, null, null, null, null
                        )
                )),
                eq(123123)
        );
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockParser, mockBuilder);
        verifyNoInteractions(qType, qSource, query);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testQueryLoader_defaultTimeout() {
        // query loader actually just passes arguments between parser and builder 乁( ͡° ͜ʖ ͡°)ㄏ
        // with minor tweaks now around named extractor labels/properties
        final QueryParser mockParser = mock();
        final QueryBuilder mockBuilder = mock();
        // data
        final QueryType qType = mock();
        final QuerySource qSource = mock();
        final InjectionStrategy iStrat = InjectionStrategy.indexed();
        final ExtractionStrategy eStrat = ExtractionStrategy.indexed();
        final Traversable<QueryParser.QueryParam> params = List.of(
                new QueryParser.QueryParam(QueryParser.ParamType.INJECTION,
                        11, null, null, "mapper", "tag", "jdbcType", "javaType"
                ),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION,
                        -11, "labbel", "proppy", null, null, null, null
                ),
                new QueryParser.QueryParam(QueryParser.ParamType.EXTRACTION,
                        -22, "babbel", null, null, null, null, null
                )
        );
        final QueryParser.ParsedQuery parsedQuery = new QueryParser.ParsedQuery("this is SQL", params);
        final Query query = mock();
        // behaviour
        when(mockParser.parse(any())).thenReturn(parsedQuery);
        when(mockBuilder.buildQuery(any(), anyString(), any(), any(), any(), anyInt())).thenReturn(query);
        // test
        QueryLoaderImpl queryLoader = new QueryLoaderImpl(mockParser, mockBuilder);
        Query resultQuery = queryLoader.loadQuery(qType, qSource, iStrat, eStrat);
        assertThat(resultQuery).isSameAs(query);
        // verify
        InOrder inOrder = inOrder(mockParser, mockBuilder);
        inOrder.verify(mockParser).parse(same(qSource));
        // 0 - default timeout (no timeout)
        inOrder.verify(mockBuilder).buildQuery(
                same(qType),
                eq("this is SQL"),
                same(iStrat),
                same(eStrat),
                eq(List.of(
                        new QueryParameter(ParameterType.INJECTION,
                                11, null, null, null, "mapper", "tag", null, "jdbcType", null, "javaType"
                        ),
                        new QueryParameter(ParameterType.EXTRACTION,
                                -11, "labbel", "proppy", null, null, null, null, null, null, null
                        ),
                        new QueryParameter(ParameterType.EXTRACTION,
                                -22, "babbel", null, null, null, null, null, null, null, null
                        )
                )),
                eq(0)
        );
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockParser, mockBuilder);
        verifyNoInteractions(qType, qSource, query);
    }

}
