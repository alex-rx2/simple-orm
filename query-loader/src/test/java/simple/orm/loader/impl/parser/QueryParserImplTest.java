package simple.orm.loader.impl.parser;

import io.vavr.collection.Traversable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.loader.QueryParser;
import simple.orm.loader.QueryParser.QueryParam;
import simple.orm.loader.QuerySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests on {@link QueryParserImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryParserImplTest {

    // it's stateless
    private final QueryParserImpl queryParser = new QueryParserImpl();

    @Test
    public void testSimplestQuery() {
        // sql
        final String sql = """
                           SELECT
                             id, --/::
                             country, --/::
                             city --/::
                           FROM table
                           WHERE a=? --?::
                             AND b=? --?::
                           """;
        // test
        final QueryParser.ParsedQuery parsedQuery = queryParser.parse(QuerySource.of(sql));
        // asserts
        assertThat(parsedQuery.querySQL()).isEqualTo(sql);
        final Traversable<QueryParam> params = parsedQuery.parsedParams();
        assertThat(params)
                .containsExactly(
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 1, "id", true, null, null, null, null, null),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 2, "country", true, null, null, null, null, null),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 3, "city", true, null, null, null, null, null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, null, null, null, null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, null, null, null, null, null)
                );
    }

    @Test
    public void testImmediateParameters() {
        // sql
        final String sql = """
                           SELECT
                             id,   --/:id:int(int):INT:java.lang.Integer
                             country, --/:countryCode:varchar:VARCHAR
                             city_id,--/:city.id::VARCHAR:java.lang.String
                             city_name --/:city.name:::java.lang.String
                             city_zip--/::::java.lang.String
                           FROM table
                           WHERE a=?, --?:some.object.propertyA:int(str):INT:java.lang.String
                             AND b=?,--?:some.object.propertyB:int(int):INT
                             AND c=?  --?:some.object.propertyC::TINYINT:java.lang.Integer
                             AND d=?--?:some.object.propertyD:::java.lang.Double
                             AND e=?--?::::java.lang.Float
                           """;
        // test
        final QueryParser.ParsedQuery parsedQuery = queryParser.parse(QuerySource.of(sql));
        // asserts
        assertThat(parsedQuery.querySQL()).isEqualTo(sql);
        final Traversable<QueryParam> params = parsedQuery.parsedParams();
        assertThat(params)
                .containsExactly(
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 1, "id", true, "id", "int", "int", "INT", "java.lang.Integer"),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 2, "country", true, "countryCode", "varchar", null, "VARCHAR", null),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 3, "city_id", true, "city.id", null, null, "VARCHAR", "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 4, "city_name", true, "city.name", null, null, null, "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 5, "city_zip", true, null, null, null, null, "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, "some.object.propertyA", "int", "str", "INT", "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, "some.object.propertyB", "int", "int", "INT", null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 3, null, false, "some.object.propertyC", null, null, "TINYINT", "java.lang.Integer"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 4, null, false, "some.object.propertyD", null, null, null, "java.lang.Double"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 5, null, false, null, null, null, null, "java.lang.Float")
                );
    }

    @Test
    public void testSeparateParameters() {
        // sql
        final String sql = """
                           
                           --//id:id:int(int):INT:java.lang.Integer
                           /*
                           //country:countryCode:varchar:VARCHAR
                           //city_id:city.id::VARCHAR:java.lang.String
                           */
                           --??:some.object.propertyA:int(str):INT:java.lang.String
                           /*
                           ??:some.object.propertyB:int(int):INT
                           ??:some.object.propertyC::TINYINT:java.lang.Integer
                           */
                           
                           SELECT
                             id,
                             country,
                             city_id,
                             city_name,
                             city_zip
                           FROM table
                           WHERE a=?
                             AND b=?
                             AND c=?
                             AND d=?
                             AND e=?
                           
                           /*
                           ??:some.object.propertyD:::java.lang.Double
                           ??::::java.lang.Float
                           //:city.name:::java.lang.String
                           //::::java.lang.String
                           */
                           
                           """;
        // test
        final QueryParser.ParsedQuery parsedQuery = queryParser.parse(QuerySource.of(sql));
        // asserts
        assertThat(parsedQuery.querySQL()).isEqualTo(sql);
        final Traversable<QueryParam> params = parsedQuery.parsedParams();
        assertThat(params)
                .containsExactly(
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 1, "id", false, "id", "int", "int", "INT", "java.lang.Integer"),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 2, "country", false, "countryCode", "varchar", null, "VARCHAR", null),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 3, "city_id", false, "city.id", null, null, "VARCHAR", "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, "some.object.propertyA", "int", "str", "INT", "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, "some.object.propertyB", "int", "int", "INT", null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 3, null, false, "some.object.propertyC", null, null, "TINYINT", "java.lang.Integer"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 4, null, false, "some.object.propertyD", null, null, null, "java.lang.Double"),
                        new QueryParam(QueryParser.ParamType.INJECTION, 5, null, false, null, null, null, null, "java.lang.Float"),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 4, null, false, "city.name", null, null, null, "java.lang.String"),
                        new QueryParam(QueryParser.ParamType.EXTRACTION, 5, null, false, null, null, null, null, "java.lang.String")
                );
    }

    @Test
    public void testImmediateParameters_insert() {
        // sql
        final String sql = """
                           INSERT INTO some_table VALUES (
                             ?, --?::int
                             ?, --?::varchar
                             ?, --?::tinyint(int)
                             ?, --?::numeric
                             ?  --?::varchar
                           )
                           """;
        // test
        final QueryParser.ParsedQuery parsedQuery = queryParser.parse(QuerySource.of(sql));
        // asserts
        assertThat(parsedQuery.querySQL()).isEqualTo(sql);
        final Traversable<QueryParam> params = parsedQuery.parsedParams();
        assertThat(params)
                .containsExactly(
                        new QueryParam(QueryParser.ParamType.INJECTION, 1, null, false, null, "int", null, null, null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 2, null, false, null, "varchar", null, null, null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 3, null, false, null, "tinyint", "int", null, null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 4, null, false, null, "numeric", null, null, null),
                        new QueryParam(QueryParser.ParamType.INJECTION, 5, null, false, null, "varchar", null, null, null)
                );
    }

}
