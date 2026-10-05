package seedu.duke;

import java.util.Scanner;

/**
 * Displays the welcome banner and greets the user by name.
 */
public class MishMash {
    /**
     * Main entry-point for the java.duke.Duke application.
     */
    public static void main(String[] args) {
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

        Scanner in = new Scanner(System.in);
        System.out.println("Hello " + in.nextLine());
    }
}
