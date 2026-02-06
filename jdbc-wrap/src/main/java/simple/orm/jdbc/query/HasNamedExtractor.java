package simple.orm.jdbc.query;

import simple.orm.jdbc.map.NamedExtractor;

/**
 * Interface-provider of named extractor for a query.
 * Such a query produces <code>ResultSet</code> where each row is extracted as a single java object
 * (with <code>ResultSet</code> columns passed as properties of said object).
 *
 * @param <T> class of object extracted as each ResultSet row.
 */
public interface HasNamedExtractor<T> {

    /**
     * Returns parameters extractor.
     *
     * @return parameters extractor.
     */
    NamedExtractor<T> getExtractor();

}
