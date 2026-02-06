package simple.orm.jdbc.query;

import simple.orm.jdbc.map.IndexedExtractor;

/**
 * Interface-provider of indexed extractor for a query.
 * Such a query produces <code>ResultSet</code> where each row is extracted as sequence of objects by column index.
 */
public interface HasIndexedExtractor {

    /**
     * Returns parameters extractor.
     *
     * @return parameters extractor.
     */
    IndexedExtractor getExtractor();

}
