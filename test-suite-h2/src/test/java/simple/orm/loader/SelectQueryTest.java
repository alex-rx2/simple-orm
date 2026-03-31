package simple.orm.loader;

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
import simple.orm.jdbc.JdbcException;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
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

    private static final TypeMapper<Integer, String> INT_STRING_MAPPER = new SimpleTypeMapper<>(
            H2Types.INTEGER,
            String.class,
            Integer::valueOf,
            Object::toString
    );

    private static final QueryLoader QLOADER = QueryLoader.of(
            QueryParser.defaultParser(),
            H2Types.collection(),
            H2Mappers.collection().addMapper("int", INT_STRING_MAPPER, "str")
    );

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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT id FROM table_one WHERE id=?
                            --//::int
                            --??::int
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            /*//::int*/
                            SELECT id FROM table_one WHERE id=?
                            /*??::int*/
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT id --/::int
                            FROM table_one
                            WHERE id=? --?::int
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT id FROM table_one WHERE id=?
                            /*
                            //::int
                            ??::int
                            */
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT id FROM table_one WHERE id=?
                            --//::int
                            --??::int
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT id FROM table_one WHERE id=?
                            --//::int
                            --??::int
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT id FROM table_one WHERE id=?
                            --//::int
                            --??::int
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//::int(str)
                            --//::double
                            --//::numeric
                            --//::varchar
                            --//::varchar
                            --//::date
                            --//::time
                            --//::timestamp
                            SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp
                             FROM table_one
                             WHERE id=?
                            --??::int(str)
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT col_date,     --/::date
                                   col_time,     --/::time
                                   col_timestamp --/::timestamp
                            FROM table_one
                            WHERE id=?           --?::int(str)
                              AND col_d=?        --?::double
                              AND col_nu=?       --?::numeric
                              AND col_str1=?     --?::varchar
                              AND col_str2=?     --?::varchar
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
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
            Query<HasId, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//:id:int
                            --//:doublePrecision:double
                            --//:num:numeric
                            --//:str1:varchar
                            --//:str2:varchar
                            --//:date:date(h2str)
                            --//:time:time(h2str)
                            --//:timestamp:timestamp(h2str)
                            SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                             FROM table_one\
                             WHERE id=?
                            --??:id:int
                            """
                    ),
                    InjectionStrategy.named(HasId.class),
                    ExtractionStrategy.named(NamedRow2.class)
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
        // test 2, extraction by label (some labels not matching property names)
        {
            Connection conn = database.connect(10);
            Query<HasId, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT
                              id,                       --/:id:
                              col_d AS doublePrecision, --/:doublePrecision:
                              col_nu AS num,            --/:num:
                              col_str1,                 --/:str1:
                              col_str2,                 --/:str2:
                              col_date,                 --/:date:
                              col_time,                 --/:time:
                              col_timestamp             --/:timestamp:
                             FROM table_one
                             WHERE id=?                 --?:id:
                            """
                    ),
                    InjectionStrategy.named(HasId.class),
                    ExtractionStrategy.named(NamedRow2.class)
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
            Query<Seq<Object>, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//:id:
                            --//:doublePrecision:
                            --//:num:
                            --//:str1:
                            --//:str2:
                            --//:date:
                            --//:time:
                            --//:timestamp:
                            SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp
                             FROM table_one
                             WHERE id=?
                            --??:id:
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.named(NamedRow2.class)
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
        // test 2, extraction by label (some columns are missed)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT
                              id,           --/:id:::java.lang.Integer
                              col_d,        --/:doublePrecision:::java.lang.Double
                              col_nu,       --/:num:::java.math.BigDecimal
                              col_str1,
                              col_str2,
                              col_date,     --/:date:::java.lang.String
                              col_time,     --/:time:::java.lang.String
                              col_timestamp --/:timestamp:::java.lang.String
                            FROM table_one
                            WHERE id=?      --?:id:::java.lang.Integer
                              AND col_str1=?--?:str1:::java.lang.String
                              AND col_str2=?--?:str2:::java.lang.String
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.named(NamedRow2.class)
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
            Query<HasId, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            /*
                            //::int(str)
                            //::double
                            //::numeric
                            //::varchar
                            //::varchar
                            //::date
                            //::time
                            //::timestamp
                            */
                            
                            SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp
                            FROM table_one
                            WHERE id=?
                            
                            --??:id:int
                            """
                    ),
                    InjectionStrategy.named(HasId.class),
                    ExtractionStrategy.indexed()
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
        // test 2 - extraction by index
        {
            Connection conn = database.connect(10);
            Query<NamedRow1, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//::date
                            --//::varchar
                            --//::varchar
                            --//::time
                            --//::timestamp
                            --//::double
                            --//::numeric
                            SELECT
                              col_date,
                              col_str1,
                              col_str2,
                              col_time,
                              col_timestamp,
                              col_d,
                              col_nu
                            FROM table_one
                            WHERE id=?--?:id:
                              AND col_str1=?--?:str1:
                              AND col_str2=?--?:str2:
                            """
                    ),
                    InjectionStrategy.named(NamedRow1.class),
                    ExtractionStrategy.indexed()
            );
            Result<Seq<Object>> result = conn.executeSelect(query, new NamedRow1(2, "n1", "n2"));
            assertThat(result.hasNextRow()).isTrue();
            Seq<Object> row = result.nextRow();
            assertThat(row).containsExactly(
                    LocalDate.of(2025, 10, 24),
                    "n1",
                    "n2",
                    LocalTime.of(13, 20, 0, 111),
                    LocalDateTime.of(2025, 10, 24, 13, 20, 30, 123123123),
                    -10.1d,
                    new BigDecimal("-100.0010000000")
            );
            assertThat(result.hasNextRow()).isFalse();
            conn.close();
        }
        // test 3 - extraction by label (immediate params)
        {
            Connection conn = database.connect(10);
            Query<NamedRow1, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    // col_str1 and col_str2 are ignored, but because extraction is by label (all labels are guessed)
                    // there is no errors (if extraction was by index we would try to extract --/::time with index 2 corresponding to col_str1 instead)
                    QuerySource.of(
                            """
                            SELECT
                              col_date,--/::date
                              col_str1,
                              col_str2,
                              col_time,--/::time
                              col_timestamp,--/::timestamp
                              col_d,--/::double
                              col_nu--/::numeric
                            FROM table_one
                            WHERE id=?--?:id:
                              AND col_str1=?--?:str1:
                              AND col_str2=?--?:str2:
                            """
                    ),
                    InjectionStrategy.named(NamedRow1.class),
                    ExtractionStrategy.indexed()
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
        // test 4 - extraction by label (anywhere params)
        {
            Connection conn = database.connect(10);
            Query<NamedRow1, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    // col_str1 and col_str2 are ignored, but because extraction is by label (all labels are guessed)
                    // there is no errors (if extraction was by index we would try to extract --/::time with index 2 corresponding to col_str1 instead)
                    QuerySource.of(
                            """
                            /*
                            //date::date
                            //time::time
                            //timestamp::timestamp
                            //ddd::double
                            //nnn::numeric
                            */
                            SELECT
                              col_date AS date,
                              col_str1 AS str1,
                              col_str2 AS str2,
                              col_time AS time,
                              col_timestamp AS timestamp,
                              col_d AS ddd,
                              col_nu AS nnn
                            FROM table_one
                            WHERE id=?--?:id:
                              AND col_str1=?--?:str1:
                              AND col_str2=?--?:str2:
                            """
                    ),
                    InjectionStrategy.named(NamedRow1.class),
                    ExtractionStrategy.indexed()
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
            Query<Void, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//::int(str)
                            --//::double
                            --//::numeric
                            --//::varchar
                            --//::varchar
                            --//::date
                            --//::time
                            --//::timestamp
                            SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp
                            FROM table_one
                            WHERE id=1
                            """
                    ),
                    InjectionStrategy.none(),
                    ExtractionStrategy.indexed()
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
            Query<Void, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//:id:int
                            --//:doublePrecision:double
                            --//:num:numeric
                            --//:str1:varchar
                            --//:str2:varchar
                            --//:date:date(h2str)
                            --//:time:time(h2str)
                            --//:timestamp:timestamp(h2str)
                            SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp
                            FROM table_one
                            WHERE id=1
                            """
                    ),
                    InjectionStrategy.none(),
                    ExtractionStrategy.named(NamedRow2.class)
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
            Query<Void, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    // we get SQL type from metadata and javaClass for property via reflections
                    // property name is taken from guessed label
                    // this is enough to find a single mapper in mappers collection we have
                    QuerySource.of(
                            """
                            SELECT
                              id,                               --/::
                              col_d         AS doublePrecision, --/::
                              col_nu        AS num,             --/::
                              col_str1      AS str1,            --/::
                              col_str2      AS str2,            --/::
                              col_date      AS date,            --/::
                              col_time      AS time,            --/::
                              col_timestamp AS timestamp        --/::
                            FROM table_one
                            WHERE id=2
                            """
                    ),
                    InjectionStrategy.none(),
                    ExtractionStrategy.named(NamedRow2.class)
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
            Query<Seq<Object>, Seq<Object>> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            --//::int(str)
                            --//::varchar
                            --//::varchar
                            SELECT id, col_str1, col_str2
                             FROM table_one
                             WHERE id>? AND col_str1<>?
                            --??::int
                            --??::varchar
                            """
                    ),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.indexed()
            );
            Result<Seq<Object>> result = conn.executeAnyQuery(query, List.of(0, "hehehe"));
            Seq<Seq<Object>> rows = result.extractAll();
            assertThat(rows).containsExactly(
                    List.of("1", "p1", "p2"),
                    List.of("2", "n1", "n2")
            );
            assertThat(result.isClosed()).isTrue();
            conn.close();
        }
        // test named named query
        {
            Connection conn = database.connect(10);
            Query<NamedRow1, NamedRow2> query = QLOADER.loadQuery(
                    QueryType.SELECT,
                    QuerySource.of(
                            """
                            SELECT
                               id,           --/::int
                               col_d,        --/:doublePrecision:double
                               col_nu,       --/:num:numeric
                               col_str1,     --/:str1:varchar
                               col_str2,     --/:str2:varchar
                               col_date,     --/:date:date(h2str)
                               col_time,     --/:time:time(h2str)
                               col_timestamp --/:timestamp:timestamp(h2str)
                            FROM table_one
                            WHERE id<?        --?:id:int
                              AND col_str2<>? --?:str2:varchar
                            """
                    ),
                    InjectionStrategy.named(NamedRow1.class),
                    ExtractionStrategy.named(NamedRow2.class)
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
