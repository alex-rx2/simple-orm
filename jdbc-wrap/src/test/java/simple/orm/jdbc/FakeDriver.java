package simple.orm.jdbc;

import java.sql.*;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Fake JDBC driver for some tests.
 */
public class FakeDriver implements Driver {
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean jdbcCompliant() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException();
    }
}
