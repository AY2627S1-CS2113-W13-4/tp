package mishmash.testutil;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

/**
 * Checks the order of CLI responses so a later command cannot satisfy an earlier command's expectations.
 */
public final class CliAssertions {
    private static final Pattern ANSI_STYLE = Pattern.compile("\\u001B\\[[0-9;]*m");

    private CliAssertions() {
    }

    /**
     * Finds each expected fragment strictly after the preceding fragment, including repeated responses.
     *
     * @param output The complete captured session output.
     * @param expectedFragments The expected response fragments in command order.
     */
    public static void assertInOrder(String output, String... expectedFragments) {
        int nextPosition = 0;
        for (String fragment : expectedFragments) {
            int position = output.indexOf(fragment, nextPosition);
            assertTrue(position >= 0, "Missing or out-of-order response: " + fragment + "\n" + output);
            nextPosition = position + fragment.length();
        }
    }

    /**
     * Returns console text without ANSI styling while preserving every visible character and line break.
     *
     * @param output The captured console text.
     * @return The same text with ANSI Select Graphic Rendition sequences removed.
     */
    public static String stripAnsi(String output) {
        return ANSI_STYLE.matcher(output).replaceAll("");
    }
}
