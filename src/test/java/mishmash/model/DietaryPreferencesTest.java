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

/** Tests dietary rules, explicit classifications, and defensive collection ownership. */
class DietaryPreferencesTest {
    private Recipe recipe(String ingredient, FoodInfo info) {
        return new Recipe("Recipe", 300, 10, 30, 5,
                List.of(new GroceryItem(ingredient, BigDecimal.ONE, "g")), info);
    }

    private DietaryPreferences diet(Diet diet) {
        return new DietaryPreferences(diet, Set.of(), Set.of());
    }

    @TestFactory
    Stream<DynamicTest> allows_eachAvoidedCategory_rejectsMatchingRecipe() {
        return Arrays.stream(FoodTag.values()).map(tag -> DynamicTest.dynamicTest(tag.label(), () -> {
            DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, Set.of(tag), Set.of());
            assertFalse(preferences.allows(recipe("Ingredient", new FoodInfo(true, Set.of(tag)))));
            assertTrue(preferences.allows(recipe("Rice", new FoodInfo(true, Set.of()))));
        }));
    }

    @TestFactory
    Stream<DynamicTest> allows_namedDiets_enforceDietSpecificRules() {
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
        return Arrays.stream(cases).map(c -> DynamicTest.dynamicTest(c[0] + " " + c[1], () -> {
            Recipe recipe = recipe("Ingredient", new FoodInfo(true, Set.of((FoodTag) c[1])));
            assertEquals(c[2], diet((Diet) c[0]).allows(recipe));
        }));
    }

    @Test
    void allows_unknownClassification_requiresUnrestrictedPreferences() {
        Recipe unknown = recipe("Rice", FoodInfo.unknown());
        assertTrue(DietaryPreferences.none().allows(unknown));
        assertFalse(diet(Diet.VEGAN).allows(unknown));
        assertFalse(new DietaryPreferences(Diet.NONE, Set.of(), Set.of("Salt")).allows(unknown));
    }

    @Test
    void allows_dietAndAdditionalExclusions_appliesBoth() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.VEGAN, Set.of(FoodTag.SOY), Set.of());
        assertFalse(preferences.allows(recipe("Tofu", new FoodInfo(true, Set.of(FoodTag.SOY)))));
        assertFalse(preferences.allows(recipe("Egg", new FoodInfo(true, Set.of(FoodTag.EGG)))));
        assertTrue(preferences.allows(recipe("Rice", FoodInfo.fromField("none"))));
    }

    @Test
    void allows_ingredientExclusion_normalizesWholeNameWithoutSubstringMatching() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, Set.of(), Set.of(" BELL   PEPPER "));
        assertFalse(preferences.allows(recipe("bell pepper", FoodInfo.fromField("none"))));
        assertTrue(preferences.allows(recipe("pepper", FoodInfo.fromField("none"))));
        assertTrue(preferences.allows(recipe("Bell Pepper Sauce", FoodInfo.fromField("none"))));
    }

    @Test
    void allows_plantMilk_usesDeclaredCategoryInsteadOfIngredientSubstring() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.NONE, Set.of(FoodTag.MILK), Set.of());
        assertTrue(preferences.allows(recipe("Almond Milk", FoodInfo.fromField("tree-nut"))));
        assertFalse(preferences.allows(recipe("Lactose-free Milk", FoodInfo.fromField("milk"))));
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
        assertThrows(NullPointerException.class, () -> DietaryPreferences.none().allows(null));
        assertThrows(NullPointerException.class,
                () -> new DietaryPreferences(Diet.NONE, new HashSet<>(Arrays.asList((FoodTag) null)), Set.of()));
        assertThrows(NullPointerException.class,
                () -> new DietaryPreferences(Diet.NONE, Set.of(), new HashSet<>(Arrays.asList((String) null))));
    }

    @Test
    void describe_multipleExclusions_hasStableReadableOrder() {
        DietaryPreferences preferences = new DietaryPreferences(Diet.VEGAN,
                Set.of(FoodTag.SOY, FoodTag.PEANUT), Set.of("Salt", "Mushroom"));
        assertEquals(String.join(System.lineSeparator(), "Diet: vegan",
                "Avoided food categories: peanut, soy", "Excluded ingredients: mushroom, salt"),
                preferences.describe());
        assertTrue(DietaryPreferences.none().describe().contains("Avoided food categories: none"));
    }
}
