package simple.orm.jdbc.map;

import io.vavr.collection.Seq;

import java.sql.ResultSet;

/**
 * Extractor of parameters from ResultSet current row as a sequence of values.
 * <br>
 * Indexed in the name means that each row of {@link ResultSet} is extracted as a sequence of Object.
 * <br>
 * Each parameter in a sequence is extracted either by label (column name in {@link ResultSet}),
 * either by index (starting with 1 as per JDBC API).
 */
public interface IndexedExtractor {

    /**
     * Extracts a row of data from {@link ResultSet}.
     *
     * @param rs a ResultSet.
     * @return extracted columns as a sequence of objects.
     */
    Seq<Object> extractRow(ResultSet rs);

}
