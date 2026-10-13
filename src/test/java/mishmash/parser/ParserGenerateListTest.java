package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mishmash.command.Command;
import mishmash.command.GenerateListCommand;
import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.RecipeBook;
import mishmash.testutil.TestRecipes;

/**
 * Checks grocery syntax and the shared generation/viewing flow through the parser.
 */
class ParserGenerateListTest {
    @Test
    void parseCommand_whitespace_createsCommandWithoutChangingState() throws MishMashException {
        ActiveMealPlan activePlan = new ActiveMealPlan();
        Parser parser = new Parser(new MealPlanGenerator(new Random(1)), activePlan);

        assertInstanceOf(GenerateListCommand.class, parser.parseCommand(" \tgenerate-list \t "));
        assertTrue(activePlan.get().isEmpty());
        assertTrue(activePlan.getGroceryList().isEmpty());
        assertTrue(Parser.COMMAND_USAGE.contains(Parser.GENERATE_LIST_USAGE));
    }

    @Test
    void parseCommand_arguments_rejectsWithGroceryUsage() {
        Parser parser = new Parser();
        for (String arguments : List.of("extra", "d/1", "r/1", "i/Rice:1g")) {
            MishMashException error = assertThrows(MishMashException.class,
                    () -> parser.parseCommand("generate-list " + arguments));
            assertEquals("generate-list does not accept arguments." + System.lineSeparator()
                    + Parser.GENERATE_LIST_USAGE, error.getMessage());
        }
    }

    @Test
    void parseCommand_defaultParser_connectsGenerationAndExistingViewer() throws MishMashException {
        verifyIntegratedFlow(new Parser());
    }

    @Test
    void parseCommand_sharedPlan_failedGenerationPreservesGroceries() throws MishMashException {
        ActiveMealPlan activePlan = new ActiveMealPlan();
        Parser parser = new Parser(new MealPlanGenerator(new Random(1)), activePlan);
        verifyIntegratedFlow(parser);
    }

    @Test
    void constructor_nullDependencies_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Parser(null, new ActiveMealPlan()));
        assertThrows(NullPointerException.class, () -> new Parser(new MealPlanGenerator(new Random()), null));
    }

    /**
     * Exercises deferred execution, failure preservation, invalidation, and regeneration.
     */
    private void verifyIntegratedFlow(Parser parser) throws MishMashException {
        RecipeBook book = new RecipeBook();
        for (String name : List.of("A", "B", "C")) {
            book.add(TestRecipes.recipe(name, 500, 20, 50, 10));
        }
        Command generate = parser.parseCommand("generate-list");
        Command view = parser.parseCommand("view-list");
        assertTrue(view.execute(book).startsWith("No grocery list generated."));

        parser.parseCommand("generate-plan d/2").execute(book);
        generate.execute(book);
        String original = view.execute(book);
        assertTrue(original.contains("[ ] Ingredient: 6g"), original);

        assertThrows(MishMashException.class, () -> parser.parseCommand("generate-plan d/2 cal/1").execute(book));
        assertEquals(original, view.execute(book));
        assertThrows(MishMashException.class, () -> parser.parseCommand("generate-list extra"));
        assertEquals(original, view.execute(book));

        parser.parseCommand("generate-plan d/1").execute(book);
        assertTrue(view.execute(book).startsWith("No grocery list generated."));
        generate.execute(book);
        assertTrue(view.execute(book).contains("[ ] Ingredient: 3g"));
    }
}
