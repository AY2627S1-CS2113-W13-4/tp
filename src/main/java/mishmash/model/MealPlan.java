package mishmash.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mishmash.ui.MealPlanViewer;
import mishmash.ui.MealPlanViewer.MealSummary;

/**
 * A generated meal plan: one {@link DayPlan} for each day, in order.
 */
public class MealPlan {
    private final List<DayPlan> days;

    /**
     * Creates a meal plan from the given days.
     *
     * @param days The day plans, in order; copied so later changes do not leak in.
     * @throws NullPointerException If the list or any day is null.
     */
    public MealPlan(List<DayPlan> days) {
        Objects.requireNonNull(days, "days");
        this.days = List.copyOf(days);
    }

    /**
     * Returns an unmodifiable list of the plan's days.
     */
    public List<DayPlan> getDays() {
        return days;
    }

    /**
     * Returns how many days the plan covers.
     */
    public int getNumberOfDays() {
        return days.size();
    }

    /**
     * Returns a new plan with one selected planned meal's portion count changed.
     *
     * @param dayIndex The zero-based day position to update.
     * @param mealIndex The zero-based meal position within that day.
     * @param portionCount The new positive whole-recipe portion count.
     * @return A new plan, or this plan when the requested count is already set.
     * @throws IndexOutOfBoundsException If either position does not exist.
     * @throws IllegalArgumentException If the portion count is not positive.
     * @throws ArithmeticException If the changed daily nutrition cannot be represented as long values.
     */
    public MealPlan withAdjustedPortion(int dayIndex, int mealIndex, int portionCount) {
        DayPlan currentDay = days.get(dayIndex);
        DayPlan updatedDay = currentDay.withAdjustedPortion(mealIndex, portionCount);
        if (updatedDay == currentDay) {
            return this;
        }

        List<DayPlan> updatedDays = new ArrayList<>(days);
        updatedDays.set(dayIndex, updatedDay);
        return new MealPlan(updatedDays);
    }

    /**
     * Converts the plan to the display data that {@link MealPlanViewer} expects.
     * The viewer is deliberately independent of how recipes and plans are stored,
     * so this method is the single place that adapts the two.
     *
     * @return The meals of each day, where the first list represents day 1.
     */
    public List<List<MealSummary>> toMealSummaries() {
        List<List<MealSummary>> mealsByDay = new ArrayList<>();
        for (DayPlan day : days) {
            List<MealSummary> summaries = new ArrayList<>();
            for (PlannedMeal meal : day.getPlannedMeals()) {
                summaries.add(new MealSummary(meal.getRecipe().getName(), meal.getPortionCount(),
                        meal.getCalories(), meal.getProtein(), meal.getCarbs(), meal.getFats()));
            }
            mealsByDay.add(summaries);
        }
        return mealsByDay;
    }
}
