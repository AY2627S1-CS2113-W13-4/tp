package mishmash.command;

import java.util.Objects;
import java.util.Optional;

import mishmash.exception.MishMashException;
import mishmash.logic.DietaryPlanner;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.DietarySettings;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.MealPlan;
import mishmash.model.RecipeBook;

/**
 * Runs a parsed {@code generate-plan} command: generates a plan from the recipe book
 * and makes it the active plan.
 */
public class GeneratePlanCommand implements Command {
    private final GeneratePlanRequest request;
    private final MealPlanGenerator generator;
    private final ActiveMealPlan activePlan;
    private final DietarySettings dietarySettings;

    /**
     * Creates a command for the given request.
     *
     * @param request The validated command arguments.
     * @param generator The generator used to pick the meals.
     * @param activePlan Where the generated plan is stored, shared with other commands such as {@code view-plan}.
     * @throws NullPointerException If any argument is null.
     */
    public GeneratePlanCommand(GeneratePlanRequest request, MealPlanGenerator generator,
            ActiveMealPlan activePlan) {
        this(request, generator, activePlan, new DietarySettings());
    }

    /** Creates a generation command sharing the current session's dietary requirements. */
    public GeneratePlanCommand(GeneratePlanRequest request, MealPlanGenerator generator,
            ActiveMealPlan activePlan, DietarySettings dietarySettings) {
        this.request = Objects.requireNonNull(request, "request");
        this.generator = Objects.requireNonNull(generator, "generator");
        this.activePlan = Objects.requireNonNull(activePlan, "activePlan");
        this.dietarySettings = Objects.requireNonNull(dietarySettings, "dietarySettings");
    }

    /**
     * Generates the plan from the recipes in the book and stores it as the active plan.
     * If generation fails, the previous active plan is left unchanged.
     * The plan itself is not printed; the confirmation points the user to {@code view-plan}.
     *
     * @param recipeBook The recipe book to choose meals from.
     * @return The confirmation naming the number of days and pointing to {@code view-plan}.
     * @throws NullPointerException If the recipe book is null.
     * @throws MishMashException If the plan cannot be generated, such as when there are too few recipes
     *         or no recipes meet the targets.
     */
    @Override
    public String execute(RecipeBook recipeBook) throws MishMashException {
        Objects.requireNonNull(recipeBook, "recipeBook");

        MealPlan plan;
        try {
            plan = new DietaryPlanner(generator).generate(recipeBook.getRecipes(), request, dietarySettings.get());
        } catch (IllegalArgumentException e) {
            // The generator reports user-fixable problems this way; show them like any other input error.
            throw new MishMashException(e.getMessage(), e);
        }

        activePlan.setGenerated(plan, request, Optional.empty());
        return "Meal plan successfully generated for " + plan.getNumberOfDays() + " day(s)!"
                + System.lineSeparator() + "Use 'view-plan' to inspect your planned meals.";
    }
}
