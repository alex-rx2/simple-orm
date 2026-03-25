package simple.orm.repo.query;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.BaseH2Test;
import simple.orm.h2.H2Mappers;
import simple.orm.h2.H2Types;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.QueryParser;
import simple.orm.loader.builder.QueryBuilder;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;
import simple.orm.repo.RepositoryBuilder;
import simple.orm.repo.SQLLoader;
import simple.orm.repo.anno.InjectParam;
import simple.orm.repo.anno.ParameterStrategy;
import simple.orm.repo.anno.QuerySource;
import simple.orm.repo.anno.RepoType;
import simple.orm.repo.anno.SimpleOrmRepo;
import simple.orm.repo.anno.SimpleQuery;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of DML queries.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DMLQueryTest extends BaseH2Test {

    private DatabaseAccessPoint database;
    private RepositoryBuilder repositoryBuilder;
    private DMLRepository repository;

    @BeforeAll
    void setUp() throws SQLException {
        final ReflectionsFinder reflectionsFinder = ReflectionsFinder.defaultFinder();
        repositoryBuilder = RepositoryBuilder.of(
                TEST_SQL_LOADER,
                QueryParser.defaultParser(),
                QueryBuilder.of(
                        H2Types.collection(),
                        () -> MappersFinder.defaultFinder(H2Mappers.collection()),
                        () -> reflectionsFinder
                )
        );
        repository = repositoryBuilder.buildRepository(DMLRepository.class);
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
        try (Connection conn = directConnect()) {
            conn.createStatement().executeUpdate(
                    """
                    CREATE TABLE table_one\
                     (id INT PRIMARY KEY,\
                      col_ti TINYINT NULL,\
                      col_s1 VARCHAR(256) NULL,\
                      col_si SMALLINT NULL,\
                      col_s2 VARCHAR(256) NULL,\
                      col_bi BIGINT NULL,\
                      col_s3 VARCHAR(256) NULL,\
                      col_r REAL NULL,\
                      col_s4 VARCHAR(256) NULL,\
                      col_d DOUBLE PRECISION NULL,\
                      col_s5 VARCHAR(256) NULL,\
                      col_nu NUMERIC(30,10) NULL,\
                      col_s6 VARCHAR(256) NULL\
                    )\
                    """);
        }
    }

    private void seedSomeData() throws SQLException {
        try (Connection conn = directConnect()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("TRUNCATE TABLE table_one");
            stmt.executeUpdate("""
                               INSERT INTO table_one\
                                VALUES (1,\
                                 10,    'p1',\
                                 10,    'p2',\
                                 10,    'p3',\
                                 10.10, 'p4',\
                                 10.10, 'p5',\
                                 10.10, 'p6'\
                               )\
                               """);
            stmt.executeUpdate("""
                               INSERT INTO table_one\
                                VALUES (2,\
                                 -10,    'n1',\
                                 -10,    'n2',\
                                 -10,    'n3',\
                                 -10.10, 'n4',\
                                 -10.10, 'n5',\
                                 -10.10, 'n6'\
                               )\
                               """);
        }
    }

    @Test
    public void testInsertIndexed() throws SQLException {
        // test
        {
            simple.orm.jdbc.Connection conn = database.connect();
            Query<Seq<Object>, Integer> query = repository.insertIndexed();
            int result = conn.executeDMLQuery(query,
                    3,
                    33, "test1-1",
                    -33, "test1-2",
                    8888888888888888888L, "test1-3",
                    33.33f, "test1-4",
                    -33.33d, "test1-5",
                    new BigDecimal("12345678901234567890.0987654321"), "test1-6"
            );
            assertThat(result).isEqualTo(1);
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (Connection conn = directConnect()) {
                ResultSet rs = conn.createStatement().executeQuery(
                        """
                        SELECT concat_ws(',',\
                          id,\
                          to_char(col_ti),col_s1,\
                          to_char(col_si),col_s2,\
                          to_char(col_bi),col_s3,\
                          to_char(col_r),col_s4,\
                          to_char(col_d),col_s5,\
                          to_char(col_nu),col_s6\
                        )\
                        FROM table_one ORDER BY id\
                        """
                );
                while (rs.next()) {
                    columns = columns.append(rs.getString(1));
                }
            }
            assertThat(columns).containsExactly(
                    // 10.10 -> 10.1 and numeric fills 0 to all fractional digits
                    "1,10,p1,10,p2,10,p3,10.1,p4,10.1,p5,10.1000000000,p6",
                    "2,-10,n1,-10,n2,-10,n3,-10.1,n4,-10.1,n5,-10.1000000000,n6",
                    "3,33,test1-1,-33,test1-2,8888888888888888888,test1-3,33.33,test1-4,-33.33,test1-5,12345678901234567890.0987654321,test1-6"
            );
        }
    }

    @Test
    public void testInsertNamed() throws SQLException {
        // test
        {
            simple.orm.jdbc.Connection conn = database.connect();
            Query<NamedRow2, Integer> query = repository.insertNamed();
            int result = conn.executeDMLQuery(query,
                    new NamedRow2(5,
                            55, 55, 555555555555555555L, -5.5f, 6.6, new BigDecimal("777.777"),
                            "t2-1", "t2-2", "t2-3", "t2-4", "t2-5", "t2-6"
                    )
            );
            assertThat(result).isEqualTo(1);
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (Connection conn = directConnect()) {
                ResultSet rs = conn.createStatement().executeQuery(
                        """
                        SELECT concat_ws(',',\
                          id,\
                          to_char(col_ti),col_s1,\
                          to_char(col_si),col_s2,\
                          to_char(col_bi),col_s3,\
                          to_char(col_r),col_s4,\
                          to_char(col_d),col_s5,\
                          to_char(col_nu),col_s6\
                         )\
                         FROM table_one ORDER BY id\
                        """
                );
                while (rs.next()) {
                    columns = columns.append(rs.getString(1));
                }
            }
            assertThat(columns).containsExactly(
                    // 10.10 -> 10.1 and numeric fills 0 to all fractional digits
                    "1,10,p1,10,p2,10,p3,10.1,p4,10.1,p5,10.1000000000,p6",
                    "2,-10,n1,-10,n2,-10,n3,-10.1,n4,-10.1,n5,-10.1000000000,n6",
                    "5,55,t2-1,55,t2-2,555555555555555555,t2-3,-5.5,t2-4,6.6,t2-5,777.7770000000,t2-6"
            );
        }
    }

    @Test
    public void testDeleteWithoutParameters() throws SQLException {
        // test
        {
            simple.orm.jdbc.Connection conn = database.connect();
            Query<Void, Integer> query = repository.delete();
            int result = conn.executeDMLQuery(query);
            assertThat(result).isEqualTo(1);
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (Connection conn = directConnect()) {
                ResultSet rs = conn.createStatement().executeQuery(
                        """
                        SELECT concat_ws(',',\
                          id,\
                          to_char(col_ti),col_s1,\
                          to_char(col_si),col_s2,\
                          to_char(col_bi),col_s3,\
                          to_char(col_r),col_s4,\
                          to_char(col_d),col_s5,\
                          to_char(col_nu),col_s6\
                         )\
                         FROM table_one ORDER BY id\
                        """
                );
                while (rs.next()) {
                    columns = columns.append(rs.getString(1));
                }
            }
            assertThat(columns).containsExactly(
                    // 10.10 -> 10.1 and numeric fills 0 to all fractional digits
                    "1,10,p1,10,p2,10,p3,10.1,p4,10.1,p5,10.1000000000,p6"
            );
        }
    }

    @Test
    public void testInsertUpdate() throws SQLException {
        // test
        {
            simple.orm.jdbc.Connection conn = database.connect(10);
            // insert
            Query<Seq<Object>, Integer> query1 = repository.insertIndexed();
            int result1 = conn.executeAnyQuery(query1,
                    3,
                    33, "test1-1",
                    -33, "test1-2",
                    8888888888888888888L, "test1-3",
                    33.33f, "test1-4",
                    -33.33d, "test1-5",
                    new BigDecimal("12345678901234567890.0987654321"), "test1-6"
            );
            assertThat(result1).isEqualTo(1);
            // update
            Query<HasId, Integer> query2 = repository.update();
            int result2 = conn.executeAnyQuery(query2, new HasId(1));
            assertThat(result2).isEqualTo(2);
            // close connection
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (Connection conn = directConnect()) {
                ResultSet rs = conn.createStatement().executeQuery(
                        """
                        SELECT concat_ws(',',\
                          id,\
                          to_char(col_ti),col_s1,\
                          to_char(col_si),col_s2,\
                          to_char(col_bi),col_s3,\
                          to_char(col_r),col_s4,\
                          to_char(col_d),col_s5,\
                          to_char(col_nu),col_s6\
                        )\
                        FROM table_one ORDER BY id\
                        """
                );
                while (rs.next()) {
                    columns = columns.append(rs.getString(1));
                }
            }
            assertThat(columns).containsExactly(
                    // 10.10 -> 10.1 and numeric fills 0 to all fractional digits
                    "1,10,p1,10,p2,10,p3,10.1,p4,10.1,p5,10.1000000000,p6",
                    "2,0,n1,-20,n2,-10,n3,-10.1,n4,-10.1,n5,-10.1000000000,n6",
                    "3,43,test1-1,-43,test1-2,8888888888888888888,test1-3,33.33,test1-4,-33.33,test1-5,12345678901234567890.0987654321,test1-6"
            );
        }
    }

    private static final SQLLoader TEST_SQL_LOADER = new SQLLoader() {
        private final Map<String, String> queries = HashMap.of(
                "test://dml/query=insert", "INSERT INTO table_one VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "test://dml/query=update", "UPDATE table_one SET col_ti=col_ti+10, col_si=col_si-10 WHERE id>?",
                "test://dml/query=delete", "DELETE FROM table_one WHERE id>1"
        );

        @Override
        public String loadFromURI(String uriStr, String charset) {
            return queries.get(uriStr).get();
        }
    };

    @SimpleOrmRepo(type = RepoType.QUERY, timeout = 3)
    public interface DMLRepository {

        @SimpleQuery(type = QueryType.DML, parameters = ParameterStrategy.PROVIDED)
        @QuerySource(uri = "test://dml/query=insert")
        @InjectParam(index = 1, mapper = "int")
        @InjectParam(index = 2, mapper = "tinyint", tag = "int")
        @InjectParam(index = 3, mapper = "varchar")
        @InjectParam(index = 4, mapper = "smallint", tag = "int")
        @InjectParam(index = 5, mapper = "varchar")
        @InjectParam(index = 6, mapper = "bigint")
        @InjectParam(index = 7, mapper = "varchar")
        @InjectParam(index = 8, mapper = "real")
        @InjectParam(index = 9, mapper = "varchar")
        @InjectParam(index = 10, mapper = "double")
        @InjectParam(index = 11, mapper = "varchar")
        @InjectParam(index = 12, mapper = "numeric")
        @InjectParam(index = 13, mapper = "varchar")
        Query<Seq<Object>, Integer> insertIndexed();

        @SimpleQuery(type = QueryType.DML, parameters = ParameterStrategy.PROVIDED, sourceClass = NamedRow2.class)
        @QuerySource(uri = "test://dml/query=insert")
        @InjectParam(prop = "id", mapper = "int")
        @InjectParam(prop = "tiny", mapper = "tinyint", tag = "int")
        @InjectParam(prop = "s1", mapper = "varchar")
        @InjectParam(prop = "small", mapper = "smallint", tag = "int")
        @InjectParam(prop = "s2", mapper = "varchar")
        @InjectParam(prop = "big", mapper = "bigint")
        @InjectParam(prop = "s3", mapper = "varchar")
        @InjectParam(prop = "real", mapper = "real")
        @InjectParam(prop = "s4", mapper = "varchar")
        @InjectParam(prop = "doublePrecision", mapper = "double")
        @InjectParam(prop = "s5", mapper = "varchar")
        @InjectParam(prop = "num", mapper = "numeric")
        @InjectParam(prop = "s6", mapper = "varchar")
        Query<NamedRow2, Integer> insertNamed();

        @SimpleQuery(type = QueryType.DML, parameters = ParameterStrategy.PROVIDED, sourceClass = HasId.class)
        @QuerySource(uri = "test://dml/query=update")
        @InjectParam(prop = "id", jdbc = "INT", java = Integer.class)
        Query<HasId, Integer> update();

        @SimpleQuery(type = QueryType.DML)
        @QuerySource(uri = "test://dml/query=delete")
        Query<Void, Integer> delete();

    }

    public static class NamedRow2 extends NamedRow1 {
        private Integer id;
        private Integer tinyTiny;
        private Integer smallSmall;
        private Long big;
        private Float realFloat;
        private Double doublePrecision;
        private BigDecimal num;

        public NamedRow2(Integer id,
                         Integer tiny, Integer small, Long big,
                         Float real, Double doublePrecision, BigDecimal num,
                         String s1, String s2, String s3, String s4, String s5, String s6) {
            super(s1, s2, s3, s4, s5, s6);
            this.id = id;
            this.tinyTiny = tiny;
            this.smallSmall = small;
            this.big = big;
            this.realFloat = real;
            this.doublePrecision = doublePrecision;
            this.num = num;
        }

        public Integer getTiny() {
            return tinyTiny;
        }

        public Integer getSmall() {
            return smallSmall;
        }

        public Float getReal() {
            return realFloat;
        }

        public Double getDoublePrecision() {
            return doublePrecision;
        }
    }

    public static class NamedRow1 {
        private String s1;
        private String s2;
        private String s3;
        private String s4;
        private String s5;
        private String s6;

        public NamedRow1(String s1, String s2, String s3, String s4, String s5, String s6) {
            this.s1 = s1;
            this.s2 = s2;
            this.s3 = s3;
            this.s4 = s4;
            this.s5 = s5;
            this.s6 = s6;
        }

        public String getS2() {
            return s2;
        }

        public String getS4() {
            return s4;
        }

        public String getS6() {
            return s6;
        }
    }

    public static class HasId {
        protected Integer id;

        public HasId(Integer id) {
            this.id = id;
        }
    }

}
