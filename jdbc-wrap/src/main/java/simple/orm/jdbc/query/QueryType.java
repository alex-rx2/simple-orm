package simple.orm.jdbc.query;

/**
 * Type of SQL query (in terms of JDBC processing).
 */
public enum QueryType {

    EXECUTE_UPDATE,
    EXECUTE_QUERY,
    // TODO more types
    //  (callable at least, batch updates/inserts maybe should be separate query type for ease of implementation
    //  and logic separation)

}
