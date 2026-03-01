package simple.orm.mapping;

/**
 * No accessor (getter, setter, field or constructor) found.
 */
public class NoAccessorFoundException extends RuntimeException {

    public NoAccessorFoundException(String message) {
        super(message);
    }

}
