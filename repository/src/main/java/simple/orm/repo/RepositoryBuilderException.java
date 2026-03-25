package simple.orm.repo;

/**
 * {@link RepositoryBuilder} exception.
 */
public class RepositoryBuilderException extends RuntimeException {
    public RepositoryBuilderException(String message) {
        super(message);
    }

    public RepositoryBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
