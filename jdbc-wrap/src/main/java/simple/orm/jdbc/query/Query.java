package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.InMapper;
import simple.orm.jdbc.map.out.OutMapper;

/**
 * SQL Query.
 *
 * @param <T> expected execution result.
 */
public interface Query<T> {

    QueryType getType();

    String getSQLQuery();

    InMapper getInParametersMapper();

    OutMapper<T> getOutResultMapper();

    int getQueryTimeout();

}
