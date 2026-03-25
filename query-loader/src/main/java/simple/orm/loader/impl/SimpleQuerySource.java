package simple.orm.loader.impl;

import simple.orm.loader.QuerySource;

import java.io.Reader;

/**
 * Basic implementation of {@link QuerySource}.
 */
public class SimpleQuerySource implements QuerySource {

    private final Reader reader;

    public SimpleQuerySource(Reader reader) {
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
