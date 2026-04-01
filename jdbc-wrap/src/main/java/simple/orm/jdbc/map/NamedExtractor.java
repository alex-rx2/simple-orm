package simple.orm.jdbc.map;

import java.sql.ResultSet;

/**
 * Extractor of parameters from ResultSet current row as a java object (using object properties names).
 * <br>
 * Named in the name means that each row of {@link ResultSet} is extracted as a single Object
 * with its properties (using property name) assigned the extracted values.
 * <br>
 * Each parameter (property value) is extracted either by label (column name in {@link ResultSet}),
 * either by index (starting with 1 as per JDBC API).
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
