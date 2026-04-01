package simple.orm.jdbc.map;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import simple.orm.jdbc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Injector of sequence of parameters into JDBC statement (by index).
 * <br>
 * Indexed in the name means that source of parameters is a plain sequence of Objects.
 * <br>
 * Each parameter in a sequence is injected under incrementing index starting with 1 (as per JDBC API).
 */
public interface IndexedInjector {

    /**
     * Injects parameters into {@link PreparedStatement}.
     *
     * @param stmt   {@link PreparedStatement}.
     * @param values parameters to inject.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    default void injectParameters(PreparedStatement stmt, Object... values) {
        injectParameters(stmt, Array.of(values));
    }

    /**
     * Injects parameters into {@link PreparedStatement}.
     *
     * @param stmt   {@link PreparedStatement}.
     * @param values parameters to inject.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void injectParameters(PreparedStatement stmt, Seq<Object> values);

}
