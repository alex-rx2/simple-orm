package simple.orm.jdbc.map;

import io.vavr.collection.Seq;

import java.sql.ResultSet;

/**
 * Extractor of parameters from ResultSet current row as a sequence of values.
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
