package simple.orm.loader.impl.loader;

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
import simple.orm.loader.builder.QueryBuilder;

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
        final InjectionStrategy iStrat = mock();
        final ExtractionStrategy eStrat = mock();
        final Traversable<QueryParser.QueryParam> params = mock();
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
        inOrder.verify(mockBuilder).buildQuery(same(qType), eq("this is SQL"), same(iStrat), same(eStrat), same(params), eq(123123));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockParser, mockBuilder);
        verifyNoInteractions(qType, qSource, iStrat, eStrat, params, query);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testQueryLoader_defaultTimeout() {
        // query loader actually just passes arguments between parser and builder 乁( ͡° ͜ʖ ͡°)ㄏ
        final QueryParser mockParser = mock();
        final QueryBuilder mockBuilder = mock();
        // data
        final QueryType qType = mock();
        final QuerySource qSource = mock();
        final InjectionStrategy iStrat = mock();
        final ExtractionStrategy eStrat = mock();
        final Traversable<QueryParser.QueryParam> params = mock();
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
        inOrder.verify(mockBuilder).buildQuery(same(qType), eq("this is SQL"), same(iStrat), same(eStrat), same(params), eq(0));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockParser, mockBuilder);
        verifyNoInteractions(qType, qSource, iStrat, eStrat, params, query);
    }

}
