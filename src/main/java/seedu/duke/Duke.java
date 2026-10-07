package seedu.duke;

import java.util.Scanner;

/**
 * Runs the MishMash command-line application.
 */
public class Duke {
    /**
     * Reads and executes commands until the user exits or input ends.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        RecipeBook recipeBook = new RecipeBook();
        Parser parser = new Parser();

        System.out.println("Welcome to MishMash!");
        System.out.println("Add a recipe with 'add-recipe', or type 'bye' to exit.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.equals("bye")) {
                    System.out.println("Goodbye!");
                    break;
                }

                try {
                    Command command = parser.parseCommand(input);
                    System.out.println(command.execute(recipeBook));
                } catch (MishMashException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}

