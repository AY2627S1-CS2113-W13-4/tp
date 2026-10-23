package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import mishmash.logic.GroceryListGenerator;

/**
 * Checks request retention, snapshot ownership, and atomic validation of generated plan updates.
 */
class ActiveMealPlanGeneratedTest {
    private final ActiveMealPlan active = new ActiveMealPlan();
    private final MealPlan plan = new MealPlan(List.of(new DayPlan(List.of())));
    private final GeneratePlanRequest request = new GeneratePlanRequest(1, NutritionTargets.none());
    private final GroceryItem item = new GroceryItem("Rice", BigDecimal.ONE, "g");

    @Test
    void setGenerated_validInput_retainsRequestAndCopiesGroceries() {
        List<GroceryItem> items = new ArrayList<>(List.of(item));
        active.setGenerated(plan, request, Optional.of(items));
        items.clear();
        assertSame(plan, active.get().orElseThrow());
        assertSame(request, active.getRequest().orElseThrow());
        assertEquals(List.of(item), active.getGroceryList().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> active.getGroceryList().orElseThrow().clear());
    }

    @Test
    void setGenerated_invalidInput_preservesEveryExistingField() {
        active.setGenerated(plan, request, Optional.of(List.of(item)));
        assertThrows(NullPointerException.class, () -> active.setGenerated(null, request, Optional.empty()));
        assertThrows(NullPointerException.class, () -> active.setGenerated(plan, null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> active.setGenerated(plan, request, null));
        assertThrows(NullPointerException.class,
                () -> active.setGenerated(plan, request, Optional.of(Arrays.asList(item, null))));
        assertThrows(IllegalArgumentException.class,
                () -> active.setGenerated(new MealPlan(List.of()), request, Optional.empty()));
        assertSame(plan, active.get().orElseThrow());
        assertSame(request, active.getRequest().orElseThrow());
        assertEquals(List.of(item), active.getGroceryList().orElseThrow());
    }

    @Test
    void clear_generatedPlan_removesPlanRequestAndGroceries() {
        active.setGenerated(plan, request, Optional.of(List.of(item)));
        active.clear();
        active.clear();
        assertTrue(active.get().isEmpty());
        assertTrue(active.getRequest().isEmpty());
        assertTrue(active.getGroceryList().isEmpty());
    }

    @Test
    void set_legacyPlan_invalidatesPreviousRequestAndGroceries() {
        active.setGenerated(plan, request, Optional.of(List.of(item)));
        active.set(plan);
        assertSame(plan, active.get().orElseThrow());
        assertTrue(active.getRequest().isEmpty());
        assertTrue(active.getGroceryList().isEmpty());
    }

    @Test
    void generateForPlan_repeatedRecipes_aggregatesOccurrencesAndKeepsUnitsSeparate() {
        Recipe rice = new Recipe("Rice", 1, 1, 1, 1, List.of(item,
                new GroceryItem("Rice", new BigDecimal("0.25"), "kg")));
        MealPlan repeated = new MealPlan(List.of(new DayPlan(List.of(rice, rice))));
        assertEquals(List.of("Rice: 2g", "Rice: 0.5kg"),
                new GroceryListGenerator().generateForPlan(repeated).stream().map(GroceryItem::toString).toList());
        assertEquals(List.of(), new GroceryListGenerator().generateForPlan(new MealPlan(List.of())));
        assertThrows(NullPointerException.class, () -> new GroceryListGenerator().generateForPlan(null));
    }
}
