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
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

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

    private static final QueryLoader QLOADER = QueryLoader.of(
            QueryParser.defaultParser(),
            H2Types.collection(),
            H2Mappers.collection()
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
            simple.orm.jdbc.Connection conn = database.connect(10);
            Query<Seq<Object>, Integer> query = QLOADER.loadQuery(
                    QueryType.DML,
                    QuerySource.of("""
                                   INSERT INTO table_one VALUES (
                                     ?, --?::int
                                     ?, --?::tinyint(int)
                                     ?, --?::varchar
                                     ?, --?::smallint(int)
                                     ?, --?::varchar
                                     ?, --?::bigint
                                     ?, --?::varchar
                                     ?, --?::real
                                     ?, --?::varchar
                                     ?, --?::double
                                     ?, --?::varchar
                                     ?, --?::numeric
                                     ?  --?::varchar
                                   )
                                   """),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.noneDml()
            );
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
            simple.orm.jdbc.Connection conn = database.connect(10);
            Query<NamedRow2, Integer> query = QLOADER.loadQuery(
                    QueryType.DML,
                    QuerySource.of("""
                                   INSERT INTO table_one VALUES (
                                     ?, --?:id::INT
                                     ?, --?:tiny::TINYINT
                                     ?, --?:s1::VARCHAR
                                     ?, --?:small::SMALLINT
                                     ?, --?:s2::VARCHAR
                                     ?, --?:big::BIGINT
                                     ?, --?:s3::VARCHAR
                                     ?, --?:real::REAL
                                     ?, --?:s4::VARCHAR
                                     ?, --?:doublePrecision::DOUBLE
                                     ?, --?:s5::VARCHAR
                                     ?, --?:num::NUMERIC
                                     ?  --?:s6::VARCHAR
                                   )
                                   """),
                    InjectionStrategy.named(NamedRow2.class),
                    ExtractionStrategy.noneDml()
            );
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
            simple.orm.jdbc.Connection conn = database.connect(10);
            Query<Void, Integer> query = QLOADER.loadQuery(
                    QueryType.DML,
                    QuerySource.of("DELETE FROM table_one WHERE id>1"),
                    InjectionStrategy.none(),
                    ExtractionStrategy.noneDml()
            );
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
    public void executeAnyQuery() throws SQLException {
        // test
        {
            simple.orm.jdbc.Connection conn = database.connect(10);
            // insert
            Query<Seq<Object>, Integer> query1 = QLOADER.loadQuery(
                    QueryType.DML,
                    QuerySource.of("""
                                   INSERT INTO table_one VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                   /*
                                   ??:::INT
                                   ??:::TINYINT
                                   ??:::VARCHAR
                                   ??:::SMALLINT
                                   ??:::VARCHAR
                                   ??:::BIGINT
                                   ??:::VARCHAR
                                   ??:::REAL
                                   ??:::VARCHAR
                                   ??:::DOUBLE
                                   ??:::VARCHAR
                                   ??:::NUMERIC
                                   ??:::VARCHAR
                                   */
                                   """),
                    InjectionStrategy.indexed(),
                    ExtractionStrategy.noneDml()
            );
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
            Query<HasId, Integer> query2 = QLOADER.loadQuery(
                    QueryType.DML,
                    // note: h2 provides correct SQL type for parameter in PreparedStatement metadata (wow, didn't expect that actually)
                    QuerySource.of(
                            """
                            UPDATE table_one SET col_ti=col_ti+10, col_si=col_si-10 WHERE id>?
                            --??:id:
                            """
                    ),
                    InjectionStrategy.named(HasId.class),
                    ExtractionStrategy.noneDml()
            );
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

    private static class NamedRow2 extends NamedRow1 {
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

    private static class NamedRow1 {
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

    private static class HasId {
        protected Integer id;

        public HasId(Integer id) {
            this.id = id;
        }
    }

}
