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

    Seq<ParameterMapper<?, ?>> getInParametersMappers();

    Seq<ParameterMapper<?, ?>> getOutResultMappers();

    int getQueryTimeout();

}
