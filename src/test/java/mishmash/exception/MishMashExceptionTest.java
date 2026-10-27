package mishmash.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Checks separate error explanations, optional usage, and preservation of the original failure.
 */
class MishMashExceptionTest {
    @Test
    void constructor_specificUsage_keepsExplanationAndCauseSeparateFromDisplay() {
        IllegalArgumentException cause = new IllegalArgumentException("Invalid day");
        MishMashException error = new MishMashException("Choose a valid day.", "Format: view-plan d/DAY", cause);

        assertEquals("Choose a valid day.", error.getExplanation());
        assertEquals("Format: view-plan d/DAY", error.getUsage().orElseThrow());
        assertEquals("Choose a valid day." + System.lineSeparator() + "Format: view-plan d/DAY", error.getMessage());
        assertSame(cause, error.getCause());
    }

    @Test
    void constructor_legacyOverloads_preservesMessageAndCauseWithoutInventingUsage() {
        Throwable cause = new IllegalArgumentException("Cause");
        MishMashException simple = new MishMashException("Plain explanation");
        MishMashException caused = new MishMashException("Format: is part of the input", cause);

        assertEquals("Plain explanation", simple.getMessage());
        assertTrue(simple.getUsage().isEmpty());
        assertEquals("Format: is part of the input", caused.getExplanation());
        assertTrue(caused.getUsage().isEmpty());
        assertSame(cause, caused.getCause());
    }

    @Test
    void constructor_missingExplanationOrBlankUsage_rejectsInvalidMetadata() {
        assertThrows(NullPointerException.class, () -> new MishMashException(null));
        assertThrows(IllegalArgumentException.class, () -> new MishMashException("Error", " ", null));
    }
}
