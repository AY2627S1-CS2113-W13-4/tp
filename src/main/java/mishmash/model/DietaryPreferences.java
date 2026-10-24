package mishmash.model;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Holds immutable dietary requirements that apply together to each selected recipe.
 *
 * <p>Ingredient exclusions use complete names after case and whitespace normalization.
 *
 * @param diet The named diet, or {@link Diet#NONE} for no named-diet restriction.
 * @param avoidedTags Additional excluded food categories.
 * @param excludedIngredients Additional excluded ingredient names.
 */
public record DietaryPreferences(Diet diet, Set<FoodTag> avoidedTags, Set<String> excludedIngredients) {
    /**
     * Creates a profile with independent, unmodifiable exclusion sets.
     *
     * @param diet The named diet applied alongside explicit exclusions.
     * @param avoidedTags The additional categories to exclude.
     * @param excludedIngredients The complete ingredient names to normalize and exclude.
     * @throws NullPointerException If any argument or collection element is null.
     * @throws IllegalArgumentException If an ingredient name is blank or repeated after normalization.
     */
    public DietaryPreferences {
        Objects.requireNonNull(diet, "diet");
        avoidedTags = Set.copyOf(Objects.requireNonNull(avoidedTags, "avoidedTags"));
        Objects.requireNonNull(excludedIngredients, "excludedIngredients");
        Set<String> normalizedIngredients = new HashSet<>();
        for (String ingredient : excludedIngredients) {
            String name = normalizeIngredient(ingredient);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Excluded ingredient names must not be blank.");
            }
            if (!normalizedIngredients.add(name)) {
                throw new IllegalArgumentException("Repeated excluded ingredient: " + name + ".");
            }
        }
        excludedIngredients = Set.copyOf(normalizedIngredients);
    }

    /**
     * Creates a profile without named-diet, category, or ingredient restrictions.
     *
     * @return A profile that also permits unclassified recipes.
     */
    public static DietaryPreferences createUnrestricted() {
        return new DietaryPreferences(Diet.NONE, Set.of(), Set.of());
    }

    /**
     * Checks whether any dietary requirement is active.
     *
     * @return True if a named diet or at least one explicit exclusion is present.
     */
    public boolean isRestricted() {
        return diet != Diet.NONE || !avoidedTags.isEmpty() || !excludedIngredients.isEmpty();
    }

    /**
     * Checks a recipe against every active requirement.
     *
     * <p>Unclassified recipes are rejected whenever any restriction is active. Classification is based
     * on declared categories; ingredient names are not used to infer missing food tags.
     *
     * @param recipe The recipe to evaluate without modifying it.
     * @return True if the recipe satisfies the complete profile.
     * @throws NullPointerException If the recipe is null.
     */
    public boolean isAllowed(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (!isRestricted()) {
            return true;
        }
        FoodInfo foodInfo = recipe.getFoodInfo();
        if (!foodInfo.isClassified()) {
            return false;
        }
        for (FoodTag tag : foodInfo.tags()) {
            if (diet.isExcluded(tag) || avoidedTags.contains(tag)) {
                return false;
            }
        }
        return recipe.getIngredients().stream()
                .noneMatch(item -> excludedIngredients.contains(normalizeIngredient(item.getName())));
    }

    /**
     * Normalizes a complete ingredient name for exact matching.
     *
     * @param name The ingredient name to normalize.
     * @return The lowercase name with trimmed and collapsed whitespace.
     * @throws NullPointerException If the name is null.
     */
    private static String normalizeIngredient(String name) {
        return Objects.requireNonNull(name, "ingredient").trim()
                .toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

}
