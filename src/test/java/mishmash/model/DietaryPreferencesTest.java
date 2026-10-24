package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests dietary rules, explicit classifications, and defensive collection ownership.
 */
class DietaryPreferencesTest {
    /**
     * Creates a classified or unclassified recipe with one ingredient for dietary-rule tests.
     */
    private Recipe createRecipe(String ingredient, FoodInfo info) {
        return new Recipe("Recipe", 300, 10, 30, 5,
                List.of(new GroceryItem(ingredient, BigDecimal.ONE, "g")), info);
    }

    /**
     * Creates a profile containing only the named diet.
     */
    private DietaryPreferences createDietPreferences(Diet diet) {
        return new DietaryPreferences(diet, Set.of(), Set.of());
    }

    @TestFactory
    Stream<DynamicTest> isAllowed_eachAvoidedCategory_rejectsMatchingRecipe() {
        return Arrays.stream(FoodTag.values()).map(tag -> DynamicTest.dynamicTest(tag.getLabel(), () -> {
            DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, Set.of(tag), Set.of());
            assertFalse(preferences.isAllowed(createRecipe("Ingredient", new FoodInfo(true, Set.of(tag)))));
            assertTrue(preferences.isAllowed(createRecipe("Rice", new FoodInfo(true, Set.of()))));
        }));
    }

    @TestFactory
    Stream<DynamicTest> isAllowed_namedDiets_enforceDietSpecificRules() {
        Object[][] cases = {
            {Diet.VEGETARIAN, FoodTag.POULTRY, false},
            {Diet.VEGETARIAN, FoodTag.FISH, false},
            {Diet.VEGETARIAN, FoodTag.MEAT_DERIVATIVE, false},
            {Diet.VEGETARIAN, FoodTag.GELATIN, false},
            {Diet.VEGETARIAN, FoodTag.MILK, true},
            {Diet.VEGETARIAN, FoodTag.EGG, true},
            {Diet.VEGAN, FoodTag.MILK, false},
            {Diet.VEGAN, FoodTag.LACTOSE, false},
            {Diet.VEGAN, FoodTag.EGG, false},
            {Diet.VEGAN, FoodTag.HONEY, false},
            {Diet.VEGAN, FoodTag.OTHER_ANIMAL_PRODUCT, false},
            {Diet.VEGAN, FoodTag.SOY, true},
            {Diet.PESCATARIAN, FoodTag.FISH, true},
            {Diet.PESCATARIAN, FoodTag.CRUSTACEAN, true},
            {Diet.PESCATARIAN, FoodTag.BEEF, false},
            {Diet.PESCATARIAN, FoodTag.PORK, false},
            {Diet.LACTO_VEGETARIAN, FoodTag.MILK, true},
            {Diet.LACTO_VEGETARIAN, FoodTag.EGG, false},
            {Diet.OVO_VEGETARIAN, FoodTag.EGG, true},
            {Diet.OVO_VEGETARIAN, FoodTag.MILK, false},
            {Diet.NONE, FoodTag.PORK, true}
        };
        return Arrays.stream(cases).map(testCase -> DynamicTest.dynamicTest(testCase[0] + " " + testCase[1], () -> {
            Recipe recipe = createRecipe("Ingredient", new FoodInfo(true, Set.of((FoodTag) testCase[1])));
            assertEquals(testCase[2], createDietPreferences((Diet) testCase[0]).isAllowed(recipe));
        }));
    }

    @Test
    void isAllowed_unknownClassification_requiresUnrestrictedPreferences() {
        Recipe unknown = createRecipe("Rice", FoodInfo.createUnknown());
        assertTrue(DietaryPreferences.createUnrestricted().isAllowed(unknown));
        assertFalse(createDietPreferences(Diet.VEGAN).isAllowed(unknown));
        assertFalse(new DietaryPreferences(Diet.NONE, Set.of(), Set.of("Salt")).isAllowed(unknown));
    }

    @Test
    void isAllowed_dietAndAdditionalExclusions_appliesBoth() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.VEGAN, Set.of(FoodTag.SOY), Set.of());
        assertFalse(preferences.isAllowed(createRecipe("Tofu", new FoodInfo(true, Set.of(FoodTag.SOY)))));
        assertFalse(preferences.isAllowed(createRecipe("Egg", new FoodInfo(true, Set.of(FoodTag.EGG)))));
        assertTrue(preferences.isAllowed(createRecipe("Rice", FoodInfo.parseField("none"))));
    }

    @Test
    void isAllowed_ingredientExclusion_normalizesWholeNameWithoutSubstringMatching() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, Set.of(), Set.of(" BELL   PEPPER "));
        assertFalse(preferences.isAllowed(createRecipe("bell pepper", FoodInfo.parseField("none"))));
        assertTrue(preferences.isAllowed(createRecipe("pepper", FoodInfo.parseField("none"))));
        assertTrue(preferences.isAllowed(createRecipe("Bell Pepper Sauce", FoodInfo.parseField("none"))));
    }

    @Test
    void isAllowed_plantMilk_usesDeclaredCategoryInsteadOfIngredientSubstring() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, Set.of(FoodTag.MILK), Set.of());
        assertTrue(preferences.isAllowed(createRecipe("Almond Milk", FoodInfo.parseField("tree-nut"))));
        assertFalse(preferences.isAllowed(createRecipe("Lactose-free Milk", FoodInfo.parseField("milk"))));
    }

    @Test
    void constructor_mutableCollections_makesReadOnlyCopies() {
        Set<FoodTag> tags = new HashSet<>(Set.of(FoodTag.MILK));
        Set<String> ingredients = new HashSet<>(Set.of("Salt"));
        DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, tags, ingredients);
        tags.clear();
        ingredients.clear();
        assertEquals(Set.of(FoodTag.MILK), preferences.avoidedTags());
        assertEquals(Set.of("salt"), preferences.excludedIngredients());
        assertThrows(UnsupportedOperationException.class, () -> preferences.avoidedTags().clear());
        assertThrows(UnsupportedOperationException.class, () -> preferences.excludedIngredients().clear());
    }

    @Test
    void constructor_invalidExclusions_rejectsBlankDuplicateAndNullValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new DietaryPreferences(Diet.NONE, Set.of(), Set.of(" ")));
        assertThrows(IllegalArgumentException.class,
                () -> new DietaryPreferences(Diet.NONE, Set.of(), Set.of("salt", " SALT ")));
        assertThrows(NullPointerException.class, () -> new DietaryPreferences(null, Set.of(), Set.of()));
        assertThrows(NullPointerException.class, () -> new DietaryPreferences(Diet.NONE, null, Set.of()));
        assertThrows(NullPointerException.class, () -> new DietaryPreferences(Diet.NONE, Set.of(), null));
        assertThrows(NullPointerException.class, () -> DietaryPreferences.createUnrestricted().isAllowed(null));
        assertThrows(NullPointerException.class,
                () -> new DietaryPreferences(Diet.NONE, new HashSet<>(Arrays.asList((FoodTag) null)), Set.of()));
        assertThrows(NullPointerException.class,
                () -> new DietaryPreferences(Diet.NONE, Set.of(), new HashSet<>(Arrays.asList((String) null))));
    }

}
