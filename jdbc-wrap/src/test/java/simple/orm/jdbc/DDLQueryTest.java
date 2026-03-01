package simple.orm.jdbc;

import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.query.QueryFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of DDL queries.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DDLQueryTest extends BaseH2Test {

    private static final QueryFactory QFACTORY = QueryFactory.defaultFactory();

    private DatabaseAccessPoint database;

    @BeforeAll
    void setUp() throws SQLException {
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
    public void testCreateTable() throws SQLException {
        // test
        {
            Connection conn = database.connect(10);
            conn.executeDDLQuery(
                    QFACTORY.ddlQuery("CREATE TABLE test_table (id INT PRIMARY KEY, some_value VARCHAR(255) NULL)")
            );
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
                    Tuple.of("SOME_VALUE", "CHARACTER VARYING", "YES", 255)
            );
        }
    }

    @Test
    public void testExecuteAnyQuery() throws SQLException {
        // test
        {
            Connection conn = database.connect(10);
            conn.executeAnyQuery(
                    QFACTORY.ddlQuery("CREATE TABLE test_table (id INT PRIMARY KEY, some_value VARCHAR(255) NULL)")
            );
            conn.executeAnyQuery(
                    QFACTORY.ddlQuery("ALTER TABLE test_table ADD COLUMN some_number INT NOT NULL")
            );
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

}
