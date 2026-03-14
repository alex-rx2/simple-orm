package simple.orm.loader.impl.parser;

import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;

/**
 * Default {@link QueryParser} implementation.
 * <br>
 * Parameters information is embedded into SQL via SQL comments.
 * <br>
 * For more details see {@link ParameterParserUtil}.
 *
 * @see ParameterParserUtil
 */
public class QueryParserImpl implements QueryParser {

    @Override
    public ParsedQuery parse(QuerySource source) {
        QueryParserInternal internal = new QueryParserInternal(source);
        return internal.extract();
    }

}
