package simple.orm.jdbc.common;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.query.NamedParametersMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests on {@link DefaultNPProcessor}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultNPProcessorTest {

    private DefaultNPProcessor npProcessor;

    @BeforeEach
    void setUp() {
        npProcessor = new DefaultNPProcessor(); // it has no state itself
    }

    @Test
    public void testSimpleQuery() {
        String sql = "" +
                "\nSELECT *" +
                "\nFROM table" +
                "\nWHERE a=:aa AND b=:bb";
        String expectedSql = "" +
                "\nSELECT *" +
                "\nFROM table" +
                "\nWHERE a=? AND b=?";
        Tuple2<String, NamedParametersMap> res = npProcessor.process(sql);
        String processedSql = res._1;
        NamedParametersMap params = res._2;
        assertThat(processedSql).isEqualTo(expectedSql);
        assertThat(params.getParameters())
                .containsExactly(
                        Tuple.of(1, "aa"),
                        Tuple.of(2, "bb")
                );
    }

    @Test
    public void testQueryWithComments() {
        String sql = "" +
                "\nSELECT * --comment:cc" +
                "\n/*FROM table :tt" +
                "\nWHERE a=:aa*/" +
                "\nFROM table" +
                "\nWHERE a=:aa --AND b=:bb" +
                "\n  AND x=:xx/*" +
                "\n  AND y=:yy*/";
        String expectedSql = "" +
                "\nSELECT * --comment:cc" +
                "\n/*FROM table :tt" +
                "\nWHERE a=:aa*/" +
                "\nFROM table" +
                "\nWHERE a=? --AND b=:bb" +
                "\n  AND x=?/*" +
                "\n  AND y=:yy*/";
        Tuple2<String, NamedParametersMap> res = npProcessor.process(sql);
        String processedSql = res._1;
        NamedParametersMap params = res._2;
        assertThat(processedSql).isEqualTo(expectedSql);
        assertThat(params.getParameters())
                .containsExactly(
                        Tuple.of(1, "aa"),
                        Tuple.of(2, "xx")
                );
    }

    @Test
    public void testQueryWithStringLiterals() {
        String sql = "" +
                "\nSELECT *" +
                "\nFROM table" +
                "\nWHERE a=:xx AND b=':bb'" +
                "\n  AND c=:xx AND d=\":dd\"" +
                "\n  AND e=:e''e AND f=\"'\":ff" +
                "\n  AND g=::gg\"g\" AND h=:'h'':hh'";
        String expectedSql = "" +
                "\nSELECT *" +
                "\nFROM table" +
                "\nWHERE a=? AND b=':bb'" +
                "\n  AND c=? AND d=\":dd\"" +
                "\n  AND e=?''e AND f=\"'\"?" +
                "\n  AND g=:?\"g\" AND h=:'h'':hh'";
        Tuple2<String, NamedParametersMap> res = npProcessor.process(sql);
        String processedSql = res._1;
        NamedParametersMap params = res._2;
        assertThat(processedSql).isEqualTo(expectedSql);
        assertThat(params.getParameters())
                .containsExactly(
                        Tuple.of(1, "xx"),
                        Tuple.of(2, "xx"),
                        Tuple.of(3, "e"),
                        Tuple.of(4, "ff"),
                        Tuple.of(5, "gg")
                );
    }
}
