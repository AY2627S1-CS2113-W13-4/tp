package mishmash;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mishmash.command.Command;
import mishmash.exception.MishMashException;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;

/**
 * Runs the MishMash command-line application.
 */
public class MishMash {
    /**
     * Records application lifecycle events and command execution diagnostics.
     */
    private static final Logger LOGGER = Logger.getLogger(MishMash.class.getName());

    /**
     * Shares the file handler and logging level with loggers in all MishMash subpackages.
     */
    private static final Logger APP_LOGGER = Logger.getLogger("mishmash");

    /**
     * Limits the approximate size of each rotating log file in bytes.
     */
    private static final int LOG_FILE_SIZE_LIMIT_BYTES = 1_000_000;

    /**
     * Limits the number of rotating log files retained across sessions.
     */
    private static final int LOG_FILE_COUNT = 3;

    /** Separates command output from the next input prompt. */
    private static final String DIVIDER = "------------------------------------------------------------";

    private static final String WELCOME_BANNER = """
              __  __ _     _     __  __           _
             |  \\/  (_)___| |__ |  \\/  | __ _ ___| |__
             | |\\/| | / __| '_ \\| |\\/| |/ _` / __| '_ \\
             | |  | | \\__ \\ | | | |  | | (_| \\__ \\ | | |
             |_|  |_|_|___/_| |_|_|  |_|\\__,_|___/_| |_|

                          .-~~~~~~~~~-.
                       .-' . .. .. . '-.
                      / . .. .. .. .. . \\
                     /___________________\\
                     \\                   /
                      \\                 /
                       \\_______________/
                            \\_____/
            """;

    /** Borrowed console input; the caller remains responsible for closing it. */
    private final Scanner scanner;
    /** Destination for user-facing output, independent of diagnostic logging. */
    private final PrintStream output;
    private final Parser parser;
    private final RecipeBook recipeBook;

    /**
     * Creates an application session without changing global console streams or logging configuration.
     * The caller owns the input and output, so running a session does not close either resource.
     *
     * @param scanner The session's input and confirmation answers.
     * @param output The destination for prompts, results, and recoverable errors.
     * @param parser The parser connected to the session's shared application state.
     * @param recipeBook The recipe book passed to commands in this session.
     * @throws NullPointerException If any dependency is null.
     */
    public MishMash(Scanner scanner, PrintStream output, Parser parser, RecipeBook recipeBook) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.output = Objects.requireNonNull(output, "output");
        this.parser = Objects.requireNonNull(parser, "parser");
        this.recipeBook = Objects.requireNonNull(recipeBook, "recipeBook");
    }

    /**
     * Reads and executes commands until the user exits or input ends.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        Level originalLogLevel = APP_LOGGER.getLevel();
        boolean wasUsingParentHandlers = APP_LOGGER.getUseParentHandlers();
        FileHandler logFile = configureLogging();
        try (Scanner scanner = new Scanner(System.in)) {
            new MishMash(scanner, System.out, new Parser(), new RecipeBook()).run();
        } finally {
            if (logFile != null) {
                APP_LOGGER.removeHandler(logFile);
                logFile.close();
                APP_LOGGER.setLevel(originalLogLevel);
                APP_LOGGER.setUseParentHandlers(wasUsingParentHandlers);
            }
        }
    }

    /**
     * Runs the greeting and command loop using the supplied dependencies, leaving setup to the caller.
     * Logs unexpected failures once before rethrowing the same exception or assertion error.
     */
    public void run() {
        try {
            LOGGER.info("MishMash started");
            greet(scanner, output);
            output.println("Welcome to MishMash!");
            output.println("Type 'help' to view all commands and what they do, or 'bye' to exit.");
            readCommands();
        } catch (RuntimeException | AssertionError e) {
            // Log unexpected failures once at the application boundary before ending the session.
            LOGGER.log(Level.SEVERE, "Unexpected application failure", e);
            throw e;
        } finally {
            LOGGER.info("MishMash stopped");
        }
    }

    /**
     * Writes package diagnostics to rotating files without mixing them into console interaction.
     * Logging failure leaves the application usable with its existing console logging configuration.
     *
     * @return The file handler to close at shutdown, or null if its file could not be opened.
     */
    private static FileHandler configureLogging() {
        try {
            // Append across sessions and rotate files when the size limit is reached.
            FileHandler logFile = new FileHandler("mishmash.%g.log",
                    LOG_FILE_SIZE_LIMIT_BYTES, LOG_FILE_COUNT, true);
            logFile.setFormatter(new SimpleFormatter());
            logFile.setLevel(Level.FINE);
            APP_LOGGER.addHandler(logFile);
            APP_LOGGER.setLevel(Level.FINE);
            APP_LOGGER.setUseParentHandlers(false);
            return logFile;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not open the log file; continuing without file logging", e);
            return null;
        }
    }

    /**
     * Reads commands until the user exits or console input ends.
     */
    private void readCommands() {
        displayCommandPrompt();
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.equals("bye")) {
                LOGGER.fine("Exit requested with bye");
                output.println("Goodbye!");
                return;
            }
            executeCommand(input);
            displayCommandPrompt();
        }
        LOGGER.fine("Console input ended");
    }

    /**
     * Executes one command and displays recoverable errors so the session can continue.
     * A command that cannot be undone, such as a delete, is only run after the user agrees.
     *
     * @param input The trimmed command input.
     */
    private void executeCommand(String input) {
        try {
            Command command = Objects.requireNonNull(parser.parseCommand(input), "command");
            Optional<String> question = Objects.requireNonNull(command.confirmationPrompt(recipeBook),
                    "confirmationPrompt");
            // A command must provide a readable question before requesting confirmation.
            assert question.isEmpty() || !question.get().isBlank() : "Confirmation prompts must not be blank.";
            if (question.isPresent() && !userConfirms(question.get())) {
                LOGGER.fine(() -> "Cancelled " + command.getClass().getSimpleName());
                output.println("Cancelled. Nothing was changed.");
                return;
            }
            LOGGER.fine(() -> "Executing " + command.getClass().getSimpleName());
            String feedback = Objects.requireNonNull(command.execute(recipeBook), "command feedback");
            output.println(feedback);
            LOGGER.fine(() -> "Completed " + command.getClass().getSimpleName());
        } catch (MishMashException e) {
            LOGGER.log(Level.FINE, "Command rejected", e);
            displayError(e);
        }
    }

    /**
     * Displays an error, adding general command help if no specific usage was supplied.
     *
     * @param exception The recoverable command error.
     */
    private void displayError(MishMashException exception) {
        output.println("Error: " + exception.getExplanation());
        output.println(exception.getUsage().orElse(Parser.COMMAND_USAGE));
    }

    /**
     * Displays a consistent prompt after every command, including errors and cancellations.
     */
    private void displayCommandPrompt() {
        output.println(DIVIDER);
        output.println("Enter a command, or type 'help' for all commands or 'bye' to exit.");
    }

    /**
     * Asks a yes/no question and reads the answer from the next input line.
     * Only "y" or "yes" (in any letter case) counts as agreement; anything else, including
     * the end of input, counts as "no" so that a destructive action never runs by accident.
     *
     * @param question The question to display.
     * @return True if the user agreed.
     */
    private boolean userConfirms(String question) {
        output.println(question);
        if (!scanner.hasNextLine()) {
            return false;
        }
        String answer = scanner.nextLine().trim();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    /**
     * Displays the banner and reads the user's name.
     *
     * @param scanner Scanner used to read user input.
     * @throws NullPointerException If the scanner is null.
     */
    public static void greet(Scanner scanner) {
        greet(scanner, System.out);
    }

    /**
     * Displays the greeting on the supplied output without changing the global console.
     */
    private static void greet(Scanner scanner, PrintStream output) {
        Objects.requireNonNull(scanner, "scanner");
        output.println(WELCOME_BANNER);
        output.println("What is your name?");
        if (scanner.hasNextLine()) {
            output.println("Hello " + scanner.nextLine());
        }
    }
}

