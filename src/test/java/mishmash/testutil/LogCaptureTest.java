package mishmash.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

/**
 * Checks that temporary log capture restores existing logger configuration even when a test fails.
 */
class LogCaptureTest {
    @Test
    void close_loggingCapture_restoresSettingsAndExistingHandlersAfterFailure() {
        Logger logger = Logger.getLogger(LogCaptureTest.class.getName());
        Level originalLevel = logger.getLevel();
        boolean wereParentHandlersEnabled = logger.getUseParentHandlers();
        List<Handler> originalHandlers = Arrays.asList(logger.getHandlers());
        IllegalStateException failure = new IllegalStateException("Test failure");

        assertSame(failure, assertThrows(IllegalStateException.class, () -> {
            try (LogCapture logs = new LogCapture(LogCaptureTest.class)) {
                logger.fine("Captured before failure");
                assertEquals(1, logs.getRecords().size());
                throw failure;
            }
        }));

        assertEquals(originalLevel, logger.getLevel());
        assertEquals(wereParentHandlersEnabled, logger.getUseParentHandlers());
        assertEquals(originalHandlers, Arrays.asList(logger.getHandlers()));
    }
}
