package simple.orm.jdbc.query;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.param.ParameterMapper;

/**
 * SQL Query.
 *
 * @param <T> expected execution result.
 */
public interface Query<T> {

    QueryType getType();

    String getSQLQuery();

    // TODO replace with InMapper
    Seq<ParameterMapper<?, ?>> getInParametersMappers();

    // TODO replace with OutMapper
    Seq<ParameterMapper<?, ?>> getOutResultMappers();

    int getQueryTimeout();

}
