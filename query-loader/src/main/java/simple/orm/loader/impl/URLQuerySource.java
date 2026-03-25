package simple.orm.loader.impl;

import simple.orm.loader.QuerySource;
import simple.orm.loader.RuntimeIOException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * URL-based implementation of {@link QuerySource}.
 */
public class URLQuerySource implements QuerySource {

    private final URL url;
    private final Charset charset;

    public URLQuerySource(URL url, Charset charset) {
        this.url = url;
        this.charset = charset;
    }

    @Override
    public Reader getReader() {
        try {
            return new InputStreamReader(url.openStream(), charset);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

}
