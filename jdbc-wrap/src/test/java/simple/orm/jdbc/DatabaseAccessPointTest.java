package simple.orm.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Simple test on DatabaseAccessPoint connecting to actual database.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseAccessPointTest extends BaseH2Test {

    private DatabaseAccessPoint dbAccessPoint;

    @BeforeEach
    void setUp() {
        dbAccessPoint = DatabaseAccessPoint.builder()
                .driverClass(h2DriverClass)
                .connectionUrl(h2InMemUrl)
                .connectionProperties(h2ConnectionProperties)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (!dbAccessPoint.isClosed()) dbAccessPoint.close();
    }

    @Test
    public void testConnect() throws Exception {
        Connection connection = dbAccessPoint.connect();
        assertThat(connection).isInstanceOf(ConnectionImpl.class);
        java.sql.Connection jdbcConnection = ((ConnectionImpl) connection).jdbcConnection;
        assertThat(jdbcConnection).isNotNull();

        Statement stmt = jdbcConnection.createStatement();
        ResultSet rs = stmt.executeQuery("select '123'");
        rs.next();
        String result = rs.getString(1);
        assertThat(result).isEqualTo("123");

        dbAccessPoint.close();

        assertThatCode(() -> dbAccessPoint.connect())
                .isInstanceOf(DatabaseClosedException.class);
    }
}
