package simple.orm.repo.impl.meta;

import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.repo.RepositoryBuilderException;
import simple.orm.repo.anno.ExtractParam;
import simple.orm.repo.anno.InjectParam;
import simple.orm.repo.anno.ParamInjector;
import simple.orm.repo.anno.ParameterStrategy;
import simple.orm.repo.anno.QuerySource;
import simple.orm.repo.anno.RepoType;
import simple.orm.repo.anno.ResultExtractor;
import simple.orm.repo.anno.SimpleOrmRepo;
import simple.orm.repo.anno.SimpleQuery;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests on {@link MetadataCollector}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataCollectorValidationTest {

    // has no state
    private final MetadataCollector collector = new MetadataCollector();

    @Test
    public void testPreParameters() {
        // validations of annotations not related to parameters
        {
            assertThatCode(() -> collector.collectMetadata(Test_noSimpleOrmRepo.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageContaining("with SimpleOrmRepo");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_wrongMethod_return.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("wrongReturnTypeMethod")
                    .hasMessageContaining("should return simple.orm.jdbc.query.Query");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_wrongMethod_args.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("hasArgumentsMethod")
                    .hasMessageContaining("should have no parameters");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_noSimpleQuery.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("with SimpleQuery");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_noQuerySource.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("with QuerySource");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_wrongQuerySource_allEmpty.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("query SQL")
                    .hasMessageContaining("query URI")
                    .hasMessageNotContaining("both");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_wrongQuerySource_bothSQLandURI.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("query SQL")
                    .hasMessageContaining("query URI")
                    .hasMessageContaining("not both");
        }
    }

    @Test
    public void testParametersPresentAbsent() {
        // validations errors with parameters present or absent
        {
            assertThatCode(() -> collector.collectMetadata(Test_ddl_with_extract_param.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("should have no ExtractParam")
                    .hasMessageContaining("DDL");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_dml_with_extract_param.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("should have no ExtractParam")
                    .hasMessageContaining("DML");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_select_without_extract_param.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("must be annotated with at least one ExtractParam")
                    .hasMessageContaining("SELECT");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_parse_query_has_inject_params.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("should have no InjectParam and ExtractParam")
                    .hasMessageContaining("PARSE_QUERY");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_parse_query_has_extract_params.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("should have no InjectParam and ExtractParam")
                    .hasMessageContaining("PARSE_QUERY");
        }
    }

    @Test
    public void testInjectionParameters() {
        // injection parameters validation
        {
            assertThatCode(() -> collector.collectMetadata(Test_inject_params_not_all_indexed.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("must be either properly indexed, either have no indexes");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_inject_params_indexed_with_gaps.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("indexing is broken");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_inject_params_indexed_has_prop_names.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("have no property names specified for each InjectParam");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_inject_params_named_has_no_prop_names.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("have property name specified for each InjectParam");
        }
    }

    @Test
    public void testExtractionParameters() {
        // extraction parameters validation
        {
            assertThatCode(() -> collector.collectMetadata(Test_extract_params_not_all_labels.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("must either have label specified, either have none");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_extract_params_not_all_indexed.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("either have proper indexes (>0), either have no indexes at all");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_extract_params_wrong_indexes.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("either have proper indexes (>0), either have no indexes at all");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_extract_params_not_all_indexed_ignored.class))
                    .doesNotThrowAnyException(); // has all labels - indexes ignored
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_extract_params_indexed_has_prop_names.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("have no property names specified for each ExtractParam");
        }
        {
            assertThatCode(() -> collector.collectMetadata(Test_extract_params_named_has_no_prop_names.class))
                    .isInstanceOf(RepositoryBuilderException.class)
                    .hasMessageStartingWith("query")
                    .hasMessageContaining("have property name specified for each ExtractParam");
        }
    }

    // --- errors before validating parameters

    public interface Test_noSimpleOrmRepo {
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_wrongMethod_return {
        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = "SQL")
        int wrongReturnTypeMethod();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_wrongMethod_args {
        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = "SQL")
        Query<Void, Void> hasArgumentsMethod(int argument);
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_noSimpleQuery {
        @QuerySource(query = "SQL")
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_noQuerySource {
        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PARSE_QUERY)
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_wrongQuerySource_allEmpty {
        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource()
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_wrongQuerySource_bothSQLandURI {
        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = "SQL", uri = "http://go.there")
        Query<Void, Void> query();
    }

    // --- errors parameters present/absent

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_ddl_with_extract_param {
        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ResultExtractor(@ExtractParam)
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_dml_with_extract_param {
        @SimpleQuery(type = QueryType.DML, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ExtractParam
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_select_without_extract_param {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @InjectParam
        Query<Void, Seq<Object>> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_parse_query_has_inject_params {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = "SQL")
        @ParamInjector(@InjectParam)
        Query<Void, Seq<Object>> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_parse_query_has_extract_params {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = "SQL")
        @ExtractParam
        Query<Void, Seq<Object>> query();
    }

    // --- injection parameters validation

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_inject_params_not_all_indexed {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @InjectParam(index = 1)
        @InjectParam
        @ExtractParam
        Query<Seq<Object>, Seq<Object>> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_inject_params_indexed_with_gaps {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @InjectParam(index = 1)
        @InjectParam(index = 3)
        @ExtractParam
        Query<Seq<Object>, Seq<Object>> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_inject_params_indexed_has_prop_names {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @InjectParam(index = 1, prop = "propName")
        @InjectParam(index = 2)
        @ExtractParam
        Query<Seq<Object>, Seq<Object>> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_inject_params_named_has_no_prop_names {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED, sourceClass = SomeComplexObject.class)
        @QuerySource(query = "SQL")
        @InjectParam(index = 1)
        @InjectParam(index = 2, prop = "propName")
        @ExtractParam
        Query<SomeComplexObject, Seq<Object>> query();
    }

    // --- extraction parameters validation

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_extract_params_not_all_labels {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ExtractParam(index = 1, label = "label")
        @ExtractParam(index = 2)
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_extract_params_not_all_indexed {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ExtractParam(index = 1)
        @ExtractParam
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_extract_params_wrong_indexes {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ExtractParam(index = 1)
        @ExtractParam(index = -3)
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_extract_params_not_all_indexed_ignored {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ExtractParam(index = 1, label = "label1")
        @ExtractParam(label = "label2")
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_extract_params_indexed_has_prop_names {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "SQL")
        @ExtractParam(index = 1, prop = "propName")
        @ExtractParam(index = 2)
        Query<Void, Void> query();
    }

    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface Test_extract_params_named_has_no_prop_names {
        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PROVIDED, targetClass = SomeComplexObject.class)
        @QuerySource(query = "SQL")
        @ExtractParam(index = 1)
        @ExtractParam(index = 2, prop = "propName")
        Query<Void, Void> query();
    }

    // --- SomeComplexObject

    public static class SomeComplexObject {
        // just to use something as class in named strategies
        // not some nonsense like Object.class
    }

}
