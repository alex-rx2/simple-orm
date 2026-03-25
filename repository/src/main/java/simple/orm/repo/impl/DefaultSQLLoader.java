package simple.orm.repo.impl;

import simple.orm.repo.RepositoryBuilderException;
import simple.orm.repo.SQLLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import static simple.orm.util.StringUtils.qnn;

/**
 * Basic implementation of {@link SQLLoader}.
 * <br>
 * Note: without any validations or whatsoever, straightforward <code>URL.openConnection().getInputStream()</code>.
 */
public class DefaultSQLLoader implements SQLLoader {

    @Override
    public String loadFromURI(String uriStr, String charset) {
        if (uriStr == null) {
            throw new NullPointerException("uriStr is null");
        }
        URI uri = URI.create(uriStr);
        try (InputStream is = uri.toURL().openStream()) {
            BufferedInputStream bis = is instanceof BufferedInputStream isBis ? isBis : new BufferedInputStream(is);
            ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
            for (int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
            }
            return buf.toString(charset);
        } catch (MalformedURLException e) {
            throw new RepositoryBuilderException("wrong query SQL URI: " + qnn(uriStr), e);
        } catch (IOException e) {
            throw new RepositoryBuilderException("failed to read query SQL from provided URI: " + qnn(uriStr), e);
        }
    }

}
