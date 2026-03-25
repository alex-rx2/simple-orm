package simple.orm.loader;

import simple.orm.loader.impl.SimpleQuerySource;
import simple.orm.loader.impl.URLQuerySource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

    /**
     * Returns <code>true</code> if reader should be closed after query was read from it (or on error).
     *
     * @return <code>true</code> if reader should be closed after query was read from it (or on error).
     */
    default boolean autoclose() {
        return true;
    }

    static QuerySource of(Reader reader) {
        return new SimpleQuerySource(reader);
    }

    static QuerySource of(String sql) {
        return of(new StringReader(sql));
    }

    static QuerySource of(InputStream inputStream, Charset charset) {
        return of(new InputStreamReader(inputStream, charset));
    }

    static QuerySource of(String uri, String charset) throws MalformedURLException {
        return new URLQuerySource(URI.create(uri).toURL(), Charset.forName(charset));
    }

    static QuerySource of(URL url, Charset charset) {
        return new URLQuerySource(url, charset);
    }

}
