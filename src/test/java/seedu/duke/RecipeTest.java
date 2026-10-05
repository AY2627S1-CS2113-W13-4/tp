package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks recipe fields, constraints, and protection against external changes.
 */
class RecipeTest {
    private GroceryItem rice() {
        return new GroceryItem("Rice", new BigDecimal("150"), "g");
    }

    @Test
    void constructor_validData_preservesFields() {
        Recipe recipe = new Recipe(" Chicken and Rice ", 650, 50, 70, 15,
                List.of(rice()));

        assertEquals("Chicken and Rice", recipe.getName());
        assertEquals(650, recipe.getCalories());
        assertEquals(50, recipe.getProtein());
        assertEquals(70, recipe.getCarbs());
        assertEquals(15, recipe.getFats());
        assertEquals("Rice", recipe.getIngredients().get(0).getName());
    }

    @Test
    void constructor_invalidFields_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe(" ", 650, 50, 70, 15, List.of(rice())));
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe("Rice", 650, 50, 70, 15, List.of()));

        int[][] invalidNutrition = {
                {-1, 50, 70, 15},
                {650, -1, 70, 15},
                {650, 50, -1, 15},
                {650, 50, 70, -1}
        };
        for (int[] values : invalidNutrition) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Recipe("Rice", values[0], values[1], values[2],
                            values[3], List.of(rice())));
        }

        GroceryItem zeroQuantity = new GroceryItem("Rice", BigDecimal.ZERO, "g");
        assertThrows(IllegalArgumentException.class,
                () -> new Recipe("Rice", 0, 0, 0, 0, List.of(zeroQuantity)));
    }

    @Test
    void constructor_zeroNutrition_acceptsRecipe() {
        Recipe recipe = new Recipe("Water", 0, 0, 0, 0,
                List.of(new GroceryItem("Water", new BigDecimal("250"), "ml")));

        assertEquals(0, recipe.getCalories());
    }

    @Test
    void getIngredients_externalChanges_doNotChangeRecipe() {
        List<GroceryItem> ingredients = new ArrayList<>(List.of(rice()));
        Recipe recipe = new Recipe("Rice", 200, 4, 45, 1, ingredients);

        ingredients.clear();

        assertEquals(1, recipe.getIngredients().size());
        assertThrows(UnsupportedOperationException.class,
                () -> recipe.getIngredients().clear());
    }

    @Test
    void constructor_nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Recipe(null, 200, 4, 45, 1, List.of(rice())));
        assertThrows(NullPointerException.class,
                () -> new Recipe("Rice", 200, 4, 45, 1, null));
        assertThrows(NullPointerException.class,
                () -> new Recipe("Rice", 200, 4, 45, 1,
                        Arrays.asList(rice(), null)));
    }
}

