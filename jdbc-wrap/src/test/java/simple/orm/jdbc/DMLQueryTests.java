package simple.orm.jdbc;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.common.BasicTypes;
import simple.orm.jdbc.common.InjectorsExtractors;
import simple.orm.jdbc.common.QueryFactory;
import simple.orm.jdbc.query.IndexedQuery;
import simple.orm.jdbc.query.NamedQuery;
import simple.orm.jdbc.query.Query;

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
public class DMLQueryTests extends BaseH2Test {

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
            IndexedQuery query = QFACTORY.iudQuery(
                    "INSERT INTO table_one VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    InjectorsExtractors.indexedInjector().params(
                            BasicTypes.INTEGER,
                            BasicTypes.TINYINT, BasicTypes.VARCHAR,
                            BasicTypes.SMALLINT, BasicTypes.VARCHAR,
                            BasicTypes.BIGINT, BasicTypes.VARCHAR,
                            BasicTypes.FLOAT, BasicTypes.VARCHAR,
                            BasicTypes.DOUBLE, BasicTypes.VARCHAR,
                            BasicTypes.NUMERIC, BasicTypes.VARCHAR
                    ).build()
            );
            conn.executeDMLUpdate(query,
                    3,
                    33, "test1-1",
                    -33, "test1-2",
                    8888888888888888888L, "test1-3",
                    33.33f, "test1-4",
                    -33.33d, "test1-5",
                    new BigDecimal("12345678901234567890.0987654321"), "test1-6"
            );
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (java.sql.Connection conn = directConnect()) {
                ResultSet rs = conn.createStatement().executeQuery("" +
                        "SELECT concat_ws(','," +
                        "  id," +
                        "  to_char(col_ti),col_s1," +
                        "  to_char(col_si),col_s2," +
                        "  to_char(col_bi),col_s3," +
                        "  to_char(col_r),col_s4," +
                        "  to_char(col_d),col_s5," +
                        "  to_char(col_nu),col_s6" +
                        ")" +
                        "\nFROM table_one ORDER BY id"
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
            NamedQuery<NamedRow2, Void> query = QFACTORY.iudQuery(
                    """
                    INSERT INTO table_one\
                     VALUES (:id, :tiny, :s1, :small, :s2, :big, :s3, :real, :s4, :doublePrecision, :s5, :num, :s6)
                    """,
                    InjectorsExtractors.<NamedRow2>namedInjector()
                            .param("id", BasicTypes.INTEGER)
                            .param("tiny", BasicTypes.TINYINT)
                            .param("s1", BasicTypes.VARCHAR)
                            .param("small", BasicTypes.SMALLINT)
                            .param("s2", BasicTypes.VARCHAR)
                            .param("big", BasicTypes.BIGINT)
                            .param("s3", BasicTypes.VARCHAR)
                            .param("real", BasicTypes.REAL)
                            .param("s4", BasicTypes.VARCHAR)
                            .param("doublePrecision", BasicTypes.DOUBLE)
                            .param("s5", BasicTypes.VARCHAR)
                            .param("num", BasicTypes.NUMERIC)
                            .param("s6", BasicTypes.VARCHAR)
                            .build()
            );
            conn.executeDMLUpdate(query,
                    new NamedRow2(5,
                            55, 55, 555555555555555555L, -5.5f, 6.6, new BigDecimal("777.777"),
                            "t2-1", "t2-2", "t2-3", "t2-4", "t2-5", "t2-6"
                    )
            );
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (java.sql.Connection conn = directConnect()) {
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
                         FROM table_one ORDER BY id
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
    public void testInsertNamedPartiallyOtherPropNames() throws SQLException {
        // test
        {
            simple.orm.jdbc.Connection conn = database.connect(10);
            NamedQuery<NamedRow2, Void> query = QFACTORY.iudQuery(
                    """
                    INSERT INTO table_one\
                     VALUES (:111, :222, :333, :aaa, :bbb, :___, :s3, :real, :s4, :doublePrecision, :s5, :num, :s6)
                    """,
                    InjectorsExtractors.<NamedRow2>namedInjector()
                            .param("111", BasicTypes.INTEGER, "id")
                            .param("222", BasicTypes.TINYINT, "tiny")
                            .param("333", BasicTypes.VARCHAR, "s1")
                            .param("aaa", BasicTypes.SMALLINT, "small")
                            .param("bbb", BasicTypes.VARCHAR, "s2")
                            .param("___", BasicTypes.BIGINT, "big")
                            .param("s3", BasicTypes.VARCHAR)
                            .param("real", BasicTypes.REAL)
                            .param("s4", BasicTypes.VARCHAR)
                            .param("doublePrecision", BasicTypes.DOUBLE)
                            .param("s5", BasicTypes.VARCHAR)
                            .param("num", BasicTypes.NUMERIC)
                            .param("s6", BasicTypes.VARCHAR)
                            .build()
            );
            conn.executeDMLUpdate(query,
                    new NamedRow2(5,
                            55, 55, 555555555555555555L, -5.5f, 6.6, new BigDecimal("777.777"),
                            "t2-1", "t2-2", "t2-3", "t2-4", "t2-5", "t2-6"
                    )
            );
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (java.sql.Connection conn = directConnect()) {
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
                         FROM table_one ORDER BY id
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
            Query query = QFACTORY.iudQueryWithoutParameters("DELETE FROM table_one WHERE id>1");
            conn.executeDMLUpdate(query);
            conn.close();
        }
        // verify
        {
            Seq<String> columns = List.empty();
            try (java.sql.Connection conn = directConnect()) {
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
                         FROM table_one ORDER BY id
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

}
