package simple.orm.loader.impl;

import simple.orm.loader.QuerySource;

import java.io.Reader;

/**
 * Implementation of {@link QuerySource}.
 */
public class QuerySourceImpl implements QuerySource {

    private final Reader reader;

    public QuerySourceImpl(Reader reader) {
        if (reader == null) {
            throw new NullPointerException("reader is null");
        }
        this.reader = reader;
    }

    @Override
    public Reader getReader() {
        return reader;
    }

}
