package mishmash.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Named diets expressed as exclusions; MEAT_DERIVATIVE means land-animal meat derivatives.
 */
public enum Diet {
    /**
     * Applies no exclusions from a named diet.
     */
    NONE(),
    /**
     * Excludes land-animal meat and unspecified animal products while allowing seafood.
     */
    PESCATARIAN(FoodTag.BEEF, FoodTag.PORK, FoodTag.POULTRY,
            FoodTag.OTHER_MEAT, FoodTag.MEAT_DERIVATIVE, FoodTag.OTHER_ANIMAL_PRODUCT),
    /**
     * Also excludes seafood and gelatin while allowing dairy, eggs, and honey.
     */
    VEGETARIAN(PESCATARIAN, FoodTag.FISH, FoodTag.CRUSTACEAN, FoodTag.MOLLUSC, FoodTag.GELATIN),
    /**
     * Extends vegetarian exclusions to eggs.
     */
    LACTO_VEGETARIAN(VEGETARIAN, FoodTag.EGG),
    /**
     * Extends vegetarian exclusions to milk and lactose.
     */
    OVO_VEGETARIAN(VEGETARIAN, FoodTag.MILK, FoodTag.LACTOSE),
    /**
     * Extends vegetarian exclusions to milk, lactose, eggs, and honey.
     */
    VEGAN(VEGETARIAN, FoodTag.MILK, FoodTag.LACTOSE, FoodTag.EGG, FoodTag.HONEY);

    private final Set<FoodTag> excludedTags;

    /**
     * Creates a named diet from an explicit set of excluded categories.
     *
     * @param tags The categories excluded by this diet.
     */
    Diet(FoodTag... tags) {
        excludedTags = Set.of(tags);
    }

    /**
     * Creates a named diet by extending another diet's exclusions.
     *
     * @param base The diet whose exclusions are inherited.
     * @param additionalTags The extra categories excluded by the new diet.
     */
    Diet(Diet base, FoodTag... additionalTags) {
        Set<FoodTag> combinedTags = EnumSet.noneOf(FoodTag.class);
        combinedTags.addAll(base.excludedTags);
        for (FoodTag tag : additionalTags) {
            combinedTags.add(tag);
        }
        excludedTags = Set.copyOf(combinedTags);
    }

    /**
     * Checks whether this named diet excludes a category.
     *
     * @param tag The category to check.
     * @return True if the category conflicts with this diet.
     * @throws NullPointerException If the tag is null.
     */
    public boolean isExcluded(FoodTag tag) {
        return excludedTags.contains(Objects.requireNonNull(tag, "tag"));
    }

    /**
     * Returns the canonical lowercase, hyphen-separated diet label.
     *
     * @return The diet value accepted by commands.
     */
    public String getLabel() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Returns the canonical labels of every supported named diet.
     *
     * @return Comma-separated labels in enum declaration order.
     */
    public static String getSupportedLabels() {
        return Arrays.stream(values()).map(Diet::getLabel).collect(Collectors.joining(", "));
    }

    /**
     * Returns valid diet values and aliases for help and validation errors.
     *
     * @return A multiline explanation of supported diet input.
     */
    public static String getInputHelp() {
        return "Valid diets: " + getSupportedLabels() + "." + System.lineSeparator()
                + "Aliases: omnivore (none), lacto-ovo-vegetarian (vegetarian).";
    }

    /**
     * Parses a case-insensitive diet label or supported alias.
     *
     * @param text The diet label or alias supplied by the caller.
     * @return The matching named diet.
     * @throws NullPointerException If the text is null.
     * @throws IllegalArgumentException If the label is unsupported or blank.
     */
    public static Diet parse(String text) {
        String name = Objects.requireNonNull(text, "text").trim()
                .toUpperCase(Locale.ROOT).replace('-', '_');
        if (name.equals("OMNIVORE")) {
            return NONE;
        }
        if (name.equals("LACTO_OVO_VEGETARIAN")) {
            return VEGETARIAN;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid diet '" + text + "'."
                    + System.lineSeparator() + getInputHelp(), e);
        }
    }
}
