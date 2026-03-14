package simple.orm.loader;

import simple.orm.loader.impl.QuerySourceImpl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * SQL query source.
 */
public interface QuerySource {

    /**
     * Returns {@link Reader} that provides SQL query.
     *
     * @return SQL query {@link Reader}.
     */
    Reader getReader();

    static QuerySource of(Reader reader) {
        return new QuerySourceImpl(reader);
    }

    static QuerySource of(String sql) {
        return of(new StringReader(sql));
    }

    static QuerySource of(InputStream inputStream, Charset charset) {
        return of(new InputStreamReader(inputStream, charset));
    }

}
