package simple.orm.jdbc.map;

import simple.orm.jdbc.JdbcException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Injector of properties of object into JDBC statement (using object properties names and named parameters in a query).
 * <br>
 * Named in the name means that source of parameters is a single Object with named properties.
 * <br>
 * Each parameter in a sequence is injected under incrementing index starting with 1 (as per JDBC API).
 *
 * @param <T> class of object used as source of parameters' values for query.
 */
public interface NamedInjector<T> {

    /**
     * Injects parameters into {@link PreparedStatement}.
     *
     * @param stmt   {@link PreparedStatement}.
     * @param source object with properties to be injected as parameters.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void injectParameters(PreparedStatement stmt, T source);

}
