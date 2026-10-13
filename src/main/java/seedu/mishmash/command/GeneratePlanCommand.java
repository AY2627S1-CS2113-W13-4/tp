package seedu.mishmash.command;

import java.util.Objects;

import seedu.mishmash.exception.MishMashException;
import seedu.mishmash.logic.MealPlanGenerator;
import seedu.mishmash.model.ActiveMealPlan;
import seedu.mishmash.model.GeneratePlanRequest;
import seedu.mishmash.model.MealPlan;
import seedu.mishmash.model.RecipeBook;

/**
 * Runs a parsed {@code generate-plan} command: generates a plan from the recipe book
 * and makes it the active plan.
 */
public class GeneratePlanCommand implements Command {
    private final GeneratePlanRequest request;
    private final MealPlanGenerator generator;
    private final ActiveMealPlan activePlan;

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
        this.request = Objects.requireNonNull(request, "request");
        this.generator = Objects.requireNonNull(generator, "generator");
        this.activePlan = Objects.requireNonNull(activePlan, "activePlan");
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
            plan = generator.generate(recipeBook.getRecipes(), request.days(), request.targets());
        } catch (IllegalArgumentException e) {
            // The generator reports user-fixable problems this way; show them like any other input error.
            throw new MishMashException(e.getMessage());
        }

        activePlan.set(plan);
        return "Meal plan successfully generated for " + plan.getNumberOfDays() + " day(s)!"
                + System.lineSeparator() + "Use 'view-plan' to inspect your planned meals.";
    }
}
