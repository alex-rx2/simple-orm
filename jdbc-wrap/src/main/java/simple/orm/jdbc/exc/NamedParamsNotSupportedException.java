package simple.orm.jdbc.exc;

/**
 * Named parameters not supported in provided query or mapper.
 */
public class NamedParamsNotSupportedException extends RuntimeException {
    public NamedParamsNotSupportedException(String message) {
        super(message);
    }
}
