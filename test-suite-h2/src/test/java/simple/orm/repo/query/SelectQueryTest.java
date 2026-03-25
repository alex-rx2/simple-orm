package simple.orm.repo.query;

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
import simple.orm.jdbc.Result;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.QueryParser;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;
import simple.orm.repo.RepositoryBuilder;
import simple.orm.repo.SQLLoader;
import simple.orm.repo.anno.ExtractParam;
import simple.orm.repo.anno.InjectParam;
import simple.orm.repo.anno.ParamInjector;
import simple.orm.repo.anno.ParameterStrategy;
import simple.orm.repo.anno.QuerySource;
import simple.orm.repo.anno.RepoType;
import simple.orm.repo.anno.ResultExtractor;
import simple.orm.repo.anno.SimpleOrmRepo;
import simple.orm.repo.anno.SimpleQuery;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of SELECT queries.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SelectQueryTest extends BaseH2Test {

    private DatabaseAccessPoint database;
    private RepositoryBuilder repositoryBuilder;
    private SelectRepository repository;

    @BeforeAll
    void setUp() throws SQLException {
        final ReflectionsFinder reflectionsFinder = ReflectionsFinder.defaultFinder();
        MappersCollection mappersCollection =
                H2Mappers.collection()
                        .addMapper("int", INT_STRING_MAPPER, "str")
                        .addMapper("intstr", INT_STRING_MAPPER);
        repositoryBuilder = RepositoryBuilder.of(
                SQLLoader.defaultLoader(),
                QueryParser.defaultParser(),
                QueryBuilder.of(
                        H2Types.collection(),
                        () -> MappersFinder.defaultFinder(mappersCollection),
                        () -> reflectionsFinder
                )
        );
        repository = repositoryBuilder.buildRepository(SelectRepository.class);
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
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = repository.emptySelect01();
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.extractAll()).isEmpty();
            conn.close();
        }
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = repository.emptySelect02();
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.extractAll()).isEmpty();
            conn.close();
        }
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = repository.emptySelect03();
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.extractAll()).isEmpty();
            conn.close();
        }
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = repository.emptySelect04();
            Result<Seq<Object>> result = conn.executeSelect(query, 222);
            assertThat(result.extractAll()).isEmpty();
            conn.close();
        }
    }

    @Test
    public void testSelectIndexedIndexed() {
        // test 1
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, Seq<Object>> query = repository.indexedIndexed01();
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
            Query<Seq<Object>, Seq<Object>> query = repository.indexedIndexed02();
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
            Query<HasId, NamedRow2> query = repository.namedNamed01();
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
            Query<HasId, NamedRow2> query = repository.namedNamed02();
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
            Query<Seq<Object>, NamedRow2> query = repository.indexedNamed01();
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
        // test 2, extraction by label (some labels not matching property names)
        {
            Connection conn = database.connect(10);
            Query<Seq<Object>, NamedRow2> query = repository.indexedNamed02();
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
            Query<HasId, Seq<Object>> query = repository.namedIndexed01();
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
            Query<NamedRow1, Seq<Object>> query = repository.namedIndexed02();
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
            Query<Void, Seq<Object>> query = repository.withoutParams01();
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
            Query<Void, NamedRow2> query = repository.withoutParams02();
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
        // test 3, extraction by label (some labels not matching property names)
        {
            Connection conn = database.connect(10);
            Query<Void, NamedRow2> query = repository.withoutParams03();
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


    @SimpleOrmRepo(type = RepoType.QUERY)
    public interface SelectRepository {

        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = """
                             SELECT id FROM table_one WHERE id=?
                             --//::int
                             --??::int
                             """)
        Query<Seq<Object>, Seq<Object>> emptySelect01();

        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = """
                             /*//::int*/
                             SELECT id FROM table_one WHERE id=?
                             /*??::int*/
                             """)
        Query<Seq<Object>, Seq<Object>> emptySelect02();

        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = """
                             SELECT id --/::int
                             FROM table_one
                             WHERE id=? --?::int
                             """)
        Query<Seq<Object>, Seq<Object>> emptySelect03();

        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY)
        @QuerySource(query = """
                             SELECT id FROM table_one WHERE id=?
                             /*
                             //::int
                             ??::int
                             */
                             """)
        Query<Seq<Object>, Seq<Object>> emptySelect04();

        @SimpleQuery(type = QueryType.SELECT)
        @QuerySource(query = """
                             SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=?\
                             """)
        @InjectParam(mapper = "intstr")
        @ExtractParam(mapper = "intstr")
        @ExtractParam(mapper = "double")
        @ExtractParam(mapper = "numeric")
        @ExtractParam(mapper = "varchar")
        @ExtractParam(mapper = "varchar")
        @ExtractParam(mapper = "date")
        @ExtractParam(mapper = "time")
        @ExtractParam(mapper = "timestamp")
        Query<Seq<Object>, Seq<Object>> indexedIndexed01();

        @SimpleQuery(type = QueryType.SELECT)
        @QuerySource(query = """
                             SELECT col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=? AND col_d=? AND col_nu=? AND col_str1=? AND col_str2=?\
                             """)
        @InjectParam(index = 1, mapper = "intstr")
        @InjectParam(index = 4, mapper = "varchar")
        @InjectParam(index = 5, mapper = "varchar")
        @InjectParam(index = 2, mapper = "double")
        @InjectParam(index = 3, mapper = "numeric")
        @ExtractParam(index = 3, mapper = "timestamp")
        @ExtractParam(index = 2, mapper = "time")
        @ExtractParam(index = 1, mapper = "date")
        Query<Seq<Object>, Seq<Object>> indexedIndexed02();

        @SimpleQuery(type = QueryType.SELECT,
                parameters = ParameterStrategy.PARSE_QUERY,
                sourceClass = HasId.class,
                targetClass = NamedRow2.class)
        @QuerySource(query = """
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
                              WHERE id=?
                             --??:id:int
                             """)
        Query<HasId, NamedRow2> namedNamed01();

        @SimpleQuery(type = QueryType.SELECT,
                parameters = ParameterStrategy.PARSE_QUERY,
                sourceClass = HasId.class,
                targetClass = NamedRow2.class)
        @QuerySource(query = """
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
                             """)
        Query<HasId, NamedRow2> namedNamed02();

        @SimpleQuery(type = QueryType.SELECT, targetClass = NamedRow2.class)
        @QuerySource(query = """
                             SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=?\
                             """)
        @ParamInjector({
                @InjectParam(mapper = "intstr")
        })
        @ResultExtractor({
                @ExtractParam(prop = "id", mapper = "int"),
                @ExtractParam(prop = "doublePrecision", mapper = "double"),
                @ExtractParam(prop = "num", mapper = "numeric"),
                @ExtractParam(prop = "str1", mapper = "varchar"),
                @ExtractParam(prop = "str2", mapper = "varchar"),
                @ExtractParam(prop = "date", mapper = "date", tag = "h2str"),
                @ExtractParam(prop = "time", mapper = "time", tag = "h2str"),
                @ExtractParam(prop = "timestamp", mapper = "timestamp", tag = "h2str")
        })
        Query<Seq<Object>, NamedRow2> indexedNamed01();

        @SimpleQuery(type = QueryType.SELECT, targetClass = NamedRow2.class)
        @QuerySource(query = """
                             SELECT id, col_d AS double_precision, col_nu AS num, col_str1, col_str2, col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=? AND col_str1=? AND col_str2=?\
                             """)
        @ParamInjector({
                @InjectParam(mapper = "int"),
                @InjectParam(mapper = "varchar"),
                @InjectParam(mapper = "varchar")
        })
        @ResultExtractor({
                @ExtractParam(label = "id", prop = "id", mapper = "int"),
                @ExtractParam(label = "double_precision", prop = "doublePrecision", mapper = "double"),
                @ExtractParam(label = "num", prop = "num", mapper = "numeric"),
                @ExtractParam(label = "col_date", prop = "date", mapper = "date", tag = "h2str"),
                @ExtractParam(label = "col_time", prop = "time", mapper = "time", tag = "h2str"),
                @ExtractParam(label = "col_timestamp", prop = "timestamp", mapper = "timestamp", tag = "h2str")
        })
        Query<Seq<Object>, NamedRow2> indexedNamed02();

        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY, sourceClass = HasId.class)
        @QuerySource(query = """
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
                             """)
        Query<HasId, Seq<Object>> namedIndexed01();

        @SimpleQuery(type = QueryType.SELECT, parameters = ParameterStrategy.PARSE_QUERY, sourceClass = NamedRow1.class)
        @QuerySource(query = """
                             SELECT
                               col_date,--/::date
                               col_time,--/::time
                               col_timestamp,--/::timestamp
                               col_d,--/::double
                               col_nu--/::numeric
                             FROM table_one
                             WHERE id=?--?:id:
                               AND col_str1=?--?:str1:
                               AND col_str2=?--?:str2:
                             """)
        Query<NamedRow1, Seq<Object>> namedIndexed02();

        @SimpleQuery(type = QueryType.SELECT)
        @QuerySource(query = """
                             SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=1\
                             """)
        @ExtractParam(mapper = "intstr")
        @ExtractParam(mapper = "double")
        @ExtractParam(mapper = "numeric")
        @ExtractParam(mapper = "varchar")
        @ExtractParam(mapper = "varchar")
        @ExtractParam(mapper = "date")
        @ExtractParam(mapper = "time")
        @ExtractParam(mapper = "timestamp")
        Query<Void, Seq<Object>> withoutParams01();

        @SimpleQuery(type = QueryType.SELECT, targetClass = NamedRow2.class)
        @QuerySource(query = """
                             SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=1\
                             """)
        @ExtractParam(prop = "id", mapper = "int")
        @ExtractParam(prop = "doublePrecision", mapper = "double")
        @ExtractParam(prop = "num", mapper = "numeric")
        @ExtractParam(prop = "str1", mapper = "varchar")
        @ExtractParam(prop = "str2", mapper = "varchar")
        @ExtractParam(prop = "date", mapper = "datestr")
        @ExtractParam(prop = "time", mapper = "timestr")
        @ExtractParam(prop = "timestamp", mapper = "timestampstr")
        Query<Void, NamedRow2> withoutParams02();

        @SimpleQuery(type = QueryType.SELECT, targetClass = NamedRow2.class)
        @QuerySource(query = """
                             SELECT id, col_d, col_nu, col_str1, col_str2, col_date, col_time, col_timestamp\
                              FROM table_one\
                              WHERE id=2\
                             """)
        @ExtractParam(label = "id", prop = "id", mapper = "int")
        @ExtractParam(label = "col_d", prop = "doublePrecision", mapper = "double")
        @ExtractParam(label = "col_nu", prop = "num", mapper = "numeric")
        @ExtractParam(label = "col_str1", prop = "str1", mapper = "varchar")
        @ExtractParam(label = "col_str2", prop = "str2", mapper = "varchar")
        @ExtractParam(label = "col_date", prop = "date", mapper = "date", tag = "h2str")
        @ExtractParam(label = "col_time", prop = "time", mapper = "time", tag = "h2str")
        @ExtractParam(label = "col_timestamp", prop = "timestamp", mapper = "timestamp", tag = "h2str")
        Query<Void, NamedRow2> withoutParams03();

    }

    public static final TypeMapper<Integer, String> INT_STRING_MAPPER = new SimpleTypeMapper<>(
            H2Types.INTEGER,
            String.class,
            Integer::valueOf,
            Object::toString
    );

    public static class NamedRow2 extends NamedRow1 {
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

    public static class NamedRow1 extends HasId {
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

    public static class HasId {
        protected Integer id;

        public HasId() {
        }

        public HasId(Integer id) {
            this.id = id;
        }
    }

}
