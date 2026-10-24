package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Checks model validation and immutable ingredient edits independently of command parsing. */
class RecipeChangesTest {
    @Test
    void constructor_noEditsOrBlankName_rejectsInvalidChanges() {
        assertThrows(IllegalArgumentException.class,
                () -> new RecipeChanges(null, null, null, null, null, List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new RecipeChanges("  ", null, null, null, null, List.of(), null));
    }

    @Test
    void constructor_negativeNutrition_rejectsEveryField() {
        assertThrows(IllegalArgumentException.class,
                () -> new RecipeChanges(null, -1, null, null, null, List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new RecipeChanges(null, null, -1, null, null, List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new RecipeChanges(null, null, null, -1, null, List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new RecipeChanges(null, null, null, null, -1, List.of(), null));
    }

    @Test
    void constructor_invalidIngredients_rejectsNullAndZeroQuantity() {
        assertThrows(NullPointerException.class,
                () -> new RecipeChanges("Rice", null, null, null, null, null, null));
        assertThrows(NullPointerException.class,
                () -> new RecipeChanges("Rice", null, null, null, null, Arrays.asList((GroceryItem) null), null));
        assertThrows(IllegalArgumentException.class, () -> new RecipeChanges(null, null, null, null, null,
                List.of(new GroceryItem("Rice", BigDecimal.ZERO, "g")), null));
    }

    @Test
    void constructor_ingredientListIsCopied_callerCannotChangePendingEdit() {
        GroceryItem rice = new GroceryItem("Rice", BigDecimal.ONE, "g");
        List<GroceryItem> source = new ArrayList<>(List.of(rice));
        RecipeChanges changes = new RecipeChanges(null, null, null, null, null, source, null);
        source.clear();

        assertEquals(List.of(rice), changes.ingredients());
        assertThrows(UnsupportedOperationException.class, () -> changes.ingredients().clear());
        assertThrows(NullPointerException.class, () -> changes.applyTo(null));
    }
}
