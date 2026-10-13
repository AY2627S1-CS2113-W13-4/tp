package mishmash;

import java.io.IOException;
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

    /**
     * Reads and executes commands until the user exits or input ends.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        Level originalLogLevel = APP_LOGGER.getLevel();
        boolean wasUsingParentHandlers = APP_LOGGER.getUseParentHandlers();
        FileHandler logFile = configureLogging();
        RecipeBook recipeBook = new RecipeBook();
        Parser parser = new Parser();

        try (Scanner scanner = new Scanner(System.in)) {
            LOGGER.info("MishMash started");
            greet(scanner);
            System.out.println("Welcome to MishMash!");
            System.out.println("Use 'add-recipe', 'generate-plan', 'view-plan', 'generate-list', or 'view-list',"
                    + " or type 'bye' to exit.");
            readCommands(scanner, parser, recipeBook);
        } finally {
            LOGGER.info("MishMash stopped");
            if (logFile != null) {
                APP_LOGGER.removeHandler(logFile);
                logFile.close();
                APP_LOGGER.setLevel(originalLogLevel);
                APP_LOGGER.setUseParentHandlers(wasUsingParentHandlers);
            }
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
     *
     * @param scanner The console input.
     * @param parser The parser connected to application data.
     * @param recipeBook The recipe book passed to each command.
     */
    private static void readCommands(Scanner scanner, Parser parser, RecipeBook recipeBook) {
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.equals("bye")) {
                LOGGER.fine("Exit requested with bye");
                System.out.println("Goodbye!");
                return;
            }
            executeCommand(input, scanner, parser, recipeBook);
        }
        LOGGER.fine("Console input ended");
    }

    /**
     * Executes one command and displays recoverable errors so the session can continue.
     * A command that cannot be undone, such as a delete, is only run after the user agrees.
     *
     * @param input The trimmed command input.
     * @param scanner The console input, used to read the answer to a confirmation question.
     * @param parser The parser connected to application data.
     * @param recipeBook The recipe book passed to the command.
     */
    private static void executeCommand(String input, Scanner scanner, Parser parser, RecipeBook recipeBook) {
        try {
            Command command = parser.parseCommand(input);
            Optional<String> question = command.confirmationPrompt(recipeBook);
            if (question.isPresent() && !userConfirms(scanner, question.get())) {
                LOGGER.fine(() -> "Cancelled " + command.getClass().getSimpleName());
                System.out.println("Cancelled. Nothing was changed.");
                return;
            }
            LOGGER.fine(() -> "Executing " + command.getClass().getSimpleName());
            System.out.println(command.execute(recipeBook));
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
    private static void displayError(MishMashException exception) {
        System.out.println("Error: " + exception.getMessage());
        if (!exception.getMessage().contains("Format: ")) {
            System.out.println(Parser.COMMAND_USAGE);
        }
        System.out.println("Please enter another command, or type 'bye' to exit.");
    }

    /**
     * Asks a yes/no question and reads the answer from the next input line.
     * Only "y" or "yes" (in any letter case) counts as agreement; anything else, including
     * the end of input, counts as "no" so that a destructive action never runs by accident.
     *
     * @param scanner Scanner used to read user input.
     * @param question The question to display.
     * @return True if the user agreed.
     */
    private static boolean userConfirms(Scanner scanner, String question) {
        System.out.println(question);
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
     */
    public static void greet(Scanner scanner) {
        System.out.println(WELCOME_BANNER);
        System.out.println("What is your name?");
        if (scanner.hasNextLine()) {
            System.out.println("Hello " + scanner.nextLine());
        }
    }
}
