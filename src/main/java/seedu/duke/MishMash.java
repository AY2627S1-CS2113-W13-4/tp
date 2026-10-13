package seedu.duke;

import java.util.Scanner;
import java.util.Optional;

/**
 * Runs the MishMash command-line application.
 */
public class MishMash {
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
        RecipeBook recipeBook = new RecipeBook();
        Parser parser = new Parser();

        try (Scanner scanner = new Scanner(System.in)) {
            greet(scanner);
            System.out.println("Welcome to MishMash!");
            System.out.println("Use 'add-recipe', 'view-plan', or 'view-list', or type 'bye' to exit.");
            readCommands(scanner, parser, recipeBook);
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
                System.out.println("Goodbye!");
                return;
            }
            executeCommand(input, scanner, parser, recipeBook);
        }
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
                System.out.println("Cancelled. Nothing was changed.");
                return;
            }
            System.out.println(command.execute(recipeBook));
        } catch (MishMashException e) {
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
