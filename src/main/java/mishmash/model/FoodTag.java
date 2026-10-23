package mishmash.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Food categories declared on recipes, including categories used for allergies.
 */
public enum FoodTag {
    MILK,
    /**
     * Lactose content, declared separately from the milk-source category.
     */
    LACTOSE,
    EGG,
    PEANUT,
    TREE_NUT,
    SOY,
    /**
     * Wheat content; also declare gluten separately when it is present.
     */
    WHEAT,
    GLUTEN,
    FISH,
    CRUSTACEAN,
    MOLLUSC,
    SESAME,
    MUSTARD,
    CELERY,
    LUPIN,
    SULPHITE,
    BEEF,
    PORK,
    POULTRY,
    /**
     * Land-animal meat not covered by beef, pork, or poultry.
     */
    OTHER_MEAT,
    /**
     * Land-animal meat derivatives such as stock or lard; also declare the source category.
     */
    MEAT_DERIVATIVE,
    /**
     * Gelatin content; also declare its source category when known.
     */
    GELATIN,
    HONEY,
    /**
     * Animal-derived ingredients without a more specific supported category.
     */
    OTHER_ANIMAL_PRODUCT,
    ALCOHOL,
    COCONUT,
    CORN,
    OAT,
    /**
     * The broad legume category; declare it alongside specific categories such as soy when applicable.
     */
    LEGUME,
    ALLIUM,
    MUSHROOM;

    /**
     * Returns the lowercase, hyphen-separated spelling accepted in commands.
     *
     * @return The canonical command-line category label.
     */
    public String getLabel() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Parses a supported food category or alias without inferring unknown foods.
     *
     * <p>Matching ignores case and surrounding spaces and accepts hyphens or underscores.
     *
     * @param text The category label or alias supplied by the caller.
     * @return The corresponding category.
     * @throws NullPointerException If the text is null.
     * @throws IllegalArgumentException If the label is unsupported or blank.
     */
    public static FoodTag parse(String text) {
        String name = Objects.requireNonNull(text, "text").trim()
                .toUpperCase(Locale.ROOT).replace('-', '_');
        name = switch (name) {
            case "DAIRY" -> "MILK";
            case "SOYA" -> "SOY";
            case "SULFITE" -> "SULPHITE";
            case "EGGS" -> "EGG";
            case "PEANUTS" -> "PEANUT";
            case "TREE_NUTS" -> "TREE_NUT";
            case "CRUSTACEANS" -> "CRUSTACEAN";
            case "MOLLUSCS" -> "MOLLUSC";
            default -> name;
        };
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid food tag '" + text + "'."
                    + System.lineSeparator() + "Valid food tags: " + getSupportedLabels() + ".", e);
        }
    }

    /**
     * Parses a comma-separated list of food categories into an unmodifiable set.
     *
     * <p>Aliases are resolved before duplicate detection, so {@code milk,dairy} is rejected.
     *
     * @param text The comma-separated category labels.
     * @return A nonempty set of distinct supported categories.
     * @throws NullPointerException If the text is null.
     * @throws IllegalArgumentException If an entry is blank, unsupported, or repeated.
     */
    public static Set<FoodTag> parseCsv(String text) {
        Objects.requireNonNull(text, "text");
        Set<FoodTag> parsedTags = EnumSet.noneOf(FoodTag.class);
        for (String entry : text.split(",", -1)) {
            if (entry.isBlank()) {
                throw new IllegalArgumentException("Food tags must not be blank."
                        + System.lineSeparator() + "Valid food tags: " + getSupportedLabels() + ".");
            }
            FoodTag tag = parse(entry);
            if (!parsedTags.add(tag)) {
                throw new IllegalArgumentException("Repeated food tag: " + tag.getLabel() + ".");
            }
        }
        return Set.copyOf(parsedTags);
    }

    /**
     * Returns all canonical category labels for help and validation errors.
     *
     * @return Comma-separated labels in enum declaration order.
     */
    public static String getSupportedLabels() {
        return Arrays.stream(values()).map(FoodTag::getLabel).collect(Collectors.joining(", "));
    }
}
