package mishmash.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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
    void constructor_blankUsage_rejectsInvalidMetadata() {
        assertThrows(IllegalArgumentException.class, () -> new MishMashException("Error", " ", null));
    }

    @TestFactory
    Stream<DynamicTest> constructor_missingExplanation_usesFallbackAndPreservesCauseAndUsage() {
        IllegalArgumentException cause = new IllegalArgumentException();
        MishMashException simple = new MishMashException(null);
        return Stream.of(simple, new MishMashException(cause.getMessage(), cause),
                new MishMashException(cause.getMessage(), "Format: generate-plan d/DAYS", cause))
                .map(error -> DynamicTest.dynamicTest(error.getMessage(), () -> {
                    assertEquals("Command could not be completed.", error.getExplanation());
                    if (error.getUsage().isPresent()) {
                        assertEquals("Format: generate-plan d/DAYS", error.getUsage().orElseThrow());
                        assertEquals(error.getExplanation() + System.lineSeparator()
                                + error.getUsage().orElseThrow(), error.getMessage());
                    } else {
                        assertEquals(error.getExplanation(), error.getMessage());
                    }
                    assertSame(error == simple ? null : cause, error.getCause());
                }));
    }
}
