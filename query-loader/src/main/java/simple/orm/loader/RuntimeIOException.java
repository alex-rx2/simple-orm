package simple.orm.loader;

import java.io.IOException;

/**
 * Runtime wrap of {@link IOException}.
 */
public class RuntimeIOException extends RuntimeException {

    public RuntimeIOException(IOException ioException) {
        this(ioException.getMessage(), ioException);
    }

    public RuntimeIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
