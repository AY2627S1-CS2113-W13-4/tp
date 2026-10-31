package mishmash.model;

/**
 * Holds validated one-based positions and a positive portion count for one plan adjustment.
 *
 * @param dayNumber The selected day, counting from one.
 * @param mealNumber The selected meal within that day, counting from one.
 * @param portionCount The requested number of whole recipe portions.
 */
public record PortionAdjustment(int dayNumber, int mealNumber, int portionCount) {
    /**
     * Creates an adjustment containing only positive whole numbers.
     *
     * @throws IllegalArgumentException If any value is less than one.
     */
    public PortionAdjustment {
        if (dayNumber < 1) {
            throw new IllegalArgumentException("Day number must be at least 1.");
        }
        if (mealNumber < 1) {
            throw new IllegalArgumentException("Meal number must be at least 1.");
        }
        if (portionCount < 1) {
            throw new IllegalArgumentException("Portion count must be at least 1.");
        }
    }
}
