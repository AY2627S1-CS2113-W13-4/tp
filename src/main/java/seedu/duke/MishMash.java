package seedu.duke;

import java.util.Scanner;

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
                    System.out.println(command.execute(recipeBook));
                } catch (MishMashException e) {
                    System.out.println("Error: " + e.getMessage());
                    if (!e.getMessage().contains(Parser.ADD_RECIPE_USAGE)) {
                        System.out.println(Parser.ADD_RECIPE_USAGE);
                    }
                    System.out.println("Please enter another command, or type 'bye' to exit.");
                }
            }
        }
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


