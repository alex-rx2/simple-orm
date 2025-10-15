package simple.orm.jdbc.map.out;

import java.sql.ResultSet;

/**
 * Extractor of parameters from ResultSet current row as a java object (using object properties names).
 */
// TODO stub interface, make a proper class
public interface NamedExtractor<T> {

    T extractRow(ResultSet rs);

}
