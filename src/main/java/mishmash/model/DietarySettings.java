package mishmash.model;

import java.util.Objects;

/**
 * Shares the user's current dietary preferences between commands during one session.
 */
public final class DietarySettings {
    /** Immutable profile shared by commands in the current application session. */
    private DietaryPreferences preferences = DietaryPreferences.createUnrestricted();

    /**
     * Returns the immutable dietary profile currently shared by session commands.
     *
     * @return The current profile, which is never null.
     */
    public DietaryPreferences get() {
        return preferences;
    }

    /**
     * Replaces the session's dietary profile after validating the replacement.
     *
     * @param preferences The new immutable profile.
     * @throws NullPointerException If the profile is null; the previous profile is preserved.
     */
    public void set(DietaryPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }
}
