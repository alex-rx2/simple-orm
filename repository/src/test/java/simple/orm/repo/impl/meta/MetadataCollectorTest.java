package simple.orm.repo.impl.meta;

import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.query.Query;
import simple.orm.loader.QueryParser;
import simple.orm.repo.anno.ExtractParam;
import simple.orm.repo.anno.InjectParam;
import simple.orm.repo.anno.QuerySource;
import simple.orm.repo.anno.SimpleOrmRepo;
import simple.orm.repo.anno.SimpleQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static simple.orm.jdbc.query.QueryType.*;
import static simple.orm.repo.anno.ParameterStrategy.*;
import static simple.orm.repo.anno.RepoType.QUERY;

/**
 * Tests on {@link MetadataCollector}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataCollectorTest {

    // has no state
    private final MetadataCollector collector = new MetadataCollector();

    @Test
    public void testRepo01() {
        // test for simple DDL, DML queries
        // also different timeouts, QuerySource
        final RepositoryMeta repoMeta = collector.collectMetadata(TestRepo01.class);
        // assert top level metadata
        assertThat(repoMeta.type()).isEqualTo(QUERY);
        assertThat(repoMeta.timeout()).isEqualTo(123);
        assertThat(repoMeta.queryMethods()).hasSize(4);
        assertThat(repoMeta.queryMethods().map(QueryMethodMeta::methodName))
                .containsExactlyInAnyOrder("ddlQuery", "dmlQueryNoParams", "dmlQueryIndexed", "dmlQueryNamed");
        // assert methods one by one
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "ddlQuery".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("ddlQuery"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(DDL);
            assertThat(methodMeta.querySQL()).isNull();
            assertThat(methodMeta.queryURI()).isEqualTo("load://query.here?query=ddl");
            assertThat(methodMeta.queryURICharset()).isEqualTo("US-ASCII");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams()).isEmpty();
            assertThat(methodMeta.extractParams()).isEmpty();
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "dmlQueryNoParams".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("dmlQueryNoParams"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(DML);
            assertThat(methodMeta.querySQL()).isEqualTo("DML query 01");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PARSE_QUERY);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams()).isEmpty();
            assertThat(methodMeta.extractParams()).isEmpty();
            assertThat(methodMeta.queryTimeout()).isEqualTo(0);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "dmlQueryIndexed".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("dmlQueryIndexed"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(DML);
            assertThat(methodMeta.querySQL()).isEqualTo("DML query 02");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    1, null, false, null, "int", null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    2, null, false, null, "varchar", null, null, null
                            )
                    );
            assertThat(methodMeta.extractParams()).isEmpty();
            assertThat(methodMeta.queryTimeout()).isEqualTo(321);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "dmlQueryNamed".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("dmlQueryNamed"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(DML);
            assertThat(methodMeta.querySQL()).isNull();
            assertThat(methodMeta.queryURI()).isEqualTo("load://query.here?query=dml03");
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isEqualTo(SomeComplexObject.class);
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    1, null, false, "id", "int", null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    2, null, false, "name", "varchar", null, null, null
                            )
                    );
            assertThat(methodMeta.extractParams()).isEmpty();
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
    }

    @Test
    public void testRepo02() {
        // this test focuses on provided parameters
        final RepositoryMeta repoMeta = collector.collectMetadata(TestRepo02.class);
        // assert top level metadata
        assertThat(repoMeta.type()).isEqualTo(QUERY);
        assertThat(repoMeta.timeout()).isEqualTo(-1);
        assertThat(repoMeta.queryMethods()).hasSize(6);
        assertThat(repoMeta.queryMethods().map(QueryMethodMeta::methodName))
                .containsExactlyInAnyOrder(
                        "selectIndexedNoParams",
                        "selectNamedNoParams",
                        "selectNamedNamed",
                        "selectIndexedIndexed",
                        "selectIndexedIndexed_02",
                        "selectNamedNamed_02"
                );
        // assert methods one by one
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "selectIndexedNoParams".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("selectIndexedNoParams"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(SELECT);
            assertThat(methodMeta.querySQL()).isEqualTo("SELECT 1");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams()).isEmpty();
            assertThat(methodMeta.extractParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    1, null, false, null, "rubItDaddy", null, "soft", null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    2, null, false, null, null, "keepGoing", null, "java.lang.Integer"
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    3, null, false, null, "mapItBabby", "makeItHot", "hard", "java.lang.Boolean"
                            )
                    );
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "selectNamedNoParams".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("selectNamedNoParams"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(SELECT);
            assertThat(methodMeta.querySQL()).isEqualTo("SELECT 2");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isEqualTo(SomeComplexObject.class);
            assertThat(methodMeta.injectParams()).isEmpty();
            assertThat(methodMeta.extractParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    1, "column1", false, "someProperty", "mapItBabby", "makeItHot", "hard", "java.lang.Boolean"
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    2, "column2", false, "otherProperty", "rubItDaddy", null, "soft", null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    3, "the_hole", false, "property.deep.inside.me", null, "keepGoing", null, "java.lang.Integer"
                            )
                    );
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "selectNamedNamed".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("selectNamedNamed"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(SELECT);
            assertThat(methodMeta.querySQL()).isEqualTo("SELECT 3");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isEqualTo(SomeComplexObject.class);
            assertThat(methodMeta.targetClass()).isEqualTo(SomeComplexObject.class);
            assertThat(methodMeta.injectParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    1, null, false, "someProperty", "mapItBabby", "makeItHot", "hard", "java.lang.Boolean"
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    2, null, false, "otherProperty", "rubItDaddy", null, "soft", null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    3, null, false, "property.deep.inside.me", null, "keepGoing", null, "java.lang.Integer"
                            )
                    );
            assertThat(methodMeta.extractParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    1, null, false, "p1", null, null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    2, null, false, "p2", null, null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    3, null, false, "p3", null, null, null, null
                            )
                    );
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "selectIndexedIndexed".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("selectIndexedIndexed"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(SELECT);
            assertThat(methodMeta.querySQL()).isEqualTo("SELECT 4");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    1, null, false, null, null, "keepGoing", null, "java.lang.Integer"
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    2, null, false, null, "rubItDaddy", null, "soft", null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    3, null, false, null, "mapItBabby", "makeItHot", "hard", "java.lang.Boolean"
                            )
                    );
            assertThat(methodMeta.extractParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    1, null, false, null, "mapp", null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    2, null, false, null, null, null, "BIGINT", "java.lang.Long"
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    3, null, false, null, null, "tagg", null, null
                            )
                    );
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "selectIndexedIndexed_02".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("selectIndexedIndexed_02"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(SELECT);
            assertThat(methodMeta.querySQL()).isEqualTo("SELECT 5");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PROVIDED);
            assertThat(methodMeta.sourceClass()).isNull();
            assertThat(methodMeta.targetClass()).isNull();
            assertThat(methodMeta.injectParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    1, null, false, null, null, null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.INJECTION,
                                    2, null, false, null, null, null, null, null
                            )
                    );
            assertThat(methodMeta.extractParams())
                    .containsExactly(
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    1, null, false, null, null, null, null, null
                            ),
                            new QueryParser.QueryParam(
                                    QueryParser.ParamType.EXTRACTION,
                                    2, null, false, null, null, null, null, null
                            )
                    );
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
        {
            final QueryMethodMeta methodMeta = repoMeta.queryMethods()
                    .find(qmm -> "selectNamedNamed_02".equals(qmm.methodName()))
                    .get();
            assertThat(methodMeta.methodName()).isEqualTo("selectNamedNamed_02"); // quite obvious
            assertThat(methodMeta.type()).isEqualTo(SELECT);
            assertThat(methodMeta.querySQL()).isEqualTo("SELECT 6");
            assertThat(methodMeta.queryURI()).isNull();
            assertThat(methodMeta.queryURICharset()).isEqualTo("UTF-8");
            assertThat(methodMeta.paramStrat()).isEqualTo(PARSE_QUERY);
            assertThat(methodMeta.sourceClass()).isEqualTo(SomeComplexObject.class);
            assertThat(methodMeta.targetClass()).isEqualTo(SomeComplexObject.class);
            assertThat(methodMeta.injectParams()).isEmpty();
            assertThat(methodMeta.extractParams()).isEmpty();
            assertThat(methodMeta.queryTimeout()).isEqualTo(-1);
        }
    }


    @SimpleOrmRepo(type = QUERY, timeout = 123)
    public interface TestRepo01 {

        @SimpleQuery(type = DDL, parameters = PROVIDED)
        @QuerySource(uri = "load://query.here?query=ddl", uriCharset = "US-ASCII")
        Query<Void, Void> ddlQuery();

        @SimpleQuery(type = DML, parameters = PARSE_QUERY, timeout = 0)
        @QuerySource(query = "DML query 01")
        Query<Void, Void> dmlQueryNoParams();

        @SimpleQuery(type = DML, parameters = PROVIDED, timeout = 321)
        @QuerySource(query = "DML query 02")
        @InjectParam(mapper = "int")
        @InjectParam(mapper = "varchar")
        Query<Seq<Object>, Void> dmlQueryIndexed();

        @SimpleQuery(type = DML, parameters = PROVIDED, sourceClass = SomeComplexObject.class)
        @QuerySource(uri = "load://query.here?query=dml03")
        @InjectParam(prop = "id", mapper = "int")
        @InjectParam(prop = "name", mapper = "varchar")
        Query<SomeComplexObject, Void> dmlQueryNamed();

    }

    @SimpleOrmRepo(type = QUERY)
    public interface TestRepo02 {

        @SimpleQuery(type = SELECT, parameters = PROVIDED)
        @QuerySource(query = "SELECT 1")
        @ExtractParam(index = 3, mapper = "mapItBabby", tag = "makeItHot", jdbc = "hard", java = Boolean.class)
        @ExtractParam(index = 1, mapper = "rubItDaddy", jdbc = "soft")
        @ExtractParam(index = 2, tag = "keepGoing", java = Integer.class)
        Query<Void, Seq<Object>> selectIndexedNoParams();

        @SimpleQuery(type = SELECT, parameters = PROVIDED, targetClass = SomeComplexObject.class)
        @QuerySource(query = "SELECT 2")
        @ExtractParam(label = "column1", prop = "someProperty", mapper = "mapItBabby", tag = "makeItHot", jdbc = "hard", java = Boolean.class)
        @ExtractParam(label = "column2", prop = "otherProperty", mapper = "rubItDaddy", jdbc = "soft")
        @ExtractParam(label = "the_hole", prop = "property.deep.inside.me", tag = "keepGoing", java = Integer.class)
        Query<Void, SomeComplexObject> selectNamedNoParams();

        @SimpleQuery(type = SELECT, parameters = PROVIDED, sourceClass = SomeComplexObject.class, targetClass = SomeComplexObject.class)
        @QuerySource(query = "SELECT 3")
        @InjectParam(prop = "someProperty", mapper = "mapItBabby", tag = "makeItHot", jdbc = "hard", java = Boolean.class)
        @InjectParam(prop = "otherProperty", mapper = "rubItDaddy", jdbc = "soft")
        @InjectParam(prop = "property.deep.inside.me", tag = "keepGoing", java = Integer.class)
        @ExtractParam(prop = "p1")
        @ExtractParam(prop = "p2")
        @ExtractParam(prop = "p3")
        Query<SomeComplexObject, SomeComplexObject> selectNamedNamed();

        @SimpleQuery(type = SELECT, parameters = PROVIDED)
        @QuerySource(query = "SELECT 4")
        @InjectParam(index = 3, mapper = "mapItBabby", tag = "makeItHot", jdbc = "hard", java = Boolean.class)
        @InjectParam(index = 2, mapper = "rubItDaddy", jdbc = "soft")
        @InjectParam(index = 1, tag = "keepGoing", java = Integer.class)
        @ExtractParam(index = 1, mapper = "mapp")
        @ExtractParam(index = 3, tag = "tagg")
        @ExtractParam(index = 2, jdbc = "BIGINT", java = long.class)
        Query<Seq<Object>, Seq<Object>> selectIndexedIndexed();

        @SimpleQuery(type = SELECT, parameters = PROVIDED)
        @QuerySource(query = "SELECT 5")
        @InjectParam
        @InjectParam
        @ExtractParam
        @ExtractParam
        Query<Seq<Object>, Seq<Object>> selectIndexedIndexed_02();

        @SimpleQuery(type = SELECT, parameters = PARSE_QUERY, sourceClass = SomeComplexObject.class, targetClass = SomeComplexObject.class)
        @QuerySource(query = "SELECT 6")
        Query<SomeComplexObject, SomeComplexObject> selectNamedNamed_02();

    }

    public static class SomeComplexObject {
        // just to use something as class in named strategies
        // not some nonsense like Object.class
    }

}
