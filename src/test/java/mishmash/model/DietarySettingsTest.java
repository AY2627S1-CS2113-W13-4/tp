package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Checks setting, clearing, and idempotency of the shared dietary profile.
 */
class DietarySettingsTest {
    private final DietarySettings settings = new DietarySettings();

    @Test
    void clear_restrictedProfile_returnsTrueAndResetsEveryField() {
        settings.set(new DietaryPreferences(Diet.VEGAN, Set.of(FoodTag.SOY), Set.of("Peanut")));

        assertTrue(settings.clear());
        assertFalse(settings.get().isRestricted());
        assertTrue(settings.get().avoidedTags().isEmpty());
        assertTrue(settings.get().excludedIngredients().isEmpty());
    }

    @Test
    void clear_unrestrictedProfile_returnsFalseAndRemainsUnrestricted() {
        assertFalse(settings.clear());
        assertFalse(settings.clear());
        assertFalse(settings.get().isRestricted());
    }

    @Test
    void set_nullProfile_preservesPreviousProfile() {
        DietaryPreferences restricted = new DietaryPreferences(Diet.VEGETARIAN, Set.of(), Set.of());
        settings.set(restricted);

        assertThrows(NullPointerException.class, () -> settings.set(null));
        assertTrue(settings.get().isRestricted());
    }
}
