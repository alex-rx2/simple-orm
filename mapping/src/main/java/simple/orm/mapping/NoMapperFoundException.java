package simple.orm.mapping;

/**
 * No mapper found.
 */
public class NoMapperFoundException extends RuntimeException {

    public NoMapperFoundException(String message) {
        super(message);
    }

}
