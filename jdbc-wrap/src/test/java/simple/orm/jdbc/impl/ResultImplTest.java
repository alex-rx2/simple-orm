package simple.orm.jdbc.impl;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.NamedExtractor;

import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link ResultImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResultImplTest {

    @Test
    public void testClose() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final IndexedExtractor mockIExtractor = mock();
        // test
        final ResultImpl<Seq<Object>> result = ResultImpl.indexed(mockResultSet, mockIExtractor);
        result.close();
        // verify
        verify(mockResultSet).close();
        verifyNoMoreInteractions(mockResultSet, mockIExtractor);
    }

    @Test
    public void testIsClosed() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final IndexedExtractor mockIExtractor = mock();
        // behaviour
        when(mockResultSet.isClosed()).thenReturn(false, true, false, true, false);
        // test
        final ResultImpl<Seq<Object>> result = ResultImpl.indexed(mockResultSet, mockIExtractor);
        assertThat(result.isClosed()).isFalse();
        assertThat(result.isClosed()).isTrue();
        result.close(); // test that isClosed depend only on underlying ResultSet
        assertThat(result.isClosed()).isFalse();
        assertThat(result.isClosed()).isTrue();
        // verify
        InOrder inOrder = inOrder(mockResultSet);
        inOrder.verify(mockResultSet, times(2)).isClosed();
        inOrder.verify(mockResultSet).close();
        inOrder.verify(mockResultSet, times(2)).isClosed();
        verifyNoMoreInteractions(mockResultSet, mockIExtractor);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtraction_indexed() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final IndexedExtractor mockIExtractor = mock();
        final Seq<Object> row01extractor = List.of("1", "2", "3");
        final Seq<Object> row02extractor = List.of(1, 2, 3);
        // behaviour
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockIExtractor.extractRow(any())).thenReturn(row01extractor, row02extractor);
        // test
        final ResultImpl<Seq<Object>> result = ResultImpl.indexed(mockResultSet, mockIExtractor);
        assertThat(result.shouldBeClosedAutomatically()).isTrue();
        assertThat(result.hasNextRow()).isTrue();
        assertThat(result.hasNextRow()).isTrue();
        assertThat(result.hasNextRow()).isTrue();
        final Seq<Object> row01 = result.nextRow();
        final Seq<Object> row02 = result.nextRow();
        assertThat(result.hasNextRow()).isFalse();
        assertThat(result.hasNextRow()).isFalse();
        assertThat(result.hasNextRow()).isFalse();
        assertThat(row01).isSameAs(row01extractor);
        assertThat(row02).isSameAs(row02extractor);
        // verify
        InOrder inOrder = inOrder(mockResultSet, mockIExtractor);
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockIExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockIExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockResultSet, times(3)).close(); // each hasNextRow() at the end trigger shouldAutoClose behaviour
        verifyNoMoreInteractions(mockResultSet, mockIExtractor);
    }

    @Test
    public void testExtraction_named() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final NamedExtractor<Object> mockNExtractor = mock();
        final Object row01extractor = 123;
        final Object row02extractor = "123";
        // behaviour
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockNExtractor.extractRow(any())).thenReturn(row01extractor, row02extractor, null);
        // test
        final ResultImpl<Object> result = ResultImpl.named(mockResultSet, mockNExtractor);
        assertThat(result.shouldBeClosedAutomatically()).isTrue();
        result.setShouldBeClosedAutomatically(false); // disable shouldAutoClose
        assertThat(result.shouldBeClosedAutomatically()).isFalse();
        assertThat(result.hasNextRow()).isTrue();
        final Object row01 = result.nextRow();
        assertThat(result.hasNextRow()).isTrue();
        final Object row02 = result.nextRow();
        assertThat(result.hasNextRow()).isFalse();
        assertThat(result.hasNextRow()).isFalse();
        assertThat(result.hasNextRow()).isFalse();
        assertThat(row01).isSameAs(row01extractor);
        assertThat(row02).isSameAs(row02extractor);
        // verify
        InOrder inOrder = inOrder(mockResultSet, mockNExtractor);
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockNExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockNExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockResultSet, never()).close(); // shouldAutoClose disabled - so no close()
        verifyNoMoreInteractions(mockResultSet, mockNExtractor);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtractAll() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final IndexedExtractor mockIExtractor = mock();
        final Seq<Object> row01extractor = List.of("1", "2", "3");
        final Seq<Object> row02extractor = List.of(1, 2, 3);
        final Seq<Object> row03extractor = List.of(new Object(), new Object());
        // behaviour
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockIExtractor.extractRow(any())).thenReturn(row01extractor, row02extractor, row03extractor);
        // test
        final ResultImpl<Seq<Object>> result = ResultImpl.indexed(mockResultSet, mockIExtractor);
        final Seq<Seq<Object>> rows = result.extractAll();
        assertThat(rows)
                .containsExactly(row01extractor, row02extractor, row03extractor);
        // verify
        InOrder inOrder = inOrder(mockResultSet, mockIExtractor);
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockIExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockIExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockIExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockResultSet).close(); // triggered by shouldAutoClose
        verifyNoMoreInteractions(mockResultSet, mockIExtractor);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtractAll_notAtStart() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final IndexedExtractor mockIExtractor = mock();
        final Seq<Object> row01extractor = List.of("1", "2", "3");
        final Seq<Object> row02extractor = List.of(1, 2, 3);
        final Seq<Object> row03extractor = List.of(new Object(), new Object());
        // behaviour
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockIExtractor.extractRow(any())).thenReturn(row01extractor, row02extractor, row03extractor);
        // test
        final ResultImpl<Seq<Object>> result = ResultImpl.indexed(mockResultSet, mockIExtractor);
        assertThat(result.hasNextRow()).isTrue();
        assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
        // verify
        InOrder inOrder = inOrder(mockResultSet, mockIExtractor);
        inOrder.verify(mockResultSet).next();
        verifyNoMoreInteractions(mockResultSet, mockIExtractor);
    }

    @Test
    public void testExactlySingleRow() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final NamedExtractor<Object> mockNExtractor = mock();
        final Object row01extractor = 123;
        // behaviour
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockNExtractor.extractRow(any())).thenReturn(row01extractor, (Object) null);
        // test
        final ResultImpl<Object> result = ResultImpl.named(mockResultSet, mockNExtractor);
        final Object row = result.exactlySingleRow();
        assertThat(row).isSameAs(row01extractor);
        // verify
        InOrder inOrder = inOrder(mockResultSet, mockNExtractor);
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockNExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockResultSet).close(); // triggered by shouldAutoClose
        verifyNoMoreInteractions(mockResultSet, mockNExtractor);
    }

    @Test
    public void testExactlySingleRow_hasMoreRows() throws Exception {
        // prepare
        final ResultSet mockResultSet = mock();
        final NamedExtractor<Object> mockNExtractor = mock();
        final Object row01extractor = 123;
        // behaviour
        when(mockResultSet.next()).thenReturn(true, true);
        when(mockNExtractor.extractRow(any())).thenReturn(row01extractor, (Object) null);
        // test
        final ResultImpl<Object> result = ResultImpl.named(mockResultSet, mockNExtractor);
        assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
        // verify
        InOrder inOrder = inOrder(mockResultSet, mockNExtractor);
        inOrder.verify(mockResultSet).next();
        inOrder.verify(mockNExtractor).extractRow(same(mockResultSet));
        inOrder.verify(mockResultSet).next();
        verifyNoMoreInteractions(mockResultSet, mockNExtractor);
    }

}
