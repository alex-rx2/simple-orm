package simple.orm.jdbc;

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
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryFactory;
import simple.orm.mapping.builder.InjectorsExtractors;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests of SELECT queries.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SelectQueryTest extends BaseH2Test {

    private static final QueryFactory QFACTORY = QueryFactory.instance();

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
    void beforeTest() throws SQLException {
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
                      col_time TIME(9) NULL,\
                      col_timestamp TIMESTAMP(9) NULL\
                    )\
                    """);
        }
    }

    private void seedSomeData() throws SQLException {
        try (java.sql.Connection conn = directConnect()) {
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
                    )\
                    """);
        }
    }

    @Test
    public void testEmptySelect() {
        // test (with result.hasNext)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(result::nextRow).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test (with result.exactlySingleRow)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.exactlySingleRow()).isNull();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(result::nextRow).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test (with result.extractAll)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.extractAll()).isEmpty();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(result::nextRow).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test closed throw JdbcException
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.isClosed()).isFalse();
            result.close();
            assertThat(result.isClosed()).isTrue();
            assertThatCode(result::hasNextRow).isInstanceOf(JdbcException.class);
            assertThatCode(result::nextRow).isInstanceOf(JdbcException.class);
            assertThatCode(result::extractAll).isInstanceOf(JdbcException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(JdbcException.class);
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
    }

    @Test
    public void testEmptySelectShouldAutoCloseFalse() {
        // test (with result.hasNext)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            result.setShouldBeClosedAutomatically(false);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isFalse();
            assertThatCode(result::nextRow).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isFalse();
            conn.close();
        }
        // test (with result.exactlySingleRow)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            result.setShouldBeClosedAutomatically(false);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.exactlySingleRow()).isNull();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isFalse();
            assertThatCode(result::nextRow).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isFalse();
            conn.close();
        }
        // test (with result.extractAll)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    "SELECT id FROM table_one WHERE id=?",
                    InjectorsExtractors.indexedInjector(H2Mappers.collection()).param(H2Mappers.INT).build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection()).param(H2Mappers.INT).build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            result.setShouldBeClosedAutomatically(false);
            assertThat(result.isClosed()).isFalse();
            assertThat(result.extractAll()).isEmpty();
            assertThat(result.hasNextRow()).isFalse();
            assertThat(result.isClosed()).isFalse();
            assertThatCode(result::nextRow).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::extractAll).isInstanceOf(IllegalStateException.class);
            assertThatCode(result::exactlySingleRow).isInstanceOf(IllegalStateException.class);
            assertThat(result.isClosed()).isFalse();
            conn.close();
        }
    }

    @Test
    public void testSelectIndexedIndexed() {
        // test 1
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    InjectorsExtractors.indexedInjector(H2Mappers.collection())
                            .param(INT_STRING_MAPPER)
                            .build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection())
                            .param(INT_STRING_MAPPER)
                            .param(H2Mappers.DOUBLE)
                            .param(H2Mappers.NUMERIC)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.DATE)
                            .param(H2Mappers.TIME)
                            .param(H2Mappers.TIMESTAMP)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, "1");
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    "1", 10.1d, new BigDecimal("100.0010000000"), "p1", "p2",
                    LocalDate.of(2000, 1, 31),
                    LocalTime.of(12, 30, 55, 555666777),
                    LocalDateTime.of(2001, 2, 13, 10, 20, 30, 0)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 2
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    """
                    SELECT col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=? AND col_d=? AND col_nu=? AND col_str1=? AND col_str2=?\
                    """,
                    InjectorsExtractors.indexedInjector(H2Mappers.collection())
                            .param(INT_STRING_MAPPER)
                            .param(H2Mappers.DOUBLE)
                            .param(H2Mappers.NUMERIC)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.VARCHAR)
                            .build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection())
                            .param(H2Mappers.DATE)
                            .param(H2Mappers.TIME)
                            .param(H2Mappers.TIMESTAMP)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, "2", -10.1d, new BigDecimal("-100.0010000000"), "n1", "n2");
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    LocalDate.of(2025, 10, 24),
                    LocalTime.of(13, 20, 0, 111),
                    LocalDateTime.of(2025, 10, 24, 13, 20, 30, 123123123)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
    }

    @Test
    public void testSelectNamedNamed() {
        // test 1, extraction by index
        {
            Connection conn = database.connect(10);
            Query<HasId, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    InjectorsExtractors.namedInjector(H2Mappers.collection(), HasId.class)
                            .param("id", H2Mappers.INT)
                            .build(),
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", H2Mappers.INT)
                            .param("doublePrecision", H2Mappers.DOUBLE)
                            .param("num", H2Mappers.NUMERIC)
                            .param("str1", H2Mappers.VARCHAR)
                            .param("str2", H2Mappers.VARCHAR)
                            .param("date", H2Mappers.DATE_STR)
                            .param("time", H2Mappers.TIME_STR)
                            .param("timestamp", H2Mappers.TIMESTAMP_STR)
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
            assertThat(row.time).isEqualTo("12:30:55.555666777");
            assertThat(row.timestamp).isEqualTo("2001-02-13 10:20:30");
            conn.close();
        }
        // test 2, extraction by label
        {
            Connection conn = database.connect(10);
            Query<HasId, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    InjectorsExtractors.namedInjector(H2Mappers.collection(), HasId.class)
                            .param("id", H2Mappers.INT)
                            .build(),
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", "id", H2Mappers.INT)
                            .param("doublePrecision", "doublePrecision", H2Mappers.DOUBLE)
                            .param("num", "num", H2Mappers.NUMERIC)
                            .param("str1", "col_str1", H2Mappers.VARCHAR)
                            .param("str2", "col_str2", H2Mappers.VARCHAR)
                            .param("timestamp", "col_timestamp", H2Mappers.TIMESTAMP_STR)
                            .param("date", "col_date", H2Mappers.DATE_STR)
                            .param("time", "col_time", H2Mappers.TIME_STR)
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
            assertThat(row.time).isEqualTo("13:20:00.000000111");
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    @Test
    public void testSelectIndexedNamed() {
        // test 1, extraction by index
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    InjectorsExtractors.indexedInjector(H2Mappers.collection())
                            .param(INT_STRING_MAPPER)
                            .build(),
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", H2Mappers.INT)
                            .param("doublePrecision", H2Mappers.DOUBLE)
                            .param("num", H2Mappers.NUMERIC)
                            .param("str1", H2Mappers.VARCHAR)
                            .param("str2", H2Mappers.VARCHAR)
                            .param("date", H2Mappers.DATE_STR)
                            .param("time", H2Mappers.TIME_STR)
                            .param("timestamp", H2Mappers.TIMESTAMP_STR)
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
            assertThat(row.time).isEqualTo("12:30:55.555666777");
            assertThat(row.timestamp).isEqualTo("2001-02-13 10:20:30");
            conn.close();
        }
        // test 2, extraction by label
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=? AND col_str1=? AND col_str2=?\
                    """,
                    InjectorsExtractors.indexedInjector(H2Mappers.collection())
                            .param(H2Mappers.INT)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.VARCHAR)
                            .build(),
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", "id", H2Mappers.INT)
                            .param("doublePrecision", "doublePrecision", H2Mappers.DOUBLE)
                            .param("num", "num", H2Mappers.NUMERIC)
                            .param("timestamp", "col_timestamp", H2Mappers.TIMESTAMP_STR)
                            .param("date", "col_date", H2Mappers.DATE_STR)
                            .param("time", "col_time", H2Mappers.TIME_STR)
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
            assertThat(row.time).isEqualTo("13:20:00.000000111");
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    @Test
    public void testSelectNamedIndexed() {
        // test 1
        {
            Connection conn = database.connect(10);
            Query<HasId, Seq<Object>> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=?\
                    """,
                    InjectorsExtractors.namedInjector(H2Mappers.collection(), HasId.class)
                            .param("id", H2Mappers.INT)
                            .build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection())
                            .param(INT_STRING_MAPPER)
                            .param(H2Mappers.DOUBLE)
                            .param(H2Mappers.NUMERIC)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.DATE)
                            .param(H2Mappers.TIME)
                            .param(H2Mappers.TIMESTAMP)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, new HasId(1));
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    "1", 10.1d, new BigDecimal("100.0010000000"), "p1", "p2",
                    LocalDate.of(2000, 1, 31),
                    LocalTime.of(12, 30, 55, 555666777),
                    LocalDateTime.of(2001, 2, 13, 10, 20, 30, 0)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 2
        {
            Connection conn = database.connect(10);
            Query<NamedRow1, Seq<Object>> query = QFACTORY.selectQuery(
                    """
                    SELECT col_date, col_time, col_timestamp, col_d, col_nu\
                     FROM table_one\
                     WHERE id=? AND col_str1=? AND col_str2=?\
                    """,
                    InjectorsExtractors.namedInjector(H2Mappers.collection(), NamedRow1.class)
                            .param("id", H2Mappers.INT)
                            .param("str1", H2Mappers.VARCHAR)
                            .param("str2", H2Mappers.VARCHAR)
                            .build(),
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection())
                            .param(H2Mappers.DATE)
                            .param(H2Mappers.TIME)
                            .param(H2Mappers.TIMESTAMP)
                            .param(H2Mappers.DOUBLE)
                            .param(H2Mappers.NUMERIC)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, new NamedRow1(2, "n1", "n2"));
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    LocalDate.of(2025, 10, 24),
                    LocalTime.of(13, 20, 0, 111),
                    LocalDateTime.of(2025, 10, 24, 13, 20, 30, 123123123),
                    -10.1d,
                    new BigDecimal("-100.0010000000")
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
    }

    @Test
    public void testSelectWithoutParameters() {
        // test 1
        {
            Connection conn = database.connect(10);
            Query<Void, Seq<Object>> query = QFACTORY.selectQueryWithoutParameters(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=1\
                    """,
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection())
                            .param(INT_STRING_MAPPER)
                            .param(H2Mappers.DOUBLE)
                            .param(H2Mappers.NUMERIC)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.VARCHAR)
                            .param(H2Mappers.DATE)
                            .param(H2Mappers.TIME)
                            .param(H2Mappers.TIMESTAMP)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeSelect(query);
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    "1", 10.1d, new BigDecimal("100.0010000000"), "p1", "p2",
                    LocalDate.of(2000, 1, 31),
                    LocalTime.of(12, 30, 55, 555666777),
                    LocalDateTime.of(2001, 2, 13, 10, 20, 30, 0)
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 2, extraction by index
        {
            Connection conn = database.connect(10);
            Query<Void, NamedRow2> query = QFACTORY.selectQueryWithoutParameters(
                    """
                    SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=1\
                    """,
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", H2Mappers.INT)
                            .param("doublePrecision", H2Mappers.DOUBLE)
                            .param("num", H2Mappers.NUMERIC)
                            .param("str1", H2Mappers.VARCHAR)
                            .param("str2", H2Mappers.VARCHAR)
                            .param("date", H2Mappers.DATE_STR)
                            .param("time", H2Mappers.TIME_STR)
                            .param("timestamp", H2Mappers.TIMESTAMP_STR)
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
            assertThat(row.time).isEqualTo("12:30:55.555666777");
            assertThat(row.timestamp).isEqualTo("2001-02-13 10:20:30");
            conn.close();
        }
        // test 3, extraction by label
        {
            Connection conn = database.connect(10);
            Query<Void, NamedRow2> query = QFACTORY.selectQueryWithoutParameters(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id=2\
                    """,
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", "id", H2Mappers.INT)
                            .param("doublePrecision", "doublePrecision", H2Mappers.DOUBLE)
                            .param("num", "num", H2Mappers.NUMERIC)
                            .param("str1", "col_str1", H2Mappers.VARCHAR)
                            .param("str2", "col_str2", H2Mappers.VARCHAR)
                            .param("timestamp", "col_timestamp", H2Mappers.TIMESTAMP_STR)
                            .param("date", "col_date", H2Mappers.DATE_STR)
                            .param("time", "col_time", H2Mappers.TIME_STR)
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
            assertThat(row.time).isEqualTo("13:20:00.000000111");
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    @Test
    public void testExecuteAnyQuery() {
        // test indexed indexed query
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_str1, col_str2\
                     FROM table_one\
                     WHERE id>? AND col_str1<>?\
                    """,
                    InjectorsExtractors.indexedInjector(H2Mappers.collection())
                            .param(H2Mappers.INT)
                            .param(H2Mappers.VARCHAR)
                            .build(),
                    // mixed by index and by label
                    InjectorsExtractors.indexedExtractor(H2Mappers.collection())
                            .param("id", INT_STRING_MAPPER)
                            .param(H2Mappers.VARCHAR)
                            .param("col_str2", H2Mappers.VARCHAR)
                            .param("col_str1", H2Mappers.VARCHAR) // once more same column extracted by label (bad bad bad)
                            .build()
            );
            Result<Seq<Object>> result = conn.executeAnyQuery(query, List.of(0, "hehehe"));
            Seq<Seq<Object>> rows = result.extractAll();
            assertThat(rows).containsExactly(
                    List.of("1", "p1", "p2", "p1"),
                    List.of("2", "n1", "n2", "n1")
            );
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test named named query
        {
            Connection conn = database.connect(10);
            Query<NamedRow1, NamedRow2> query = QFACTORY.selectQuery(
                    """
                    SELECT id, col_d AS doublePrecision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                     FROM table_one\
                     WHERE id<? AND col_str2<>?\
                    """,
                    InjectorsExtractors.namedInjector(H2Mappers.collection(), NamedRow1.class)
                            .param("id", H2Mappers.INT)
                            .param("str2", H2Mappers.VARCHAR)
                            .build(),
                    // mixed by index and by label
                    InjectorsExtractors.namedExtractor(H2Mappers.collection(), NamedRow2.class)
                            .param("id", H2Mappers.INT)
                            .param("doublePrecision", H2Mappers.DOUBLE)
                            .param("num", H2Mappers.NUMERIC)
                            .param("str1", "col_str1", H2Mappers.VARCHAR)
                            .param("str2", "col_str2", H2Mappers.VARCHAR)
                            .param("timestamp", "col_timestamp", H2Mappers.TIMESTAMP_STR)
                            .param("date", "col_date", H2Mappers.DATE_STR)
                            .param("time", "col_time", H2Mappers.TIME_STR)
                            .build()
            );
            Result<NamedRow2> result = conn.executeSelect(query, new NamedRow1(3, "n2", "p2"));
            NamedRow2 row = result.exactlySingleRow();
            assertThat(row).isNotNull();
            assertThat(row.id).isEqualTo(2);
            assertThat(row.doublePrecision).isEqualTo(-10.1d);
            assertThat(row.num).isEqualTo(new BigDecimal("-100.0010000000"));
            assertThat(row.str1).isEqualTo("n1");
            assertThat(row.getStr2()).isEqualTo("n2");
            assertThat(row.date).isEqualTo("2025-10-24");
            assertThat(row.time).isEqualTo("13:20:00.000000111");
            assertThat(row.timestamp).isEqualTo("2025-10-24 13:20:30.123123123");
        }
    }

    private static final TypeMapper<Integer, String> INT_STRING_MAPPER = new SimpleTypeMapper<>(
            H2Types.INTEGER,
            String.class,
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
