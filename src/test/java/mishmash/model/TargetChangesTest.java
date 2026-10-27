package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

//@@author neil0618
/**
 * Checks how stored targets are merged, replaced, cleared and summarised.
 */
class TargetChangesTest {
    private static final NutritionTargets SAVED = new NutritionTargets(2000, 150, null, 70);

    private static Map<Nutrient, Optional<Integer>> changes() {
        return new EnumMap<>(Nutrient.class);
    }

    @Test
    void applyTo_setsMentionedNutrientsAndKeepsTheRest() {
        Map<Nutrient, Optional<Integer>> map = changes();
        map.put(Nutrient.CALORIES, Optional.of(1800));
        map.put(Nutrient.CARBS, Optional.of(200));

        NutritionTargets result = new TargetChanges(map).applyTo(SAVED);

        assertEquals(1800, result.getCalories());
        assertEquals(150, result.getProtein());
        assertEquals(200, result.getCarbs());
        assertEquals(70, result.getFats());
    }

    @Test
    void applyTo_emptyOptionalClearsOnlyThatNutrient() {
        Map<Nutrient, Optional<Integer>> map = changes();
        map.put(Nutrient.PROTEIN, Optional.empty());

        NutritionTargets result = new TargetChanges(map).applyTo(SAVED);

        assertNull(result.getProtein());
        assertEquals(2000, result.getCalories());
        assertEquals(70, result.getFats());
    }

    @Test
    void applyTo_clearingEverythingLeavesNoTarget() {
        Map<Nutrient, Optional<Integer>> map = changes();
        for (Nutrient nutrient : Nutrient.values()) {
            map.put(nutrient, Optional.empty());
        }

        assertTrue(!new TargetChanges(map).applyTo(SAVED).hasAnyTarget());
    }

    @Test
    void applyTo_doesNotModifyTheOriginalTargets() {
        Map<Nutrient, Optional<Integer>> map = changes();
        map.put(Nutrient.CALORIES, Optional.of(1));

        NutritionTargets result = new TargetChanges(map).applyTo(SAVED);

        assertNotSame(SAVED, result);
        assertEquals(2000, SAVED.getCalories());
    }

    @Test
    void constructor_noChanges_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TargetChanges(changes()));
    }

    @Test
    void constructor_nonPositiveValue_throws() {
        Map<Nutrient, Optional<Integer>> map = changes();
        map.put(Nutrient.FATS, Optional.of(0));
        assertThrows(IllegalArgumentException.class, () -> new TargetChanges(map));
    }

    @Test
    void constructor_nulls_throw() {
        assertThrows(NullPointerException.class, () -> new TargetChanges(null));
        Map<Nutrient, Optional<Integer>> map = changes();
        map.put(Nutrient.FATS, null);
        assertThrows(NullPointerException.class, () -> new TargetChanges(map));
        Map<Nutrient, Optional<Integer>> valid = changes();
        valid.put(Nutrient.FATS, Optional.of(5));
        assertThrows(NullPointerException.class, () -> new TargetChanges(valid).applyTo(null));
    }

    @Test
    void constructor_copiesTheMap() {
        Map<Nutrient, Optional<Integer>> map = changes();
        map.put(Nutrient.CALORIES, Optional.of(1800));
        TargetChanges targetChanges = new TargetChanges(map);

        map.put(Nutrient.CALORIES, Optional.of(1));

        assertEquals(1800, targetChanges.applyTo(NutritionTargets.none()).getCalories());
    }

    @Test
    void overriddenBy_explicitWinsAndGapsKeepBase() {
        NutritionTargets result = SAVED.overriddenBy(new NutritionTargets(1800, null, 250, null));

        assertEquals(1800, result.getCalories());
        assertEquals(150, result.getProtein());
        assertEquals(250, result.getCarbs());
        assertEquals(70, result.getFats());
    }

    @Test
    void overriddenBy_nothingExplicit_keepsBase() {
        NutritionTargets result = SAVED.overriddenBy(NutritionTargets.none());

        for (Nutrient nutrient : Nutrient.values()) {
            assertEquals(SAVED.get(nutrient), result.get(nutrient));
        }
        assertThrows(NullPointerException.class, () -> SAVED.overriddenBy(null));
    }

    @Test
    void summarise_listsSetTargetsInOrder() {
        assertEquals("2000 kcal, 150 g protein, 70 g fats", SAVED.summarise());
        assertEquals("none", NutritionTargets.none().summarise());
        assertEquals("200 g carbs", new NutritionTargets(null, null, 200, null).summarise());
    }

    @Test
    void get_returnsEachNutrient() {
        NutritionTargets all = new NutritionTargets(1, 2, 3, 4);
        assertEquals(1, all.get(Nutrient.CALORIES));
        assertEquals(2, all.get(Nutrient.PROTEIN));
        assertEquals(3, all.get(Nutrient.CARBS));
        assertEquals(4, all.get(Nutrient.FATS));
        assertThrows(NullPointerException.class, () -> all.get(null));
    }

    @Test
    void targetSettings_startsEmptyAndKeepsTheLastValue() {
        TargetSettings settings = new TargetSettings();
        assertTrue(!settings.get().hasAnyTarget());

        settings.set(SAVED);
        assertSame(SAVED, settings.get());

        assertThrows(NullPointerException.class, () -> settings.set(null));
        assertSame(SAVED, settings.get());
    }
}
//@@author
