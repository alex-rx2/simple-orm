package simple.orm.repo.anno;

/**
 * Strategy for obtaining query parameters information.
 */
public enum ParameterStrategy {

    /**
     * Parameters should be parsed out from the query itself.
     */
    PARSE_QUERY,

    /**
     * Parameters will be provided with dedicated annotations.
     */
    PROVIDED,

}
