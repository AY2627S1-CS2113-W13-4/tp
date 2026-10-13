package seedu.mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.mishmash.model.ActiveMealPlan;
import seedu.mishmash.model.DayPlan;
import seedu.mishmash.model.GroceryItem;
import seedu.mishmash.model.MealPlan;
import seedu.mishmash.model.Recipe;
import seedu.mishmash.model.RecipeBook;

/**
 * Checks ingredient aggregation from planned meals and safe replacement of stored groceries.
 */
class GenerateListCommandTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final RecipeBook book = new RecipeBook();
    private final GenerateListCommand command = new GenerateListCommand(activePlan);

    @Test
    void execute_noPlan_returnsPromptWithoutStoringGroceries() {
        assertEquals("No active meal plan. Use 'generate-plan' first.", command.execute(book));
        assertTrue(activePlan.getGroceryList().isEmpty());
    }

    @Test
    void execute_repeatedMeals_combinesDecimalsAndKeepsUnitsAndCaseSeparate() {
        Recipe rice = recipe(new GroceryItem("Rice", new BigDecimal("0.1"), "g"));
        Recipe other = recipe(new GroceryItem("Rice", new BigDecimal("0.2"), "g"),
                new GroceryItem("Rice", BigDecimal.ONE, "kg"),
                new GroceryItem("rice", BigDecimal.ONE, "g"));
        MealPlan plan = new MealPlan(List.of(new DayPlan(List.of(rice, other)), new DayPlan(List.of(rice))));
        activePlan.set(plan);
        book.add(rice);

        assertEquals("Grocery list generated. Use 'view-list' to view it.", command.execute(book));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Rice: 0.4g", "[ ] Rice: 1kg", "[ ] rice: 1g",
                "=============================================", "Total items to purchase: 3");
        ViewListCommand viewer = new ViewListCommand(activePlan::getGroceryList);
        assertEquals(expected, viewer.execute(book));
        command.execute(book);
        assertEquals(expected, viewer.execute(book));
        assertEquals(plan, activePlan.get().orElseThrow());
        assertEquals(List.of(rice), book.getRecipes());
        assertEquals(new BigDecimal("0.1"), rice.getIngredients().get(0).getQuantity());
    }

    @Test
    void execute_emptyPlan_storesPresentEmptyList() {
        activePlan.set(new MealPlan(List.of()));
        command.execute(book);

        assertEquals(List.of(), activePlan.getGroceryList().orElseThrow());
        assertTrue(new ViewListCommand(activePlan::getGroceryList).execute(book)
                .endsWith("Total items to purchase: 0"));
    }

    @Test
    void execute_planChangedAfterConstruction_readsLatestMeals() {
        activePlan.set(new MealPlan(List.of(new DayPlan(List.of(
                recipe(new GroceryItem("Beans", BigDecimal.ONE, "g")))))));
        command.execute(book);

        assertEquals("Beans", activePlan.getGroceryList().orElseThrow().get(0).getName());
    }

    @Test
    void constructor_nullPlanHolder_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GenerateListCommand(null));
    }

    /**
     * Creates a recipe with the supplied ingredients for aggregation tests.
     */
    private Recipe recipe(GroceryItem... ingredients) {
        return new Recipe("Meal", 0, 0, 0, 0, List.of(ingredients));
    }
}
