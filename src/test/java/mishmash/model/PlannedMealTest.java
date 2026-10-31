package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks whole-portion scaling, validation, and immutability of planned meals.
 */
class PlannedMealTest {
    private final Recipe recipe = new Recipe("Rice bowl", 500, 20, 80, 10,
            List.of(new GroceryItem("Rice", new BigDecimal("125.5"), "g")));

    @Test
    void constructor_invalidArguments_rejectsBeforeCreatingMeal() {
        assertThrows(NullPointerException.class, () -> new PlannedMeal(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new PlannedMeal(recipe, 0));
        assertThrows(IllegalArgumentException.class, () -> new PlannedMeal(recipe, -1));
    }

    @Test
    void nutritionAndIngredients_multiplePortions_scalesExactlyWithoutChangingRecipe() {
        PlannedMeal meal = new PlannedMeal(recipe, 3);

        assertEquals(1500, meal.getCalories());
        assertEquals(60, meal.getProtein());
        assertEquals(240, meal.getCarbs());
        assertEquals(30, meal.getFats());
        assertEquals(new BigDecimal("376.5"), meal.getScaledIngredients().get(0).getQuantity());
        assertEquals(new BigDecimal("125.5"), recipe.getIngredients().get(0).getQuantity());
        assertThrows(UnsupportedOperationException.class, () -> meal.getScaledIngredients().clear());
    }

    @Test
    void withPortionCount_sameAndDifferentCounts_reusesOnlyUnchangedMeal() {
        PlannedMeal original = PlannedMeal.onePortionOf(recipe);

        assertSame(original, original.withPortionCount(1));
        PlannedMeal changed = original.withPortionCount(2);
        assertSame(recipe, changed.getRecipe());
        assertEquals(2, changed.getPortionCount());
        assertEquals(1, original.getPortionCount());
    }
}
