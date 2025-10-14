package simple.orm.jdbc.query;

/**
 * SQL Query.
 */
public interface Query {

    QueryType getType();

    String getSQLQuery();

    int getQueryTimeout();

}
