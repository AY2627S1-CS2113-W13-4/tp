package mishmash.model;

import java.util.Objects;

/**
 * Optional daily nutrition targets for a generated meal plan.
 *
 * <p>Each target may be {@code null}, meaning "no target". A day's meals satisfy the
 * targets when every supplied target is matched within {@link #TOLERANCE} (±15%).</p>
 */
public class NutritionTargets {
    /** Allowed relative deviation from each target, e.g. 0.15 means ±15%. */
    public static final double TOLERANCE = 0.15;

    private final Integer calories;
    private final Integer protein;
    private final Integer carbs;
    private final Integer fats;

    /**
     * Creates a set of daily targets. Pass {@code null} for any target that is not set.
     *
     * @param calories Daily calories in kcal, or null.
     * @param protein Daily protein in grams, or null.
     * @param carbs Daily carbohydrates in grams, or null.
     * @param fats Daily fats in grams, or null.
     * @throws IllegalArgumentException If a supplied target is not positive.
     */
    public NutritionTargets(Integer calories, Integer protein, Integer carbs, Integer fats) {
        if (isNotPositive(calories) || isNotPositive(protein) || isNotPositive(carbs) || isNotPositive(fats)) {
            throw new IllegalArgumentException("Nutrition targets must be positive.");
        }
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }

    /**
     * Returns targets with nothing set, which means a purely random plan.
     */
    public static NutritionTargets none() {
        return new NutritionTargets(null, null, null, null);
    }

    /**
     * Returns true if at least one target was supplied.
     */
    public boolean hasAnyTarget() {
        return calories != null || protein != null || carbs != null || fats != null;
    }

    /**
     * Returns true if the given day's meals are within tolerance of every supplied target.
     *
     * @param day The day to check.
     * @return Whether every supplied target is matched within {@link #TOLERANCE}.
     * @throws NullPointerException If the day is null.
     */
    public boolean isSatisfiedBy(DayPlan day) {
        Objects.requireNonNull(day, "day");
        return deviation(calories, day.getTotalCalories()) <= TOLERANCE
                && deviation(protein, day.getTotalProtein()) <= TOLERANCE
                && deviation(carbs, day.getTotalCarbs()) <= TOLERANCE
                && deviation(fats, day.getTotalFats()) <= TOLERANCE;
    }

    /**
     * Returns the daily calorie target in kcal, or null if none was set.
     */
    public Integer getCalories() {
        return calories;
    }

    /**
     * Returns the daily protein target in grams, or null if none was set.
     */
    public Integer getProtein() {
        return protein;
    }

    /**
     * Returns the daily carbohydrate target in grams, or null if none was set.
     */
    public Integer getCarbs() {
        return carbs;
    }

    /**
     * Returns the daily fat target in grams, or null if none was set.
     */
    public Integer getFats() {
        return fats;
    }

    /**
     * Returns whether a supplied target is invalid; an omitted target is allowed.
     */
    private static boolean isNotPositive(Integer target) {
        return target != null && target <= 0;
    }

    /**
     * Returns the relative distance of {@code actual} from {@code target}
     * (e.g. 0.10 means 10% off), or 0 when there is no target.
     */
    private static double deviation(Integer target, long actual) {
        if (target == null) {
            return 0;
        }
        return Math.abs(actual - target) / (double) target;
    }
}
