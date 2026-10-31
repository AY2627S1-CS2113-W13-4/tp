package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.FoodInfo;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;
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

    @Test
    void viewDiet_activePlanAndGroceries_preservesAllPlanningState() throws MishMashException {
        ActiveMealPlan active = new ActiveMealPlan();
        Parser statefulParser = new Parser(new MealPlanGenerator(new Random(42)), active);
        for (String name : List.of("Rice", "Beans", "Tofu")) {
            book.add(new Recipe(name, 600, 10, 20, 5,
                    List.of(new GroceryItem(name, BigDecimal.ONE, "g")), FoodInfo.parseField("none")));
        }
        statefulParser.parseCommand("set-diet diet/vegan").execute(book);
        statefulParser.parseCommand("generate-plan d/1").execute(book);
        statefulParser.parseCommand("generate-list").execute(book);
        MealPlan plan = active.get().orElseThrow();
        GeneratePlanRequest request = active.getRequest().orElseThrow();
        List<GroceryItem> groceries = active.getGroceryList().orElseThrow();
        List<Recipe> recipes = List.copyOf(book.getRecipes());

        Command view = statefulParser.parseCommand("view-diet");
        assertTrue(view.confirmationPrompt(book).isEmpty());
        assertTrue(view.execute(book).contains("Diet: vegan"));
        assertEquals(view.execute(book), view.execute(book));

        assertSame(plan, active.get().orElseThrow());
        assertSame(request, active.getRequest().orElseThrow());
        assertSame(groceries, active.getGroceryList().orElseThrow());
        assertEquals(recipes, book.getRecipes());
    }
}
