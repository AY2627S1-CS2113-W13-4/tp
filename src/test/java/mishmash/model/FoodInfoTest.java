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

import org.junit.jupiter.api.Test;

/**
 * Checks food-tag declarations and defensive model construction.
 */
class FoodInfoTest {
    @Test
    void parseField_absentAndExplicitNone_haveDifferentMeanings() {
        assertFalse(FoodInfo.parseField(null).isClassified());
        assertTrue(FoodInfo.parseField(" NONE ").isClassified());
        assertEquals("Unclassified", FoodInfo.createUnknown().describe());
        assertEquals("None declared", FoodInfo.parseField("none").describe());
    }

    @Test
    void parseField_validTags_acceptsAliasesAndDisplaysSortedLabels() {
        FoodInfo info = FoodInfo.parseField(" SOYA, Dairy, tree_nuts ");
        assertEquals(Set.of(FoodTag.SOY, FoodTag.MILK, FoodTag.TREE_NUT), info.tags());
        assertEquals("milk, soy, tree-nut", info.describe());
        for (FoodTag tag : FoodTag.values()) {
            assertEquals(tag, FoodTag.parse(tag.getLabel()));
            assertTrue(FoodTag.getSupportedLabels().contains(tag.getLabel()));
        }
    }

    @Test
    void parseField_invalidDeclarations_rejectsUnknownBlankAndRepeatedTags() {
        for (String value : List.of("", " ", "milk,", ",milk", "milk,,egg", "milk,dairy", "none,milk", "xyz")) {
            assertThrows(IllegalArgumentException.class, () -> FoodInfo.parseField(value), value);
        }
    }

    @Test
    void constructor_invalidDeclarations_rejectsNullOrContradictoryState() {
        assertThrows(NullPointerException.class, () -> new FoodInfo(true, null));
        assertThrows(NullPointerException.class,
                () -> new FoodInfo(true, new HashSet<>(Arrays.asList((FoodTag) null))));
        assertThrows(IllegalArgumentException.class, () -> new FoodInfo(false, Set.of(FoodTag.EGG)));
        assertThrows(NullPointerException.class, () -> FoodTag.parse(null));
        assertThrows(NullPointerException.class, () -> FoodTag.parseCsv(null));
        assertThrows(NullPointerException.class, () -> Diet.parse(null));
        assertThrows(NullPointerException.class, () -> Diet.VEGAN.isExcluded(null));
    }

    @Test
    void constructor_mutableTags_keepsIndependentReadOnlyDeclaration() {
        Set<FoodTag> tags = new HashSet<>(Set.of(FoodTag.EGG));
        FoodInfo info = new FoodInfo(true, tags);
        tags.clear();
        assertEquals(Set.of(FoodTag.EGG), info.tags());
        assertThrows(UnsupportedOperationException.class, () -> info.tags().clear());
    }

    @Test
    void recipe_legacyAndNewConstructors_preserveClassification() {
        List<GroceryItem> ingredients = List.of(new GroceryItem("Rice", BigDecimal.ONE, "g"));
        assertFalse(new Recipe("Rice", 100, 1, 1, 1, ingredients).getFoodInfo().isClassified());
        FoodInfo info = FoodInfo.parseField("none");
        assertEquals(info, new Recipe("Rice", 100, 1, 1, 1, ingredients, info).getFoodInfo());
        assertThrows(NullPointerException.class, () -> new Recipe("Rice", 100, 1, 1, 1, ingredients, null));
    }

    @Test
    void settings_invalidUpdate_preservesPreviousPreferences() {
        DietarySettings settings = new DietarySettings();
        DietaryPreferences vegan = new DietaryPreferences(Diet.VEGAN, Set.of(), Set.of());
        assertEquals(DietaryPreferences.createUnrestricted(), settings.get());
        settings.set(vegan);
        assertThrows(NullPointerException.class, () -> settings.set(null));
        assertEquals(vegan, settings.get());
    }

    @Test
    void parse_unknownDiet_explainsInvalidValueAndListsEverySupportedDiet() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> Diet.parse("unknown"));
        assertEquals("Invalid diet 'unknown'." + System.lineSeparator() + Diet.getInputHelp(), error.getMessage());
        assertTrue(error.getCause() instanceof IllegalArgumentException);
        for (Diet diet : Diet.values()) {
            assertTrue(Diet.getSupportedLabels().contains(diet.getLabel()));
        }
        assertTrue(Diet.getInputHelp().contains("lacto-ovo-vegetarian (vegetarian)"));
    }

    @Test
    void parseField_unknownTag_explainsInvalidValueAndListsEverySupportedTag() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> FoodInfo.parseField("unknown"));
        assertEquals("Invalid food tag 'unknown'." + System.lineSeparator()
                + "Valid food tags: " + FoodTag.getSupportedLabels() + ".", error.getMessage());
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }
}
