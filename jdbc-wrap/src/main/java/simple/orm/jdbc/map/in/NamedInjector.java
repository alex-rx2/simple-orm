package simple.orm.jdbc.map.in;

import java.sql.Statement;

/**
 * Injector of properties of object into JDBC statement (using object properties names and named parameters in a query).
 */
// TODO stub interface, make a proper class
public interface NamedInjector<T> {

    void injectParameters(Statement stmt, T source);

}
