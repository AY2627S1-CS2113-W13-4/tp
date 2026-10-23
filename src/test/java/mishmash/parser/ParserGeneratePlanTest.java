package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mishmash.command.AddRecipeCommand;
import mishmash.command.Command;
import mishmash.command.GeneratePlanCommand;
import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.RecipeBook;
import mishmash.testutil.TestRecipes;

/**
 * Checks that the shared parser recognises generate-plan and reports its errors like other commands.
 */
class ParserGeneratePlanTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final Parser parser = new Parser(new MealPlanGenerator(new Random(1)), activePlan,
            () -> Optional.empty());

    private static RecipeBook threeRecipes() {
        RecipeBook recipeBook = new RecipeBook();
        recipeBook.add(TestRecipes.recipe("A", 500, 20, 50, 10));
        recipeBook.add(TestRecipes.recipe("B", 600, 30, 60, 15));
        recipeBook.add(TestRecipes.recipe("C", 700, 40, 70, 20));
        return recipeBook;
    }

    @Test
    void parseCommand_generatePlan_returnsGeneratePlanCommand() throws MishMashException {
        assertInstanceOf(GeneratePlanCommand.class, parser.parseCommand("generate-plan d/3 cal/1800"));
    }

    @Test
    void parseCommand_generatePlan_doesNotGenerateUntilExecuted() throws MishMashException {
        Command command = parser.parseCommand("generate-plan d/2");

        assertTrue(activePlan.get().isEmpty());

        command.execute(threeRecipes());

        assertTrue(activePlan.get().isPresent());
    }

    @Test
    void parseCommand_generateThenViewPlan_viewShowsGeneratedPlan() throws MishMashException {
        RecipeBook recipeBook = threeRecipes();
        Command viewBeforeGenerating = parser.parseCommand("view-plan");
        assertTrue(viewBeforeGenerating.execute(recipeBook).startsWith("No active meal plan."));

        parser.parseCommand("generate-plan d/2").execute(recipeBook);

        String display = parser.parseCommand("view-plan").execute(recipeBook);
        assertTrue(display.contains("Day 1") && display.contains("Day 2"), display);
        assertEquals(display, viewBeforeGenerating.execute(recipeBook));
        assertTrue(parser.parseCommand("view-plan d/2").execute(recipeBook).contains("Day 2"));
        assertThrows(MishMashException.class, () -> parser.parseCommand("view-plan d/3").execute(recipeBook));
    }

    @Test
    void parseCommand_generatePlanInvalidArguments_alwaysIncludesGeneratePlanUsage() {
        for (String arguments : new String[] {"", "cal/2000", "d/0", "d/8", "d/abc", "d/2 cal/0", "d/2 x/1",
                "d/2 d/3", "d/2 cal/"}) {
            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand("generate-plan " + arguments), arguments);
            assertTrue(exception.getMessage().contains(Parser.GENERATE_PLAN_USAGE), arguments);
            assertTrue(Parser.GENERATE_PLAN_USAGE.startsWith("Format: "));
        }
    }

    @Test
    void commandUsage_listsEveryCommand() {
        for (String usage : new String[] {Parser.ADD_RECIPE_USAGE, Parser.GENERATE_PLAN_USAGE,
                Parser.VIEW_PLAN_USAGE, Parser.VIEW_LIST_USAGE}) {
            assertTrue(Parser.COMMAND_USAGE.contains(usage), usage);
        }
    }

    @Test
    void parseCommand_noArgumentConstructor_sharesPlanBetweenGenerateAndView() throws MishMashException {
        Parser plain = new Parser();
        RecipeBook recipeBook = threeRecipes();

        assertInstanceOf(GeneratePlanCommand.class, plain.parseCommand("generate-plan d/1"));
        assertInstanceOf(AddRecipeCommand.class,
                plain.parseCommand("add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g"));
        plain.parseCommand("generate-plan d/1").execute(recipeBook);

        assertTrue(plain.parseCommand("view-plan").execute(recipeBook).contains("Day 1"));
    }

    @Test
    void constructor_nullArguments_throwNullPointerException() {
        MealPlanGenerator generator = new MealPlanGenerator(new Random());
        assertThrows(NullPointerException.class, () -> new Parser(null, activePlan, () -> Optional.empty()));
        assertThrows(NullPointerException.class, () -> new Parser(generator, null, () -> Optional.empty()));
        assertThrows(NullPointerException.class, () -> new Parser(generator, activePlan, null));
    }
}
