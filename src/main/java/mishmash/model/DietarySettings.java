package mishmash.model;

import java.util.Objects;

/** Shares the user's current dietary preferences between commands during one session. */
public final class DietarySettings {
    private DietaryPreferences preferences = DietaryPreferences.none();

    public DietaryPreferences get() {
        return preferences;
    }

    public void set(DietaryPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }
}
