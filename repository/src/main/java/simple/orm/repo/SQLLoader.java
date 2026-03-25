package simple.orm.repo;

import simple.orm.repo.impl.DefaultSQLLoader;

import java.io.InputStream;

/**
 * Utility interface to load query SQL from provided URI.
 */
public interface SQLLoader {

    /**
     * Load query SQL from provided URI with provided character encoding.
     *
     * @param uriStr  resource URI.
     * @param charset charset to convert byte {@link InputStream} into {@link String}.
     * @return query SQL.
     */
    String loadFromURI(String uriStr, String charset);

    /**
     * Return default {@link SQLLoader} implementation.
     * <br>
     * This implementation just use straightforward <code>URL.openConnection().getInputStream()</code>
     * and read the whole stream without any validations or whatsoever.
     *
     * @return default {@link SQLLoader} implementation.
     */
    static SQLLoader defaultLoader() {
        return new DefaultSQLLoader();
    }

}
