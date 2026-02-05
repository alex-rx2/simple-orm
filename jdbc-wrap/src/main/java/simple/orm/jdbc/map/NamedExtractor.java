package simple.orm.jdbc.map;

import java.sql.ResultSet;

/**
 * Extractor of parameters from ResultSet current row as a java object (using object properties names).
 *
 * @param <T> class of object extracted as each ResultSet row.
 */
public interface NamedExtractor<T> {

    /**
     * Extracts a row of data from {@link ResultSet}.
     *
     * @param rs a ResultSet.
     * @return extracted row as object properties.
     */
    T extractRow(ResultSet rs);

}
