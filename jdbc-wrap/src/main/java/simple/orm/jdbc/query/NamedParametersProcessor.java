package simple.orm.jdbc.query;

import io.vavr.Tuple2;

/**
 * Interface for utility class that extracts named parameters from SQL query replacing them with ? in the query.
 */
public interface NamedParametersProcessor {

    /**
     * This method extracts named parameters from SQL query, replacing them with ? in the query.
     *
     * @param sql SQL query.
     * @return {@link Tuple2} of processed query and extracted named parameters.
     */
    Tuple2<String, NamedParametersMap> process(String sql);

}
