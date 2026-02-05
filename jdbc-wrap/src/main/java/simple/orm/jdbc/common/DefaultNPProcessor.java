package simple.orm.jdbc.common;

import io.vavr.Tuple2;
import simple.orm.jdbc.query.NamedParametersMap;

/**
 * Default implementation of {@link QueryFactory.NamedParametersProcessor}.
 * Named parameters in SQL query are strings of format ":[a-zA-Z0-9_]+" that are not inside comments or SQL string literals.
 * Said parameters are collected and replaced inside query with "?".
 */
public class DefaultNPProcessor implements QueryFactory.NamedParametersProcessor {

    @Override
    public Tuple2<String, NamedParametersMap> process(String sql) {
        return new DefaultNPProcessorExtractor(sql).extract();
    }

}
