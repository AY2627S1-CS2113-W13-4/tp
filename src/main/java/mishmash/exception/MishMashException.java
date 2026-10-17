package mishmash.exception;

/**
 * Reports an input error that can be displayed to the user.
 */
public class MishMashException extends Exception {
    /**
     * Creates an exception with a descriptive input error.
     *
     * @param message The explanation to display.
     */
    public MishMashException(String message) {
        super(message);
    }

    /**
     * Creates an input error while retaining its original cause for diagnostics.
     *
     * @param message The explanation to display.
     * @param cause The original validation failure.
     */
    public MishMashException(String message, Throwable cause) {
        super(message, cause);
    }
}

