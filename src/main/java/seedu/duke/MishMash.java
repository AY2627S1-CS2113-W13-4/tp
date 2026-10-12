package seedu.duke;

import java.util.Scanner;
import java.util.Optional;

/**
 * Runs the MishMash command-line application.
 */
public class MishMash {
    /**
     * Reads and executes commands until the user exits or input ends.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        RecipeBook recipeBook = new RecipeBook();
        Parser parser = new Parser();

        try (Scanner scanner = new Scanner(System.in)) {
            MishMash.greet(scanner);

            System.out.println("Welcome to MishMash!");
            System.out.println("Add a recipe with 'add-recipe', or type 'bye' to exit.");

            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.equals("bye")) {
                    System.out.println("Goodbye!");
                    break;
                }

                try {
                    Command command = parser.parseCommand(input);
                    Optional<String> question = command.confirmationPrompt(recipeBook);
                    if (question.isPresent() && !userConfirms(scanner, question.get())) {
                        System.out.println("Cancelled. Nothing was changed.");
                        continue;
                    }
                    System.out.println(command.execute(recipeBook));
                } catch (MishMashException e) {
                    // The parser already puts the relevant usage in the message, and errors raised
                    // while executing (e.g. a missing recipe) need no format reminder.
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Please enter another command, or type 'bye' to exit.");
                }
            }
        }
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
        String banner = """
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
        System.out.println(banner);
        System.out.println("What is your name?");
        if (scanner.hasNextLine()) {
            System.out.println("Hello " + scanner.nextLine());
        }
    }
}


