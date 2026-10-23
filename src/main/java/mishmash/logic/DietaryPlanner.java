package mishmash.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import mishmash.model.DayPlan;
import mishmash.model.DietaryPreferences;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;

/** Applies dietary filtering while retaining the existing generator's nutrition and variety rules. */
public final class DietaryPlanner {
    private final MealPlanGenerator generator;

    public DietaryPlanner(MealPlanGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "generator");
    }

    /** Generates only from eligible recipes; invalid inputs and infeasible requests are exceptions. */
    public MealPlan generate(List<Recipe> recipes, GeneratePlanRequest request,
            DietaryPreferences preferences) {
        Objects.requireNonNull(recipes, "recipes");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(preferences, "preferences");
        List<Recipe> eligible = recipes.stream().filter(preferences::allows).toList();
        if (preferences.isRestricted() && eligible.size() < MealPlanGenerator.MEALS_PER_DAY) {
            throw new IllegalArgumentException("At least " + MealPlanGenerator.MEALS_PER_DAY
                    + " compatible, classified recipes are required; found " + eligible.size()
                    + ". Add or classify more suitable recipes.");
        }
        MealPlan plan = generator.generate(eligible, request.days(), request.targets());
        assert plan.getNumberOfDays() == request.days() : "Generated plan length must match the request";
        assert isCompatible(plan, preferences) : "Every generated recipe must satisfy dietary preferences";
        assert plan.getDays().stream().allMatch(request.targets()::isSatisfiedBy)
                : "Generated days must satisfy the original nutrition targets";
        return plan;
    }

    /** Checks every meal in the plan against the complete dietary profile. */
    public boolean isCompatible(MealPlan plan, DietaryPreferences preferences) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(preferences, "preferences");
        return plan.getDays().stream().allMatch(day -> isCompatible(day, preferences));
    }

    /** A day is compatible only when all its recipes are compatible. */
    private boolean isCompatible(DayPlan day, DietaryPreferences preferences) {
        return day.getMeals().stream().allMatch(preferences::allows);
    }

    /** Preserves compatible days in their original positions and regenerates only conflicting days. */
    public MealPlan repair(List<Recipe> recipes, MealPlan current,
            GeneratePlanRequest originalRequest, DietaryPreferences preferences) {
        Objects.requireNonNull(recipes, "recipes");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(originalRequest, "originalRequest");
        Objects.requireNonNull(preferences, "preferences");
        if (current.getNumberOfDays() != originalRequest.days()) {
            throw new IllegalArgumentException("Plan length must match its generation request.");
        }
        int affectedDays = 0;
        for (DayPlan day : current.getDays()) {
            if (!isCompatible(day, preferences)) {
                affectedDays++;
            }
        }
        if (affectedDays == 0) {
            return current;
        }
        GeneratePlanRequest request = new GeneratePlanRequest(affectedDays, originalRequest.targets());
        Iterator<DayPlan> replacements = generate(recipes, request, preferences).getDays().iterator();
        List<DayPlan> updatedDays = new ArrayList<>();
        for (DayPlan day : current.getDays()) {
            updatedDays.add(isCompatible(day, preferences) ? day : replacements.next());
        }
        assert !replacements.hasNext() : "Each generated day must replace exactly one conflicting day";
        MealPlan updated = new MealPlan(updatedDays);
        assert updated.getNumberOfDays() == current.getNumberOfDays() : "Repair must preserve plan length";
        assert isCompatible(updated, preferences) : "Repaired plans must satisfy dietary preferences";
        return updated;
    }
}
