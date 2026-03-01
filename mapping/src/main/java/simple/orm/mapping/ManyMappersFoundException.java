package simple.orm.mapping;

/**
 * More than one mapper found.
 */
public class ManyMappersFoundException extends RuntimeException {

    public ManyMappersFoundException(String message) {
        super(message);
    }

}
