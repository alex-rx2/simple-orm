package simple.orm.jdbc.map.in;

import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.query.NamedParametersMap;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Injector of properties of object into JDBC statement (using object properties names and named parameters in a query).
 *
 * @param <T> class of object used as source of parameters' values for query.
 */
public interface NamedInjector<T> {

    /**
     * Injects parameters into {@link PreparedStatement}.
     *
     * @param stmt          {@link PreparedStatement}.
     * @param source        object with properties to be injected as parameters.
     * @param parametersMap mapping of parameter names from original query into parameter indexes in statement.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void injectParameters(PreparedStatement stmt, T source, NamedParametersMap parametersMap);

}
