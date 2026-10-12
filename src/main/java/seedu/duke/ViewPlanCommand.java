package seedu.duke;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import seedu.duke.MealPlanViewer.MealSummary;

/**
 * Displays all days or one selected day from the current meal plan without generating meals.
 */
public class ViewPlanCommand implements Command {
    /**
     * Describes the command's optional day argument.
     */
    public static final String USAGE = "Format: view-plan [d/DAY_NUMBER]";

    /**
     * Reads the current plan at execution time; an empty list means there is no active plan.
     */
    private final Supplier<List<List<MealSummary>>> mealPlanSource;

    /**
     * The selected one-based day number, or null when all days were requested.
     */
    private final Integer dayNumber;

    /**
     * Creates a command to display every day of the active plan.
     *
     * @param mealPlanSource A function returning the current plan's meals grouped by day.
     */
    public ViewPlanCommand(Supplier<List<List<MealSummary>>> mealPlanSource) {
        this.mealPlanSource = Objects.requireNonNull(mealPlanSource, "mealPlanSource");
        dayNumber = null;
    }

    /**
     * Creates a command to display one day, checking its upper bound against the plan at execution time.
     *
     * @param mealPlanSource A function returning the current plan's meals grouped by day.
     * @param dayNumber The day to display, starting at 1.
     * @throws IllegalArgumentException If the day number is not positive.
     */
    public ViewPlanCommand(Supplier<List<List<MealSummary>>> mealPlanSource, int dayNumber) {
        this.mealPlanSource = Objects.requireNonNull(mealPlanSource, "mealPlanSource");
        if (dayNumber < 1) {
            throw new IllegalArgumentException("Day number must be a positive integer.");
        }
        this.dayNumber = dayNumber;
    }

    /**
     * Displays the latest supplied plan, checking the selected day against its current size.
     *
     * @param recipeBook The shared command context; viewing does not read or modify recipes.
     * @return The formatted plan or a prompt when no plan is available.
     * @throws MishMashException If the selected day exceeds the active plan's size.
     */
    @Override
    public String execute(RecipeBook recipeBook) throws MishMashException {
        List<List<MealSummary>> mealsByDay = mealPlanSource.get();
        MealPlanViewer viewer = new MealPlanViewer();
        if (dayNumber == null || mealsByDay.isEmpty()) {
            return viewer.view(mealsByDay);
        }
        if (dayNumber > mealsByDay.size()) {
            throw new MishMashException("Day number must be between 1 and " + mealsByDay.size() + "."
                    + System.lineSeparator() + USAGE);
        }
        return viewer.view(mealsByDay, dayNumber);
    }
}
