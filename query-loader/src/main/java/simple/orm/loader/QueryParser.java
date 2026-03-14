package simple.orm.loader;

import io.vavr.collection.Traversable;
import simple.orm.loader.impl.parser.ParameterParserUtil;
import simple.orm.loader.impl.parser.QueryParserImpl;

import java.io.IOException;
import java.io.Reader;

/**
 * Interface for query parser.
 * <br>
 * Query parser loads query from its source and parses out parameter information provided within query itself.
 * <br>
 * Implementing classes should be stateless and thread-safe.
 * <br>
 * For parameter information syntax details see {@link ParameterParserUtil}.
 *
 * @see ParameterParserUtil
 */
public interface QueryParser {

    /**
     * Parameter type.
     */
    enum ParamType {
        INJECTION, EXTRACTION
    }

    /**
     * Provided parameter information.
     */
    record QueryParam(ParamType type,
                      int indexWithinType, // starting with 1 as per JDBC API
                      String label,
                      boolean labelGuessed, // label was guessed from last word in SQL
                      String propName,
                      String mapperName,
                      String tag,
                      String jdbcTypeName,
                      String javaClassName) {
    }

    /**
     * SQL query parsing result.
     */
    record ParsedQuery(String querySQL,
                       Traversable<QueryParam> parsedParams) {
    }

    /**
     * Method, reading query from source and parsing out provided parameters information.
     *
     * @param source query source.
     * @return parsing result.
     * @throws RuntimeIOException wrap around possible {@link IOException} working with source {@link Reader}.
     */
    ParsedQuery parse(QuerySource source);

    /**
     * Factory method, returning default implementation.
     *
     * @return default parser implementation.
     */
    static QueryParser defaultParser() {
        return new QueryParserImpl();
    }

}
