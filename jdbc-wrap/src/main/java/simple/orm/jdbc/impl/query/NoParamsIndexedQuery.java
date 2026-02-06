package simple.orm.jdbc.impl.query;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.query.HasIndexedExtractor;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} without parameters, with {@link ResultSet} rows extracted by {@link IndexedExtractor}.
 */
public class NoParamsIndexedQuery extends BasicQuery<Void, Seq<Object>> implements HasIndexedExtractor {

    private final IndexedExtractor extractor;

    public NoParamsIndexedQuery(QueryType queryType, String sql, int queryTimeoutSec,
                                IndexedExtractor extractor) {
        super(queryType, sql, queryTimeoutSec);
        this.extractor = extractor;
    }

    @Override
    public IndexedExtractor getExtractor() {
        return extractor;
    }

}
