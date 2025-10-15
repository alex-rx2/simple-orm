package simple.orm.jdbc.map.out;

import io.vavr.collection.Seq;

import java.sql.ResultSet;

/**
 * Extractor of parameters from ResultSet current row as a sequence of indexed values.
 */
// TODO stub interface, make a proper class
public interface IndexedExtractor {

    int getParametersCount();

    Seq<Object> extractRow(ResultSet rs);

}
