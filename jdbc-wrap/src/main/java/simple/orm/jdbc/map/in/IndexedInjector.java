package simple.orm.jdbc.map.in;

import io.vavr.collection.Seq;

import java.sql.Statement;

/**
 * Injector of sequence of parameters into JDBC statement (by index).
 */
// TODO stub interface, make a proper class
public interface IndexedInjector {

    int getParametersCount();

    void injectParameters(Statement stmt, Object... params);

    void injectParameters(Statement stmt, Seq<Object> params);

}
