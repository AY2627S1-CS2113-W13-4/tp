package mishmash;

import static mishmash.testutil.CliAssertions.assertInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.command.Command;
import mishmash.exception.MishMashException;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;
import mishmash.testutil.LogCapture;

/**
 * Checks isolated application execution, borrowed resource ownership, structured help, and failure diagnostics.
 */
class MishMashSessionTest {
    @TestFactory
    Stream<DynamicTest> constructor_missingDependency_rejectsBeforeWritingOutput() {
        return Stream.of("scanner", "output", "parser", "recipeBook")
                .map(dependency -> DynamicTest.dynamicTest(dependency, () -> {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    try (Scanner scanner = new Scanner("Tester\nbye\n"); PrintStream output = new PrintStream(bytes)) {
                        NullPointerException error = assertThrows(NullPointerException.class,
                                () -> new MishMash(dependency.equals("scanner") ? null : scanner,
                                        dependency.equals("output") ? null : output,
                                        dependency.equals("parser") ? null : new Parser(),
                                        dependency.equals("recipeBook") ? null : new RecipeBook()));

                        assertEquals(dependency, error.getMessage());
                        assertEquals(0, bytes.size());
                    }
                }));
    }

    @Test
    void run_borrowedStreams_preservesConsoleAndLoggingConfiguration() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        Logger logger = Logger.getLogger("mishmash");
        Level originalLevel = logger.getLevel();
        boolean wereParentHandlersEnabled = logger.getUseParentHandlers();
        List<Handler> originalHandlers = Arrays.asList(logger.getHandlers());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner("Tester\nbye\nRemaining input\n");
                PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            new MishMash(scanner, output, new Parser(), new RecipeBook()).run();

            assertEquals("Remaining input", scanner.nextLine());
            output.println("Output remains open");
            assertInOrder(bytes.toString(StandardCharsets.UTF_8), "Hello Tester", "Goodbye!", "Output remains open");
            assertFalse(output.checkError());
        }
        assertSame(originalInput, System.in);
        assertSame(originalOutput, System.out);
        assertEquals(originalLevel, logger.getLevel());
        assertEquals(wereParentHandlersEnabled, logger.getUseParentHandlers());
        assertEquals(originalHandlers, Arrays.asList(logger.getHandlers()));
    }

    @TestFactory
    Stream<DynamicTest> run_recoverableError_choosesHelpFromMetadataAndContinuesInOrder() {
        return Stream.of(false, true).map(hasSpecificUsage -> DynamicTest.dynamicTest(
                hasSpecificUsage ? "Specific usage without Format marker" : "Format marker inside explanation", () -> {
                    String explanation = hasSpecificUsage ? "Invalid day." : "Unrecognized input: Format: fake";
                    String usage = hasSpecificUsage ? "Use view-plan with an optional day." : null;
                    Parser parser = new Parser() {
                        @Override
                        public Command parseCommand(String input) throws MishMashException {
                            if (input.equals("bad")) {
                                throw new MishMashException(explanation, usage, null);
                            }
                            return super.parseCommand(input);
                        }
                    };

                    String output = runSession("Tester\nbad\nview-plan\nbye\n", parser);

                    assertInOrder(output, "Error: " + explanation, hasSpecificUsage ? usage : Parser.COMMAND_USAGE,
                            "No active meal plan. Use 'generate-plan' first.", "Goodbye!");
                    assertEquals(!hasSpecificUsage, output.contains(Parser.ADD_RECIPE_USAGE));
                }));
    }

    @TestFactory
    Stream<DynamicTest> run_unexpectedFailure_logsOriginalCauseOnceAndRethrows() {
        return Stream.of(new IllegalStateException("Broken dependency"), new AssertionError("Broken invariant"))
                .map(failure -> DynamicTest.dynamicTest(failure.getClass().getSimpleName(), () -> {
                    Parser parser = new Parser() {
                        @Override
                        public Command parseCommand(String input) {
                            if (failure instanceof RuntimeException runtimeFailure) {
                                throw runtimeFailure;
                            }
                            throw (AssertionError) failure;
                        }
                    };
                    try (LogCapture logs = new LogCapture(MishMash.class)) {
                        assertSame(failure, assertThrows(failure.getClass(),
                                () -> runSession("Tester\ncommand\n", parser)));
                        List<LogRecord> failures = logs.getRecords().stream()
                                .filter(record -> record.getLevel() == Level.SEVERE).toList();
                        assertEquals(1, failures.size());
                        assertSame(failure, failures.getFirst().getThrown());
                        assertTrue(logs.getMessages().endsWith("MishMash stopped"));
                    }
                }));
    }

    @TestFactory
    Stream<DynamicTest> run_brokenCommandContract_reportsDependencyWithoutLoggingSuccess() {
        return Stream.of("command", "confirmationPrompt", "command feedback")
                .map(contract -> DynamicTest.dynamicTest(contract, () -> {
                    Command command = brokenCommand(contract);
                    Parser parser = new Parser() {
                        @Override
                        public Command parseCommand(String input) {
                            return command;
                        }
                    };
                    try (LogCapture logs = new LogCapture(MishMash.class)) {
                        NullPointerException error = assertThrows(NullPointerException.class,
                                () -> runSession("Tester\ncommand\n", parser));

                        assertEquals(contract, error.getMessage());
                        assertFalse(logs.getMessages().contains("Completed "));
                        assertEquals(1, logs.getRecords().stream()
                                .filter(record -> record.getLevel() == Level.SEVERE).count());
                    }
                }));
    }

    /**
     * Returns a broken command, or null, to simulate an internal dependency violating its documented contract.
     */
    private Command brokenCommand(String contract) {
        if (contract.equals("command")) {
            return null;
        }
        return new Command() {
            @Override
            public Optional<String> confirmationPrompt(RecipeBook recipeBook) {
                return contract.equals("confirmationPrompt") ? null : Optional.empty();
            }

            @Override
            public String execute(RecipeBook recipeBook) {
                return null;
            }
        };
    }

    @Test
    void run_blankConfirmation_assertsBeforeExecutingAndLogsOriginalFailure() {
        Parser parser = new Parser() {
            @Override
            public Command parseCommand(String input) {
                return new Command() {
                    @Override
                    public Optional<String> confirmationPrompt(RecipeBook recipeBook) {
                        return Optional.of(" ");
                    }

                    @Override
                    public String execute(RecipeBook recipeBook) {
                        throw new AssertionError("Command must not execute.");
                    }
                };
            }
        };
        try (LogCapture logs = new LogCapture(MishMash.class)) {
            AssertionError error = assertThrows(AssertionError.class, () -> runSession("Tester\ncommand\n", parser));

            assertEquals("Confirmation prompts must not be blank.", error.getMessage());
            assertSame(error, logs.getRecords().stream().filter(record -> record.getLevel() == Level.SEVERE)
                    .findFirst().orElseThrow().getThrown());
        }
    }

    /**
     * Runs a session with local streams; the test owns and closes those streams after execution.
     */
    private String runSession(String input, Parser parser) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(input);
                PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            new MishMash(scanner, output, parser, new RecipeBook()).run();
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
