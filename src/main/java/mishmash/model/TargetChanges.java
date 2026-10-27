package mishmash.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

//@@author neil0618
/**
 * A validated request to change some of the user's stored daily targets.
 *
 * <p>Each nutrient is in one of three states: not mentioned (keep the stored value), given a value
 * (replace it), or cleared (remove it). A nutrient that is not in the map is "not mentioned";
 * an {@link Optional#empty()} entry means "cleared".</p>
 */
public final class TargetChanges {
    private final Map<Nutrient, Optional<Integer>> changes = new EnumMap<>(Nutrient.class);

    /**
     * Creates a change request.
     *
     * @param changes For each nutrient to change, its new positive value, or an empty Optional to clear it.
     * @throws NullPointerException If the map, a key, or a value is null.
     * @throws IllegalArgumentException If no nutrient is mentioned or a new value is not positive.
     */
    public TargetChanges(Map<Nutrient, Optional<Integer>> changes) {
        Objects.requireNonNull(changes, "changes");
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("Supply at least one daily target to set or clear.");
        }
        for (Map.Entry<Nutrient, Optional<Integer>> entry : changes.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "nutrient");
            Optional<Integer> value = Objects.requireNonNull(entry.getValue(), "value");
            if (value.isPresent() && value.get() <= 0) {
                throw new IllegalArgumentException(entry.getKey().getLabel() + " target must be positive.");
            }
            this.changes.put(entry.getKey(), value);
        }
    }

    /**
     * Applies these changes to the given targets without modifying them.
     *
     * @param current The targets before the change.
     * @return New targets in which mentioned nutrients are replaced or cleared and the rest are kept.
     * @throws NullPointerException If the current targets are null.
     */
    public NutritionTargets applyTo(NutritionTargets current) {
        Objects.requireNonNull(current, "current");
        return new NutritionTargets(resolve(Nutrient.CALORIES, current), resolve(Nutrient.PROTEIN, current),
                resolve(Nutrient.CARBS, current), resolve(Nutrient.FATS, current));
    }

    /**
     * Returns the new value for one nutrient: the requested one, null if cleared, or the current one.
     */
    private Integer resolve(Nutrient nutrient, NutritionTargets current) {
        Optional<Integer> change = changes.get(nutrient);
        if (change == null) {
            return current.get(nutrient);
        }
        return change.orElse(null);
    }
}
//@@author
