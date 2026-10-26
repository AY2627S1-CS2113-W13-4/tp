package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;

/**
 * Verifies read-only dietary viewing and its command-specific input validation.
 */
class ViewDietCommandTest {
    private final Parser parser = new Parser();
    private final RecipeBook book = new RecipeBook();

    @Test
    void viewDiet_defaultProfile_displaysEveryEmptyField() throws MishMashException {
        assertEquals(String.join(System.lineSeparator(), "Diet: none",
                "Avoided food categories: none", "Excluded ingredients: none"),
                parser.parseCommand("view-diet").execute(book));
    }

    @Test
    void viewDiet_pendingCommand_readsLatestPreferencesAtExecution() throws MishMashException {
        Command pending = parser.parseCommand("view-diet");
        parser.parseCommand("set-diet diet/vegan avoid/soy,peanut exclude/Salt,Mushroom").execute(book);
        String expected = String.join(System.lineSeparator(), "Diet: vegan",
                "Avoided food categories: peanut, soy", "Excluded ingredients: mushroom, salt");
        assertTrue(pending.confirmationPrompt(book).isEmpty());
        assertEquals(expected, pending.execute(book));
        assertEquals(expected, pending.execute(book));
    }

    @Test
    void viewDiet_whitespaceOnlyArguments_acceptsInput() throws MishMashException {
        assertEquals(parser.parseCommand("view-diet").execute(book),
                parser.parseCommand("view-diet   ").execute(book));
    }

    @Test
    void viewDiet_unexpectedArguments_reportsUsageAndPreservesPreferences() throws MishMashException {
        parser.parseCommand("set-diet diet/vegan").execute(book);
        String before = parser.parseCommand("view-diet").execute(book);
        MishMashException error = assertThrows(MishMashException.class,
                () -> parser.parseCommand("view-diet diet/none"));
        assertTrue(error.getMessage().contains("view-diet does not accept arguments."));
        assertTrue(error.getMessage().contains("Format: view-diet"));
        assertEquals(before, parser.parseCommand("view-diet").execute(book));
    }
}
