package seedu.mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks grocery snapshot ownership and invalidation when the active plan changes.
 */
class ActiveMealPlanTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final GroceryItem rice = new GroceryItem("Rice", BigDecimal.ONE, "g");

    @Test
    void setGroceryList_mutableInput_storesIndependentReadOnlySnapshot() {
        List<GroceryItem> items = new ArrayList<>(List.of(rice));
        activePlan.setGroceryList(items);
        items.clear();

        assertEquals(List.of(rice), activePlan.getGroceryList().orElseThrow());
        assertThrows(UnsupportedOperationException.class,
                () -> activePlan.getGroceryList().orElseThrow().clear());
    }

    @Test
    void set_invalidInput_preservesPlanAndGroceries() {
        MealPlan plan = new MealPlan(List.of());
        activePlan.set(plan);
        activePlan.setGroceryList(List.of(rice));

        assertThrows(NullPointerException.class, () -> activePlan.set(null));
        assertThrows(NullPointerException.class, () -> activePlan.setGroceryList(null));
        assertThrows(NullPointerException.class, () -> activePlan.setGroceryList(Arrays.asList(rice, null)));
        assertEquals(plan, activePlan.get().orElseThrow());
        assertEquals(List.of(rice), activePlan.getGroceryList().orElseThrow());
    }

    @Test
    void set_newPlan_clearsGroceriesWhileOldSnapshotRemainsReadable() {
        activePlan.setGroceryList(List.of(rice));
        List<GroceryItem> snapshot = activePlan.getGroceryList().orElseThrow();
        activePlan.set(new MealPlan(List.of()));

        assertTrue(activePlan.getGroceryList().isEmpty());
        assertEquals(List.of(rice), snapshot);
    }
}
