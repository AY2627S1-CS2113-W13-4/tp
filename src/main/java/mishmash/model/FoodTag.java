package mishmash.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Food categories declared on recipes, including categories used for allergies. */
public enum FoodTag {
    MILK, LACTOSE, EGG, PEANUT, TREE_NUT, SOY, WHEAT, GLUTEN,
    FISH, CRUSTACEAN, MOLLUSC, SESAME, MUSTARD, CELERY, LUPIN,
    SULPHITE, BEEF, PORK, POULTRY, OTHER_MEAT, MEAT_DERIVATIVE,
    GELATIN, HONEY, OTHER_ANIMAL_PRODUCT, ALCOHOL, COCONUT,
    CORN, OAT, LEGUME, ALLIUM, MUSHROOM;

    /** Returns the command-line spelling of this category. */
    public String label() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Parses case-insensitive labels and common aliases without guessing unknown foods. */
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
                    + System.lineSeparator() + "Valid food tags: " + supportedLabels() + ".", e);
        }
    }

    /** Rejects blank entries, unknown tags, and duplicates, including duplicate aliases. */
    public static Set<FoodTag> parseCsv(String text) {
        Objects.requireNonNull(text, "text");
        Set<FoodTag> result = EnumSet.noneOf(FoodTag.class);
        for (String entry : text.split(",", -1)) {
            if (entry.isBlank()) {
                throw new IllegalArgumentException("Food tags must not be blank."
                        + System.lineSeparator() + "Valid food tags: " + supportedLabels() + ".");
            }
            FoodTag tag = parse(entry);
            if (!result.add(tag)) {
                throw new IllegalArgumentException("Repeated food tag: " + tag.label() + ".");
            }
        }
        return Set.copyOf(result);
    }

    /** Lists every supported category for command help. */
    public static String supportedLabels() {
        return Arrays.stream(values()).map(FoodTag::label).collect(Collectors.joining(", "));
    }
}
