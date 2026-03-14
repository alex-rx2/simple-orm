package simple.orm.loader;

/**
 * Runtime wrap of {@link ClassNotFoundException}.
 */
public class RuntimeClassNotFoundException extends RuntimeException {

    public RuntimeClassNotFoundException(ClassNotFoundException cnfException) {
        this(cnfException.getMessage(), cnfException);
    }

    public RuntimeClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
