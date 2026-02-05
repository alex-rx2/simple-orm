package simple.orm.jdbc.map;

import io.vavr.collection.Seq;
import simple.orm.jdbc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Injector of sequence of parameters into JDBC statement (by index).
 */
public interface IndexedInjector {

    /**
     * Injects parameters into {@link PreparedStatement}.
     *
     * @param stmt   {@link PreparedStatement}.
     * @param params parameters to inject.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void injectParameters(PreparedStatement stmt, Object... params);

    /**
     * Injects parameters into {@link PreparedStatement}.
     *
     * @param stmt   {@link PreparedStatement}.
     * @param params parameters to inject.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void injectParameters(PreparedStatement stmt, Seq<Object> params);

}
