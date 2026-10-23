package mishmash.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import mishmash.model.DayPlan;
import mishmash.model.Diet;
import mishmash.model.DietaryPreferences;
import mishmash.model.FoodInfo;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.NutritionTargets;
import mishmash.model.Recipe;

/**
 * Tests dietary selection and partial repair independently of command parsing and display.
 */
class DietaryPlannerTest {
    private final DietaryPlanner planner = new DietaryPlanner(new MealPlanGenerator(new Random(42)));
    private final DietaryPreferences vegan = new DietaryPreferences(Diet.VEGAN, Set.of(), Set.of());

    /**
     * Creates a recipe with controlled nutrition and food metadata for planner tests.
     */
    private Recipe createRecipe(String name, int calories, String tags) {
        return new Recipe(name, calories, 10, 20, 5,
                List.of(new GroceryItem(name, BigDecimal.ONE, "g")), FoodInfo.parseField(tags));
    }

    /**
     * Creates three compatible recipes whose combined daily calories total 1800.
     */
    private List<Recipe> createSuitableRecipes() {
        return List.of(createRecipe("Rice", 600, "none"), createRecipe("Beans", 600, "legume"),
                createRecipe("Tofu", 600, "soy,legume"));
    }

    @Test
    void generate_restrictedPlan_meetsDietTargetsAndDailyVariety() {
        List<Recipe> suitableRecipes = createSuitableRecipes();
        Recipe milk = createRecipe("Milk", 600, "milk,lactose");
        List<Recipe> recipes = List.of(suitableRecipes.get(0), suitableRecipes.get(1), suitableRecipes.get(2), milk,
                createRecipe("Unknown", 600, null));
        NutritionTargets targets = new NutritionTargets(1800, 30, 60, 15);
        MealPlan plan = planner.generate(recipes, new GeneratePlanRequest(7, targets), vegan);

        assertEquals(7, plan.getNumberOfDays());
        for (DayPlan day : plan.getDays()) {
            assertEquals(3, Set.copyOf(day.getMeals()).size());
            assertTrue(targets.isSatisfiedBy(day));
            assertTrue(day.getMeals().stream().allMatch(suitableRecipes::contains));
        }
        assertEquals(5, recipes.size());
    }

    @Test
    void repair_conflictingDays_preservesCompatibleDaysAndOriginalPositions() {
        List<Recipe> recipes = createSuitableRecipes();
        DayPlan unchanged = new DayPlan(recipes);
        DayPlan conflicting = new DayPlan(List.of(recipes.get(0), recipes.get(1), createRecipe("Egg", 600, "egg")));
        MealPlan current = new MealPlan(List.of(unchanged, conflicting, unchanged, conflicting));
        NutritionTargets targets = new NutritionTargets(1800, 30, 60, 15);

        MealPlan result = planner.repair(recipes, current, new GeneratePlanRequest(4, targets), vegan);

        assertSame(unchanged, result.getDays().get(0));
        assertSame(unchanged, result.getDays().get(2));
        assertFalse(result.getDays().get(1).getMeals().contains(conflicting.getMeals().get(2)));
        assertEquals(4, result.getNumberOfDays());
        assertTrue(result.getDays().stream().allMatch(targets::isSatisfiedBy));
        assertTrue(planner.isCompatible(result, vegan));
        assertSame(conflicting, current.getDays().get(1));
    }

    @Test
    void repair_compatiblePlan_returnsSamePlanWithoutNeedingRecipePool() {
        MealPlan current = new MealPlan(List.of(new DayPlan(createSuitableRecipes())));
        assertSame(current, planner.repair(List.of(), current,
                new GeneratePlanRequest(1, NutritionTargets.none()), vegan));
    }

    @Test
    void generate_insufficientCompatibleRecipes_reportsCount() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> planner.generate(createSuitableRecipes().subList(0, 2),
                        new GeneratePlanRequest(1, NutritionTargets.none()), vegan));
        assertTrue(error.getMessage().contains("found 2"));
        assertTrue(error.getMessage().contains("classified"));
    }

    @Test
    void generate_unreachableNutritionTargets_reportsFailure() {
        assertThrows(IllegalArgumentException.class, () -> planner.generate(createSuitableRecipes(),
                new GeneratePlanRequest(1, new NutritionTargets(5000, null, null, null)), vegan));
    }

    @Test
    void repair_inconsistentRequest_rejectsBeforeGenerating() {
        MealPlan current = new MealPlan(List.of(new DayPlan(createSuitableRecipes())));
        assertThrows(IllegalArgumentException.class, () -> planner.repair(createSuitableRecipes(), current,
                new GeneratePlanRequest(2, NutritionTargets.none()), vegan));
    }

    @Test
    void generate_nullInputs_rejectsProgrammingErrors() {
        GeneratePlanRequest request = new GeneratePlanRequest(1, NutritionTargets.none());
        assertThrows(NullPointerException.class, () -> new DietaryPlanner(null));
        assertThrows(NullPointerException.class, () -> planner.generate(null, request, vegan));
        assertThrows(NullPointerException.class, () -> planner.generate(createSuitableRecipes(), null, vegan));
        assertThrows(NullPointerException.class, () -> planner.generate(createSuitableRecipes(), request, null));
        assertThrows(NullPointerException.class, () -> planner.generate(Arrays.asList((Recipe) null), request, vegan));
        assertThrows(NullPointerException.class, () -> planner.isCompatible(null, vegan));
        assertThrows(NullPointerException.class,
                () -> planner.isCompatible(new MealPlan(List.of()), null));
        assertThrows(NullPointerException.class, () -> planner.repair(null, null, null, null));
    }

    @Test
    void generate_brokenGenerator_assertsDietaryInvariant() {
        MealPlanGenerator broken = new MealPlanGenerator(new Random(1)) {
            @Override
            public MealPlan generate(List<Recipe> recipes, int days, NutritionTargets targets) {
                return new MealPlan(List.of(new DayPlan(List.of(createRecipe("Egg", 600, "egg")))));
            }
        };
        assertThrows(AssertionError.class, () -> new DietaryPlanner(broken).generate(createSuitableRecipes(),
                new GeneratePlanRequest(1, NutritionTargets.none()), vegan));
    }
}
