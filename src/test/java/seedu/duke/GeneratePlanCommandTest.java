package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Checks that generate-plan stores the plan, replaces an older one, shows it like view-plan,
 * and keeps the previous plan when generation fails.
 */
class GeneratePlanCommandTest {
    private final MealPlanGenerator generator = new MealPlanGenerator(new Random(1));
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final RecipeBook recipeBook = bookOf(
            TestRecipes.recipe("Oatmeal", 500, 20, 80, 10),
            TestRecipes.recipe("Chicken Rice", 600, 40, 70, 15),
            TestRecipes.recipe("Salmon Bowl", 700, 45, 60, 25),
            TestRecipes.recipe("Apple", 100, 1, 25, 0));

    private static RecipeBook bookOf(Recipe... recipes) {
        RecipeBook book = new RecipeBook();
        for (Recipe recipe : recipes) {
            book.add(recipe);
        }
        return book;
    }

    private GeneratePlanCommand command(String args) {
        return new GeneratePlanCommand(new GeneratePlanParser().parse(args), generator, activePlan);
    }

    @Test
    void activePlan_initiallyEmpty() {
        assertTrue(activePlan.get().isEmpty());
    }

    @Test
    void execute_success_setsActivePlan() throws MishMashException {
        String message = command("d/3").execute(recipeBook);

        assertEquals(3, activePlan.get().orElseThrow().getNumberOfDays());
        assertEquals("Meal plan successfully generated for 3 day(s)!" + System.lineSeparator()
                + "Use 'view-plan' to inspect your planned meals.", message);
    }

    @Test
    void execute_secondPlan_replacesFirst() throws MishMashException {
        command("d/2").execute(recipeBook);
        MealPlan first = activePlan.get().orElseThrow();

        command("d/5").execute(recipeBook);
        MealPlan second = activePlan.get().orElseThrow();

        assertNotSame(first, second);
        assertEquals(5, second.getNumberOfDays());
    }

    @Test
    void execute_failure_keepsPreviousPlan() throws MishMashException {
        command("d/2").execute(recipeBook);
        MealPlan previous = activePlan.get().orElseThrow();

        assertThrows(MishMashException.class, () -> command("d/3 cal/99999").execute(recipeBook));
        assertThrows(MishMashException.class, () -> command("d/3").execute(new RecipeBook()));

        assertSame(previous, activePlan.get().orElseThrow());
    }

    @Test
    void execute_emptyRecipeBook_reportsHowToFix() {
        MishMashException e = assertThrows(MishMashException.class,
                () -> command("d/3").execute(new RecipeBook()));

        assertTrue(e.getMessage().contains("recipe book is empty"));
    }

    @Test
    void execute_success_messageConfirmsAndPointsToViewPlan() throws MishMashException {
        String message = command("d/2").execute(recipeBook);

        assertEquals("Meal plan successfully generated for 2 day(s)!" + System.lineSeparator()
                + "Use 'view-plan' to inspect your planned meals.", message);
    }

    @Test
    void execute_laterRecipeAdded_isConsideredNextTime() throws MishMashException {
        RecipeBook small = bookOf(
                TestRecipes.recipe("A", 100, 1, 1, 1),
                TestRecipes.recipe("B", 100, 1, 1, 1));
        assertThrows(MishMashException.class, () -> command("d/1").execute(small));

        small.add(TestRecipes.recipe("C", 100, 1, 1, 1));
        command("d/1").execute(small);

        assertEquals(1, activePlan.get().orElseThrow().getNumberOfDays());
    }

    @Test
    void constructor_nullArguments_throwNullPointerException() {
        GeneratePlanRequest request = new GeneratePlanParser().parse("d/1");

        assertThrows(NullPointerException.class, () -> new GeneratePlanCommand(null, generator, activePlan));
        assertThrows(NullPointerException.class, () -> new GeneratePlanCommand(request, null, activePlan));
        assertThrows(NullPointerException.class, () -> new GeneratePlanCommand(request, generator, null));
    }

    @Test
    void execute_nullRecipeBook_throwsNullPointerException() {
        GeneratePlanCommand command = command("d/1");

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    void activePlan_setNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> activePlan.set(null));
    }
}
