package mishmash.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Immutable dietary requirements that must all be satisfied by a selected recipe. */
public record DietaryPreferences(Diet diet, Set<FoodTag> avoidedTags, Set<String> excludedIngredients) {
    /** Copies collections, normalizes ingredient names, and rejects invalid exclusions. */
    public DietaryPreferences {
        Objects.requireNonNull(diet, "diet");
        avoidedTags = Set.copyOf(Objects.requireNonNull(avoidedTags, "avoidedTags"));
        Objects.requireNonNull(excludedIngredients, "excludedIngredients");
        Set<String> normalized = new HashSet<>();
        for (String ingredient : excludedIngredients) {
            String name = normalizeIngredient(ingredient);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Excluded ingredient names must not be blank.");
            }
            if (!normalized.add(name)) {
                throw new IllegalArgumentException("Repeated excluded ingredient: " + name + ".");
            }
        }
        excludedIngredients = Set.copyOf(normalized);
    }

    /** Creates a profile with no dietary restrictions. */
    public static DietaryPreferences none() {
        return new DietaryPreferences(Diet.NONE, Set.of(), Set.of());
    }

    /** Reports whether any category, ingredient, or named-diet restriction is active. */
    public boolean isRestricted() {
        return diet != Diet.NONE || !avoidedTags.isEmpty() || !excludedIngredients.isEmpty();
    }

    /** Unclassified recipes are allowed only when no dietary restrictions are active. */
    public boolean allows(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (!isRestricted()) {
            return true;
        }
        FoodInfo info = recipe.getFoodInfo();
        if (!info.classified()) {
            return false;
        }
        for (FoodTag tag : info.tags()) {
            if (diet.excludes(tag) || avoidedTags.contains(tag)) {
                return false;
            }
        }
        return recipe.getIngredients().stream().noneMatch(item ->
                excludedIngredients.contains(normalizeIngredient(item.getName())));
    }

    /** Normalizes complete ingredient names without applying substring or synonym matching. */
    private static String normalizeIngredient(String name) {
        return Objects.requireNonNull(name, "ingredient").trim()
                .toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    /** Formats the current profile in a stable order for view-diet. */
    public String describe() {
        return String.join(System.lineSeparator(),
                "Diet: " + diet.label(),
                "Avoided food categories: " + display(avoidedTags.stream().map(FoodTag::label).toList()),
                "Excluded ingredients: " + display(excludedIngredients));
    }

    /** Formats a sorted exclusion list with an explicit empty-state label. */
    private static String display(Collection<String> values) {
        return values.isEmpty() ? "none" : values.stream().sorted().collect(Collectors.joining(", "));
    }
}
