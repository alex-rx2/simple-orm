package simple.orm.h2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.mapping.param.ParameterJdbcType;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests on types declared in {@link H2Types}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class H2TypesTest extends BaseH2Test {

    @BeforeAll
    void setUp() throws SQLException {
        dropAllObjects();
    }

    @AfterAll
    void tearDown() throws SQLException {
        dropAllObjects();
    }

    @BeforeEach
    void beforeTest() throws SQLException {
        dropAllObjects();
    }

    @Test
    public void testCharacter() throws SQLException {
        final ParameterJdbcType<String> type = H2Types.CHARACTER;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 CHARACTER(10) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, "value12345");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("value12345");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("value12345");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testCharacterVarying() throws SQLException {
        final ParameterJdbcType<String> type = H2Types.CHARACTER_VARYING;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 CHARACTER VARYING(20) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, "a value");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("a value");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("a value");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testCharacterLargeObject() throws SQLException {
        final ParameterJdbcType<Clob> type = H2Types.CHARACTER_LARGE_OBJECT;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 CHARACTER LARGE OBJECT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, new SerialClob("this is a very very large character object, trust me".toCharArray()));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("this is a very very large character object, trust me");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                Clob value = type.getGetter().getValue(rs, 1);
                assertThat(value.getSubString(1, (int) value.length())).isEqualTo("this is a very very large character object, trust me");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testVarcharIgnorecase() throws SQLException {
        final ParameterJdbcType<String> type = H2Types.VARCHAR_IGNORECASE;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 CHARACTER VARYING(20) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, "a value");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("a value");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("a value");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testBinary() throws SQLException {
        final ParameterJdbcType<byte[]> type = H2Types.BINARY;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 BINARY(10) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, new byte[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBytes(1)).isEqualTo(new byte[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBytes(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(new byte[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testBinaryVarying() throws SQLException {
        final ParameterJdbcType<byte[]> type = H2Types.BINARY_VARYING;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 BINARY VARYING(10) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, new byte[]{10, 20, 30});
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBytes(1)).isEqualTo(new byte[]{10, 20, 30});
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBytes(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(new byte[]{10, 20, 30});
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testBinaryLargeObject() throws SQLException {
        final ParameterJdbcType<Blob> type = H2Types.BINARY_LARGE_OBJECT;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 BINARY LARGE OBJECT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, new SerialBlob(new byte[]{11, 111, 1}));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBytes(1)).isEqualTo(new byte[]{11, 111, 1});
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBytes(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                Blob value = type.getGetter().getValue(rs, 1);
                assertThat(value.getBytes(1, (int) value.length())).isEqualTo(new byte[]{11, 111, 1});
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testBoolean() throws SQLException {
        final ParameterJdbcType<Boolean> type = H2Types.BOOLEAN;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 BOOLEAN NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, true);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, false);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBoolean(1)).isEqualTo(true);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBoolean(1)).isEqualTo(false);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBoolean(1)).isEqualTo(false);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(true);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(false);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testBooleanNonNull() throws SQLException {
        final ParameterJdbcType<Boolean> type = H2Types.BOOLEAN_NONNULL;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 BOOLEAN NOT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, true);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, false);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBoolean(1)).isEqualTo(true);
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBoolean(1)).isEqualTo(false);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(true);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(false);
            }
        }
    }

    @Test
    public void testTinyint() throws SQLException {
        final ParameterJdbcType<Byte> type = H2Types.TINYINT;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 TINYINT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, (byte) 111);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, (byte) 0);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getByte(1)).isEqualTo((byte) 111);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getByte(1)).isEqualTo((byte) 0);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getByte(1)).isEqualTo((byte) 0);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo((byte) 111);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo((byte) 0);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testSmallint() throws SQLException {
        final ParameterJdbcType<Short> type = H2Types.SMALLINT;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 SMALLINT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, (short) -222);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, (short) 0);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getShort(1)).isEqualTo((short) -222);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getShort(1)).isEqualTo((short) 0);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getShort(1)).isEqualTo((short) 0);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo((short) -222);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo((short) 0);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testInteger() throws SQLException {
        final ParameterJdbcType<Integer> type = H2Types.INTEGER;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 INTEGER NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, 123);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, 0);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(123);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(0);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(0);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(123);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(0);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testBigint() throws SQLException {
        final ParameterJdbcType<Long> type = H2Types.BIGINT;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 BIGINT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, 1234567890L);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, 0L);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getLong(1)).isEqualTo(1234567890L);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getLong(1)).isEqualTo(0);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getLong(1)).isEqualTo(0);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(1234567890L);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(0);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testNumeric() throws SQLException {
        final ParameterJdbcType<BigDecimal> type = H2Types.NUMERIC;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 NUMERIC(12,6) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, BigDecimal.valueOf(-112233.445566));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBigDecimal(1)).isEqualTo(BigDecimal.valueOf(-112233.445566));
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBigDecimal(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(BigDecimal.valueOf(-112233.445566));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testReal() throws SQLException {
        final ParameterJdbcType<Float> type = H2Types.REAL;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 REAL NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, 321.123f);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, 0.0f);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getFloat(1)).isEqualTo(321.123f);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getFloat(1)).isEqualTo(0.0f);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getFloat(1)).isEqualTo(0.0f);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(321.123f);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(0.0f);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testDoublePrecision() throws SQLException {
        final ParameterJdbcType<Double> type = H2Types.DOUBLE_PRECISION;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 DOUBLE PRECISION NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, 123.321);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, 0.0);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble(1)).isEqualTo(123.321);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble(1)).isEqualTo(0.0);
                assertThat(rs.wasNull()).isFalse();
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble(1)).isEqualTo(0.0);
                assertThat(rs.wasNull()).isTrue();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(123.321);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(0.0);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testDecfloat() throws SQLException {
        final ParameterJdbcType<String> type = H2Types.DECFLOAT;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 DECFLOAT NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, "-123.456");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, "Infinity");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, "-Infinity");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, "NaN");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("-123.456");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("Infinity");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("-Infinity");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("NaN");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("-123.456");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("Infinity");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("-Infinity");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("NaN");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testDate() throws SQLException {
        final ParameterJdbcType<LocalDate> type = H2Types.DATE;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 DATE NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, LocalDate.of(2022, Month.FEBRUARY, 24));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, LocalDate.of(2026, 3, 5));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("2022-02-24");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("2026-03-05");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(LocalDate.of(2022, Month.FEBRUARY, 24));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(LocalDate.of(2026, 3, 5));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testTime() throws SQLException {
        final ParameterJdbcType<LocalTime> type = H2Types.TIME;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 TIME(9) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, LocalTime.of(20, 58, 0));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, LocalTime.of(10, 21, 33, 123456789));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("20:58:00");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("10:21:33.123456789");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(LocalTime.of(20, 58, 0));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(LocalTime.of(10, 21, 33, 123456789));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testTimeWithTimezone() throws SQLException {
        final ParameterJdbcType<OffsetTime> type = H2Types.TIME_WITH_TIMEZONE;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 TIME(9) WITH TIME ZONE NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, OffsetTime.of(LocalTime.of(20, 58, 0), ZoneOffset.ofHours(+10)));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, OffsetTime.of(10, 21, 33, 123456789,
                        ZoneOffset.ofTotalSeconds(TimeZone.getTimeZone("America/Jamaica").getOffset(0) / 1000)
                ));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("20:58:00+10");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("10:21:33.123456789-05");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isEqualTo(OffsetTime.of(LocalTime.of(20, 58, 0), ZoneOffset.ofHours(+10)));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isEqualTo(OffsetTime.of(10, 21, 33, 123456789, ZoneOffset.of("-05:00")));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isNull();
            }
        }
    }

    @Test
    public void testTimestamp() throws SQLException {
        final ParameterJdbcType<LocalDateTime> type = H2Types.TIMESTAMP;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 TIMESTAMP(3) NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, LocalDateTime.of(2026, 3, 5, 20, 58, 0));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, LocalDateTime.of(1910, Month.DECEMBER, 13, 10, 21, 33, 123456789));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("2026-03-05 20:58:00");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("1910-12-13 10:21:33.123"); // nano is rounded to 3
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isEqualTo(LocalDateTime.of(2026, 3, 5, 20, 58, 0));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isEqualTo(LocalDateTime.of(1910, Month.DECEMBER, 13, 10, 21, 33, 123000000)); // nano is rounded to 3
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isNull();
            }
        }
    }

    @Test
    public void testTimestampWithTimezone() throws SQLException {
        final ParameterJdbcType<OffsetDateTime> type = H2Types.TIMESTAMP_WITH_TIMEZONE;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 TIMESTAMP(0) WITH TIME ZONE NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, OffsetDateTime.of(2026, 3, 5, 20, 58, 0, 0, ZoneOffset.ofHours(+10)));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, OffsetDateTime.of(
                        LocalDateTime.of(1910, Month.DECEMBER, 13, 10, 21, 33, 123456789),
                        ZoneOffset.ofTotalSeconds(TimeZone.getTimeZone("America/Jamaica").getOffset(0) / 1000)
                ));
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("2026-03-05 20:58:00+10");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("1910-12-13 10:21:33-05"); // no nano
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isEqualTo(OffsetDateTime.of(2026, 3, 5, 20, 58, 0, 0, ZoneOffset.ofHours(+10)));
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isEqualTo(OffsetDateTime.of(1910, 12, 13, 10, 21, 33, 0, ZoneOffset.ofHours(-5))); // no nano
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1))
                        .isNull();
            }
        }
    }

    @Test
    public void testUUID() throws SQLException {
        final UUID uuid1 = UUID.randomUUID();
        final UUID uuid2 = UUID.randomUUID();
        final ParameterJdbcType<UUID> type = H2Types.UUID;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 UUID NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, uuid1);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, uuid2);
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo(uuid1.toString());
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo(uuid2.toString());
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(uuid1);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo(uuid2);
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

    @Test
    public void testEnum() throws SQLException {
        final ParameterJdbcType<String> type = H2Types.ENUM;
        try (Connection conn = directConnect()) {
            // create table
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE test_table(id INT AUTO_INCREMENT NOT NULL, col1 ENUM('ONE','TWO') NULL)");
                stmt.close();
            }
            // setter test
            {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table(col1) VALUES (?)");
                type.getSetter().setValue(pstmt, 1, "ONE");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, "TWO");
                pstmt.executeUpdate();
                type.getSetter().setValue(pstmt, 1, null);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // verify data
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("ONE");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("TWO");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isNull();
                assertThat(rs.next()).isFalse();
            }
            // getter test
            {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table ORDER BY id");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("ONE");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isEqualTo("TWO");
                rs.next();
                assertThat(type.getGetter().getValue(rs, 1)).isNull();
            }
        }
    }

}
