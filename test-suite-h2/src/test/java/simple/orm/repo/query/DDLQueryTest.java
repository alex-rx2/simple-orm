package simple.orm.repo.query;

import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.BaseH2Test;
import simple.orm.h2.H2Mappers;
import simple.orm.h2.H2Types;
import simple.orm.jdbc.Connection;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.QueryParser;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.repo.ImplementationStyle;
import simple.orm.repo.RepositoryBuilder;
import simple.orm.repo.SQLLoader;
import simple.orm.repo.anno.ParameterStrategy;
import simple.orm.repo.anno.QuerySource;
import simple.orm.repo.anno.RepoType;
import simple.orm.repo.anno.SimpleOrmRepo;
import simple.orm.repo.anno.SimpleQuery;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of DDL queries.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DDLQueryTest extends BaseH2Test {

    private DatabaseAccessPoint database;
    private RepositoryBuilder repositoryBuilder;

    @BeforeAll
    void setUp() throws SQLException {
        final ReflectionsFinder reflectionsFinder =ReflectionsFinder.defaultFinder();
        repositoryBuilder = RepositoryBuilder.of(
                SQLLoader.defaultLoader(),
                QueryParser.defaultParser(),
                QueryBuilder.of(
                        H2Types.collection(),
                        ()-> MappersFinder.defaultFinder(H2Mappers.collection()),
                        ()->reflectionsFinder
                )
        );
        dropAllObjects();
        database = DatabaseAccessPoint.builder()
                .driverClass(h2DriverClass)
                .connectionUrl(h2InMemUrl)
                .connectionProperties(h2ConnectionProperties)
                .build();
    }

    @AfterAll
    void tearDown() throws SQLException {
        database.close();
        dropAllObjects();
    }

    @BeforeEach
    void beforeTest() throws SQLException {
        dropAllObjects();
    }

    @Test
    public void testCreateAndAlterTable() throws SQLException {
        // test
        {
            DDLRepository repository = repositoryBuilder.buildRepository(DDLRepository.class, ImplementationStyle.JAVA_PROXY);
            Connection conn = database.connect();
            conn.executeDDLQuery(repository.createTable());
            conn.executeDDLQuery(repository.alterTable());
            conn.close();
        }
        // verify
        {
            Seq<Tuple4<String, String, String, Integer>> columns = List.empty();
            try (java.sql.Connection conn = directConnect()) {
                ResultSet rs = conn.getMetaData().getColumns("", "", "TEST_TABLE", null);
                while (rs.next()) {
                    columns = columns.append(Tuple.of(
                            rs.getString("COLUMN_NAME"),
                            rs.getString("TYPE_NAME"),
                            rs.getString("IS_NULLABLE"),
                            rs.getInt("COLUMN_SIZE")
                    ));
                }
            }
            assertThat(columns).containsExactlyInAnyOrder(
                    Tuple.of("ID", "INTEGER", "NO", 32),
                    Tuple.of("SOME_VALUE", "CHARACTER VARYING", "YES", 255),
                    Tuple.of("SOME_NUMBER", "INTEGER", "NO", 32)
            );
        }
    }

    @Test
    public void testCreateAndAlterTable_Lazy() throws SQLException {
        // test
        {
            DDLRepository repository = repositoryBuilder.buildRepository(DDLRepository.class, ImplementationStyle.JAVA_PROXY_LAZY);
            Connection conn = database.connect();
            conn.executeAnyQuery(repository.createTable());
            conn.executeAnyQuery(repository.alterTable());
            conn.close();
        }
        // verify
        {
            Seq<Tuple4<String, String, String, Integer>> columns = List.empty();
            try (java.sql.Connection conn = directConnect()) {
                ResultSet rs = conn.getMetaData().getColumns("", "", "TEST_TABLE", null);
                while (rs.next()) {
                    columns = columns.append(Tuple.of(
                            rs.getString("COLUMN_NAME"),
                            rs.getString("TYPE_NAME"),
                            rs.getString("IS_NULLABLE"),
                            rs.getInt("COLUMN_SIZE")
                    ));
                }
            }
            assertThat(columns).containsExactlyInAnyOrder(
                    Tuple.of("ID", "INTEGER", "NO", 32),
                    Tuple.of("SOME_VALUE", "CHARACTER VARYING", "YES", 255),
                    Tuple.of("SOME_NUMBER", "INTEGER", "NO", 32)
            );
        }
    }

    @SimpleOrmRepo(type = RepoType.QUERY, timeout = 3)
    public interface DDLRepository {

        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "CREATE TABLE test_table (id INT PRIMARY KEY, some_value VARCHAR(255) NULL)")
        Query<Void, Void> createTable();

        @SimpleQuery(type = QueryType.DDL, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(query = "ALTER TABLE test_table ADD COLUMN some_number INT NOT NULL")
        Query<Void, Void> alterTable();

    }

}
