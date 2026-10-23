package mishmash.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import mishmash.logic.DietaryPlanner;
import mishmash.logic.GroceryListGenerator;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.DietaryPreferences;
import mishmash.model.DietarySettings;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.RecipeBook;

/** Saves dietary requirements, repairs conflicting days, and refreshes existing groceries. */
public final class SetDietCommand implements Command {
    private final DietaryPreferences preferences;
    private final DietarySettings settings;
    private final ActiveMealPlan activePlan;
    private final DietaryPlanner planner;

    /** Connects an immutable proposed profile to the shared session and generation dependencies. */
    public SetDietCommand(DietaryPreferences preferences, DietarySettings settings,
            ActiveMealPlan activePlan, MealPlanGenerator generator) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.activePlan = Objects.requireNonNull(activePlan, "activePlan");
        this.planner = new DietaryPlanner(generator);
    }

    @Override
    public String execute(RecipeBook recipeBook) {
        Objects.requireNonNull(recipeBook, "recipeBook");
        Optional<MealPlan> current = activePlan.get();
        if (current.isEmpty()) {
            activePlan.clear();
            settings.set(preferences);
            return "Dietary preferences saved. Future meal plans will use them.";
        }
        if (planner.isCompatible(current.get(), preferences)) {
            settings.set(preferences);
            return "Dietary preferences saved. Your existing meal plan already matches.";
        }
        try {
            GeneratePlanRequest request = activePlan.getRequest().orElseThrow(
                    () -> new IllegalArgumentException("The original meal-plan settings are unavailable."));
            MealPlan updated = planner.repair(recipeBook.getRecipes(), current.get(), request, preferences);
            Optional<List<GroceryItem>> groceries = Optional.empty();
            if (activePlan.getGroceryList().isPresent()) {
                groceries = Optional.of(new GroceryListGenerator().generateForPlan(updated));
            }
            activePlan.setGenerated(updated, request, groceries);
            settings.set(preferences);
            return "Dietary preferences saved. Conflicting days were replaced."
                    + System.lineSeparator() + (groceries.isPresent()
                    ? "Your grocery list has also been updated."
                    : "Use 'generate-list' when you need a grocery list.");
        } catch (IllegalArgumentException e) {
            // Never leave an incompatible plan visible under newly saved restrictions.
            settings.set(preferences);
            activePlan.clear();
            return "Dietary preferences saved, but a replacement plan could not be generated."
                    + System.lineSeparator() + e.getMessage() + System.lineSeparator()
                    + "The previous meal plan and grocery list have been cleared."
                    + System.lineSeparator()
                    + "Add suitable recipes or adjust your targets, then use 'generate-plan'.";
        }
    }
}
