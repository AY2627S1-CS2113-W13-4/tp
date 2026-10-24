package mishmash.model;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds an immutable declaration of a recipe's supported food categories.
 *
 * <p>An unclassified recipe differs from one explicitly declared to contain no supported categories.
 *
 * @param isClassified Whether all applicable food categories have been declared.
 * @param tags The declared categories; copied into an unmodifiable set.
 */
public record FoodInfo(boolean isClassified, Set<FoodTag> tags) {
    /**
     * Creates a food declaration after copying and validating its categories.
     *
     * @param isClassified Whether all applicable categories have been declared.
     * @param tags The complete declaration of supported categories.
     * @throws NullPointerException If the set or any tag is null.
     * @throws IllegalArgumentException If an unclassified declaration contains tags.
     */
    public FoodInfo {
        tags = Set.copyOf(Objects.requireNonNull(tags, "tags"));
        if (!isClassified && !tags.isEmpty()) {
            throw new IllegalArgumentException("Unclassified recipes cannot declare food tags.");
        }
    }

    /**
     * Creates an unclassified declaration with no known categories.
     *
     * @return A declaration that must be excluded when dietary restrictions are active.
     */
    public static FoodInfo createUnknown() {
        return new FoodInfo(false, Set.of());
    }

    /**
     * Parses an optional recipe food field into an immutable declaration.
     *
     * <p>A missing field remains unclassified. The value {@code none} explicitly declares that none
     * of the supported categories apply; other values are comma-separated food tags.
     *
     * @param value The food field, or null when the optional field is absent.
     * @return The parsed declaration.
     * @throws IllegalArgumentException If a tag is blank, unsupported, or repeated.
     */
    public static FoodInfo parseField(String value) {
        if (value == null) {
            return createUnknown();
        }
        if (value.trim().equalsIgnoreCase("none")) {
            return new FoodInfo(true, Set.of());
        }
        return new FoodInfo(true, FoodTag.parseCsv(value));
    }

    /**
     * Formats the declaration for recipe details in a stable order.
     *
     * @return Sorted category labels, or a message explaining the classification state.
     */
    public String describe() {
        if (!isClassified) {
            return "Unclassified";
        }
        return tags.isEmpty() ? "None declared" : tags.stream().map(FoodTag::getLabel).sorted()
                .collect(Collectors.joining(", "));
    }
}
