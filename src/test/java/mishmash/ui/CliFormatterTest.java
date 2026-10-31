package mishmash.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import mishmash.command.Command;
import mishmash.command.HelpCommand;
import mishmash.model.RecipeBook;

/**
 * Checks exact ANSI boundaries and safe-default prompts in the console formatter.
 */
class CliFormatterTest {
    private static final String ESCAPE = "\u001B[";
    private static final String RESET = ESCAPE + "0m";

    @Test
    void banner_stylesEveryGraphicSectionAndResetsAfterTheBowl() {
        String banner = CliFormatter.banner();

        assertTrue(banner.startsWith(ESCAPE + "2m            (       )"));
        assertTrue(banner.contains(ESCAPE + "1;93m      /          M I S H M A S H"));
        assertTrue(banner.contains(ESCAPE + "1;96m     |       PLAN  |  PREP  |  ENJOY"));
        assertTrue(banner.endsWith("               \\_____________/" + RESET));
    }

    @Test
    void welcome_listsCommonCommandsInAnAlignedCard() {
        String welcome = CliFormatter.welcome();

        assertTrue(welcome.contains("QUICK COMMANDS"));
        assertTrue(welcome.contains("help"));
        assertTrue(welcome.contains("list-recipes"));
        assertTrue(welcome.contains("generate-plan d/3"));
        assertTrue(welcome.contains("view-plan"));
        assertEquals(4, welcome.lines().filter(line -> line.contains("|")).count());
    }

    @Test
    void feedback_helpStylesTheCompleteManualWithoutChangingItsContents() {
        String manual = "Heading" + System.lineSeparator() + "Format: help";

        assertEquals(ESCAPE + "1;93m" + manual + RESET,
                CliFormatter.feedback(new HelpCommand(), manual));
    }

    @Test
    void feedback_readOnlyCommandKeepsMultilineTextContiguous() {
        String feedback = "First line" + System.lineSeparator() + "Second line";

        assertEquals(ESCAPE + "1;96m" + feedback + RESET,
                CliFormatter.feedback(commandReturning(feedback), feedback));
    }

    @Test
    void feedback_emptyStateUsesNoteLabelAndLeavesMessagePlain() {
        String feedback = "No meals recorded yet.";

        assertEquals(ESCAPE + "1;93m[NOTE]" + RESET + " " + feedback,
                CliFormatter.feedback(commandReturning(feedback), feedback));
    }

    @Test
    void errorAndUsage_resetBeforeUserContentAndAfterDimmedSyntax() {
        assertEquals(ESCAPE + "1;91m[ERROR]" + RESET + " Error: Bad command.",
                CliFormatter.error("Bad command."));
        assertEquals(ESCAPE + "1;96m[TRY]" + RESET + " "
                        + ESCAPE + "2mFormat: help" + RESET,
                CliFormatter.usage("Format: help"));
    }

    @Test
    void confirmation_replacesLegacyMarkerWithSafeDefaultPrompt() {
        assertEquals(ESCAPE + "1;91m[CONFIRM]" + RESET + " Delete this? "
                        + ESCAPE + "93m[y/N]:" + RESET,
                CliFormatter.confirmation("Delete this? (y/n)"));
    }

    /** Returns a minimal read-only command for presentation tests. */
    private Command commandReturning(String feedback) {
        return new Command() {
            @Override
            public String execute(RecipeBook recipeBook) {
                return feedback;
            }
        };
    }
}
