package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.query.HasNamedExtractor;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} without parameters, with {@link ResultSet} rows extracted by {@link NamedExtractor}.
 *
 * @param <R> type of java object extracted from ResultSet row.
 */
public class NoParamsNamedQuery<R> extends BasicQuery<Void, R> implements HasNamedExtractor<R> {

    private final NamedExtractor<R> extractor;

    public NoParamsNamedQuery(QueryType queryType, String sql, int queryTimeoutSec,
                              NamedExtractor<R> extractor) {
        super(queryType, sql, queryTimeoutSec);
        this.extractor = extractor;
    }

    @Override
    public NamedExtractor<R> getExtractor() {
        return extractor;
    }

}
