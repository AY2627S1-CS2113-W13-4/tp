package seedu.duke;

import java.util.Scanner;

/**
 * Displays the welcome banner and greets the user by name.
 */
public class MishMash {
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

