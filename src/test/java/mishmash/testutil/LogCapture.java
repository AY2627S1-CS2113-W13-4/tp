package mishmash.testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Captures one class logger in memory and restores its settings when the test's resource scope ends.
 * Keeps a strong reference to the logger for the entire capture to prevent logger configuration loss.
 */
public final class LogCapture extends Handler implements AutoCloseable {
    private final Logger logger;
    private final Level originalLevel;
    private final boolean wereParentHandlersEnabled;
    private final List<LogRecord> records = new ArrayList<>();

    /**
     * Attaches a temporary handler to the supplied class's logger without removing existing handlers.
     *
     * @param source The class whose diagnostic records are captured.
     */
    public LogCapture(Class<?> source) {
        logger = Logger.getLogger(source.getName());
        originalLevel = logger.getLevel();
        wereParentHandlersEnabled = logger.getUseParentHandlers();
        setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            records.add(record);
        }
    }

    /**
     * Returns an unmodifiable snapshot of captured records in publication order.
     */
    public List<LogRecord> getRecords() {
        return List.copyOf(records);
    }

    /**
     * Returns joined messages without depending on the application's log formatting.
     */
    public String getMessages() {
        return String.join("\n", records.stream().map(LogRecord::getMessage).toList());
    }

    @Override
    public void flush() {
        // All captured records are already in memory.
    }

    @Override
    public void close() {
        logger.removeHandler(this);
        logger.setLevel(originalLevel);
        logger.setUseParentHandlers(wereParentHandlersEnabled);
    }
}
