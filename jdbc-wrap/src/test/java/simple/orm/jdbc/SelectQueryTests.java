package simple.orm.jdbc;

import io.vavr.collection.Seq;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.map.InjectorsExtractors;
import simple.orm.jdbc.param.BasicTypes;
import simple.orm.jdbc.param.ParameterType;
import simple.orm.jdbc.param.ParameterTypeImpl;
import simple.orm.jdbc.query.IndexedNamedQuery;
import simple.orm.jdbc.query.IndexedQuery;
import simple.orm.jdbc.query.NamedIndexedQuery;
import simple.orm.jdbc.query.NamedQuery;
import simple.orm.jdbc.query.QueryFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests of SELECT queries.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SelectQueryTests extends BaseH2Test {

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
        createTables();
    }

    @AfterAll
    void tearDown() throws SQLException {
        database.close();
        dropAllObjects();
    }

    @BeforeEach
    void setUpEach() throws SQLException {
        seedSomeData();
    }

    private void createTables() throws SQLException {
        try (java.sql.Connection conn = directConnect()) {
            conn.createStatement().executeUpdate(
                    """
                    CREATE TABLE table_one\
                     (id INT PRIMARY KEY,\
                      col_d DOUBLE PRECISION NULL,\
                      col_str1 VARCHAR(256) NULL,\
                      col_nu NUMERIC(30,10) NULL,\
                      col_str2 VARCHAR(256) NULL,\
                      col_date DATE NULL,\
                      col_time TIME NULL,\
                      col_timestamp TIMESTAMP(9) NULL\
                    )\
                    """);
        }
    }

    private void seedSomeData() throws SQLException {
        try (Connection conn = directConnect()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("TRUNCATE TABLE table_one");
            stmt.executeUpdate(
                    """
                    INSERT INTO table_one\
                     VALUES (1,\
                      10.10,   'p1',\
                      100.001, 'p2',\
                      '2000-01-31',\
                      '12:30:55.555666777',\
                      '2001-02-13 10:20:30'\
                    )\
                    """);
            stmt.executeUpdate(
                    """
                    INSERT INTO table_one\
                     VALUES (2,\
                      -10.10,   'n1',\
                      -100.001, 'n2',\
                      '2025-10-24',\
                      '13:20:00.000000111',\
                      '2025-10-24 13:20:30.123123123'\
                    )""");
        }
    }

    @Test
    public void testEmptySelect() throws SQLException {
        // test (with result.hasNext)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(() -> result.nextRow()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test (with result.exactlySingleRow)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.exactlySingleRow()).isNull();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(() -> result.nextRow()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test (with result.extractAll)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.extractAll()).isEmpty();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(() -> result.nextRow()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test closed throw JdbcException
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            result.close();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(() -> result.hasNextRow()).isInstanceOf(JdbcException.class);
            assertThatCode(() -> result.nextRow()).isInstanceOf(JdbcException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(JdbcException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(JdbcException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
    }

    @Test
    public void testEmptySelectShouldAutoCloseFalse() throws SQLException {
        // test (with result.hasNext)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            result.setShouldBeClosedAutomatically(false);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isFalse();
            assertThatCode(() -> result.nextRow()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isFalse();
            conn.close();
        }
        // test (with result.exactlySingleRow)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            result.setShouldBeClosedAutomatically(false);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.exactlySingleRow()).isNull();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isFalse();
            assertThatCode(() -> result.nextRow()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isFalse();
            conn.close();
        }
        // test (with result.extractAll)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?", 10,
                    InjectorsExtractors.indexedInjector().param(BasicTypes.INTEGER).build(),
                    InjectorsExtractors.indexedExtractor().param(BasicTypes.INTEGER).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            result.setShouldBeClosedAutomatically(false);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.extractAll()).isEmpty();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isFalse();
            assertThatCode(() -> result.nextRow()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.extractAll()).isInstanceOf(IllegalStateException.class);
            assertThatCode(() -> result.exactlySingleRow()).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isFalse();
            conn.close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSelectIndexed() throws SQLException {
        // test 1
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    10,
                    InjectorsExtractors.indexedInjector()
                            .params(INT_STRING_TYPE)
                            .build(),
                    InjectorsExtractors.indexedExtractor()
                            .params(
                                    INT_STRING_TYPE,
                                    BasicTypes.DOUBLE, BasicTypes.NUMERIC, BasicTypes.VARCHAR, BasicTypes.VARCHAR,
                                    BasicTypes.DATE_SQL, BasicTypes.TIME_SQL, BasicTypes.TIMESTAMP_SQL
                            )
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, "1");
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    "1", 10.1d, new BigDecimal("100.0010000000"), "p1", "p2",
                    new Date(100, 0, 31),
                    new Time(12, 30, 56), // nano part supported by h2 is rounded (up)
                    new Timestamp(101, 1, 13, 10, 20, 30, 0)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 2
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQuery(
                    """
                    SELECT col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=? AND col_d=? AND col_nu=? AND col_str1=? AND col_str2=?\
                    """,
                    10,
                    InjectorsExtractors.indexedInjector()
                            .params(
                                    INT_STRING_TYPE,
                                    BasicTypes.DOUBLE, BasicTypes.NUMERIC, BasicTypes.VARCHAR, BasicTypes.VARCHAR
                            )
                            .build(),
                    InjectorsExtractors.indexedExtractor()
                            .params(BasicTypes.DATE_SQL, BasicTypes.TIME_SQL, BasicTypes.TIMESTAMP_SQL)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, "2", -10.1d, new BigDecimal("-100.0010000000"), "n1", "n2");
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    new Date(125, 9, 24),
                    new Time(13, 20, 0), // nano part supported by h2 is rounded (down this time)
                    new Timestamp(125, 9, 24, 13, 20, 30, 123123123)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
    }

    @Test
    public void testSelectNamed() throws SQLException {
        // test 1, extraction by index
        {
            simple.orm.jdbc.Connection conn = database.connect();
            NamedQuery<HasId, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=:id\
                    """,
                    10,
                    InjectorsExtractors.namedInjector(HasId.class)
                            .param("id", BasicTypes.INTEGER)
                            .build(),
                    InjectorsExtractors.namedExtractorByIndex(NamedRow2.class)
                            .param(BasicTypes.INTEGER, "id")
                            .param(BasicTypes.DOUBLE, "doublePrecision")
                            .param(BasicTypes.NUMERIC, "num")
                            .param(BasicTypes.VARCHAR, "str1")
                            .param(BasicTypes.VARCHAR, "str2")
                            .param(BasicTypes.DATE_STRING, "date")
                            .param(BasicTypes.TIME_STRING, "time")
                            .param(BasicTypes.TIMESTAMP_STRING, "timestamp")
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query, new HasId(1));
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(1);
            assertThat(row.doublePrecision).isEqualTo(10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("100.0010000000"));
            assertThat(row.str1).isEqualTo("p1");
            assertThat(row.getStr2()).isEqualTo("p2");
            assertThat(row.date).isEqualTo("2000-01-31");
            assertThat(row.time).isEqualTo("12:30:56"); // as it is extracted through java.sql.Date and then converted to String, nano is rounded on extraction
            assertThat(row.timestamp).isEqualTo("2001-02-13 10:20:30.0");
            conn.close();
        }
        // test 2, extraction by label (some labels not matching property names)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            NamedQuery<HasId, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=:someid\
                    """,
                    10,
                    InjectorsExtractors.namedInjector(HasId.class)
                            .param("someid", BasicTypes.INTEGER, "id")
                            .build(),
                    InjectorsExtractors.namedExtractorByLabel(NamedRow2.class)
                            .param("id", BasicTypes.INTEGER)
                            .param("col_str1", BasicTypes.VARCHAR, "str1")
                            .param("col_str2", BasicTypes.VARCHAR, "str2")
                            .param("doublePrecision", BasicTypes.DOUBLE)
                            .param("num", BasicTypes.NUMERIC)
                            .param("col_timestamp", BasicTypes.TIMESTAMP_STRING, "timestamp")
                            .param("col_date", BasicTypes.DATE_STRING, "date")
                            .param("col_time", BasicTypes.TIME_STRING, "time")
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query, new HasId(2));
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(2);
            assertThat(row.doublePrecision).isEqualTo(-10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("-100.0010000000"));
            assertThat(row.str1).isEqualTo("n1");
            assertThat(row.getStr2()).isEqualTo("n2");
            assertThat(row.date).isEqualTo("2025-10-24");
            assertThat(row.time).isEqualTo("13:20:00"); // as it is extracted through java.sql.Date and then converted to String, nano is rounded on extraction
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    @Test
    public void testSelectIndexedNamed() throws SQLException {
        // test 1, extraction by index
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedNamedQuery<NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    10,
                    InjectorsExtractors.indexedInjector()
                            .param(INT_STRING_TYPE)
                            .build(),
                    InjectorsExtractors.namedExtractorByIndex(NamedRow2.class)
                            .param(BasicTypes.INTEGER, "id")
                            .param(BasicTypes.DOUBLE, "doublePrecision")
                            .param(BasicTypes.NUMERIC, "num")
                            .param(BasicTypes.VARCHAR, "str1")
                            .param(BasicTypes.VARCHAR, "str2")
                            .param(BasicTypes.DATE_STRING, "date")
                            .param(BasicTypes.TIME_STRING, "time")
                            .param(BasicTypes.TIMESTAMP_STRING, "timestamp")
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query, "1");
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(1);
            assertThat(row.doublePrecision).isEqualTo(10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("100.0010000000"));
            assertThat(row.str1).isEqualTo("p1");
            assertThat(row.getStr2()).isEqualTo("p2");
            assertThat(row.date).isEqualTo("2000-01-31");
            assertThat(row.time).isEqualTo("12:30:56"); // as it is extracted through java.sql.Date and then converted to String, nano is rounded on extraction
            assertThat(row.timestamp).isEqualTo("2001-02-13 10:20:30.0");
            conn.close();
        }
        // test 2, extraction by label (some labels not matching property names)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedNamedQuery<NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=? AND col_str1=? AND col_str2=?\
                    """,
                    10,
                    InjectorsExtractors.indexedInjector()
                            .params(BasicTypes.INTEGER, BasicTypes.VARCHAR, BasicTypes.VARCHAR)
                            .build(),
                    InjectorsExtractors.namedExtractorByLabel(NamedRow2.class)
                            .param("id", BasicTypes.INTEGER)
                            .param("doublePrecision", BasicTypes.DOUBLE)
                            .param("num", BasicTypes.NUMERIC)
                            .param("col_timestamp", BasicTypes.TIMESTAMP_STRING, "timestamp")
                            .param("col_date", BasicTypes.DATE_STRING, "date")
                            .param("col_time", BasicTypes.TIME_STRING, "time")
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query, 2, "n1", "n2");
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(2);
            assertThat(row.doublePrecision).isEqualTo(-10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("-100.0010000000"));
            assertThat(row.str1).isNull();
            assertThat(row.getStr2()).isNull();
            assertThat(row.date).isEqualTo("2025-10-24");
            assertThat(row.time).isEqualTo("13:20:00"); // as it is extracted through java.sql.Date and then converted to String, nano is rounded on extraction
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSelectNamedIndexed() throws SQLException {
        // test 1
        {
            simple.orm.jdbc.Connection conn = database.connect();
            NamedIndexedQuery<HasId> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=:id\
                    """,
                    10,
                    InjectorsExtractors.namedInjector(HasId.class)
                            .param("id", BasicTypes.INTEGER)
                            .build(),
                    InjectorsExtractors.indexedExtractor()
                            .params(
                                    INT_STRING_TYPE,
                                    BasicTypes.DOUBLE, BasicTypes.NUMERIC, BasicTypes.VARCHAR, BasicTypes.VARCHAR,
                                    BasicTypes.DATE_SQL, BasicTypes.TIME_SQL, BasicTypes.TIMESTAMP_SQL
                            )
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, new HasId(1));
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    "1", 10.1d, new BigDecimal("100.0010000000"), "p1", "p2",
                    new Date(100, 0, 31),
                    new Time(12, 30, 56), // nano part supported by h2 is rounded (up)
                    new Timestamp(101, 1, 13, 10, 20, 30, 0)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 2
        {
            simple.orm.jdbc.Connection conn = database.connect();
            NamedIndexedQuery<NamedRow1> query = QFACTORY.selectQuery(
                    """
                    SELECT col_date, col_time, col_timestamp, col_d, col_nu\
                     FROM table_one\
                     WHERE id=:someid AND col_str1=:str1 AND col_str2=:str2\
                    """,
                    10,
                    InjectorsExtractors.namedInjector(NamedRow1.class)
                            .param("someid", BasicTypes.INTEGER, "id")
                            .param("str2", BasicTypes.VARCHAR)
                            .param("str1", BasicTypes.VARCHAR)
                            .build(),
                    InjectorsExtractors.indexedExtractor()
                            .params(
                                    BasicTypes.DATE_SQL, BasicTypes.TIME_SQL, BasicTypes.TIMESTAMP_SQL,
                                    BasicTypes.DOUBLE, BasicTypes.NUMERIC
                            )
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, new NamedRow1(2, "n1", "n2"));
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    new Date(125, 9, 24),
                    new Time(13, 20, 0), // nano part supported by h2 is rounded (down this time)
                    new Timestamp(125, 9, 24, 13, 20, 30, 123123123),
                    -10.1d,
                    new BigDecimal("-100.0010000000")
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSelectWithoutParameters() throws SQLException {
        // test 1
        {
            simple.orm.jdbc.Connection conn = database.connect();
            IndexedQuery query = QFACTORY.selectQueryWithoutParameters(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=1\
                    """,
                    10,
                    InjectorsExtractors.indexedExtractor()
                            .params(
                                    INT_STRING_TYPE,
                                    BasicTypes.DOUBLE, BasicTypes.NUMERIC, BasicTypes.VARCHAR, BasicTypes.VARCHAR,
                                    BasicTypes.DATE_SQL, BasicTypes.TIME_SQL, BasicTypes.TIMESTAMP_SQL
                            )
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query);
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    "1", 10.1d, new BigDecimal("100.0010000000"), "p1", "p2",
                    new Date(100, 0, 31),
                    new Time(12, 30, 56), // nano part supported by h2 is rounded (up)
                    new Timestamp(101, 1, 13, 10, 20, 30, 0)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 2, extraction by index
        {
            simple.orm.jdbc.Connection conn = database.connect();
            NamedQuery<Void, NamedRow2> query = QFACTORY.selectQueryWithoutParameters(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=1\
                    """,
                    10,
                    InjectorsExtractors.namedExtractorByIndex(NamedRow2.class)
                            .param(BasicTypes.INTEGER, "id")
                            .param(BasicTypes.DOUBLE, "doublePrecision")
                            .param(BasicTypes.NUMERIC, "num")
                            .param(BasicTypes.VARCHAR, "str1")
                            .param(BasicTypes.VARCHAR, "str2")
                            .param(BasicTypes.DATE_STRING, "date")
                            .param(BasicTypes.TIME_STRING, "time")
                            .param(BasicTypes.TIMESTAMP_STRING, "timestamp")
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query);
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(1);
            assertThat(row.doublePrecision).isEqualTo(10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("100.0010000000"));
            assertThat(row.str1).isEqualTo("p1");
            assertThat(row.getStr2()).isEqualTo("p2");
            assertThat(row.date).isEqualTo("2000-01-31");
            assertThat(row.time).isEqualTo("12:30:56"); // as it is extracted through java.sql.Date and then converted to String, nano is rounded on extraction
            assertThat(row.timestamp).isEqualTo("2001-02-13 10:20:30.0");
            conn.close();
        }
        // test 3, extraction by label (some labels not matching property names)
        {
            simple.orm.jdbc.Connection conn = database.connect();
            NamedQuery<Void, NamedRow2> query = QFACTORY.selectQueryWithoutParameters(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=2\
                    """,
                    10,
                    InjectorsExtractors.namedExtractorByLabel(NamedRow2.class)
                            .param("id", BasicTypes.INTEGER)
                            .param("col_str1", BasicTypes.VARCHAR, "str1")
                            .param("col_str2", BasicTypes.VARCHAR, "str2")
                            .param("doublePrecision", BasicTypes.DOUBLE)
                            .param("num", BasicTypes.NUMERIC)
                            .param("col_timestamp", BasicTypes.TIMESTAMP_STRING, "timestamp")
                            .param("col_date", BasicTypes.DATE_STRING, "date")
                            .param("col_time", BasicTypes.TIME_STRING, "time")
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query);
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(2);
            assertThat(row.doublePrecision).isEqualTo(-10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("-100.0010000000"));
            assertThat(row.str1).isEqualTo("n1");
            assertThat(row.getStr2()).isEqualTo("n2");
            assertThat(row.date).isEqualTo("2025-10-24");
            assertThat(row.time).isEqualTo("13:20:00"); // as it is extracted through java.sql.Date and then converted to String, nano is rounded on extraction
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    private static final ParameterType<Integer, String> INT_STRING_TYPE = new ParameterTypeImpl<>(
            JDBCType.INTEGER, Integer.class, String.class,
            Integer::valueOf,
            Object::toString
    );

    private static class NamedRow2 extends NamedRow1 {
        protected Double doublePrecision;
        protected BigDecimal num;
        protected String date;
        protected String time;
        protected String timestamp;

        public NamedRow2() {
        }

        public NamedRow2(Integer id, String str1, String str2, Double doublePrecision, BigDecimal num, String date, String time, String timestamp) {
            super(id, str1, str2);
            this.doublePrecision = doublePrecision;
            this.num = num;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    private static class NamedRow1 extends HasId {
        protected String str1;
        protected String str2;

        public NamedRow1() {
        }

        public NamedRow1(Integer id, String str1, String str2) {
            super(id);
            this.str1 = str1;
            this.str2 = str2;
        }

        public String getStr2() {
            return str2;
        }
    }

    private static class HasId {
        protected Integer id;

        public HasId() {
        }

        public HasId(Integer id) {
            this.id = id;
        }
    }

}
