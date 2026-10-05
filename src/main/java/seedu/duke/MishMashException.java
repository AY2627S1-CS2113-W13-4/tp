package seedu.duke;

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
}



