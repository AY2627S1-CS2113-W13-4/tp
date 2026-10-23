package mishmash.model;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** A complete declaration of a recipe's supported food categories, or an unknown classification. */
public record FoodInfo(boolean classified, Set<FoodTag> tags) {
    /** Copies tags and rejects contradictory or null declarations. */
    public FoodInfo {
        tags = Set.copyOf(Objects.requireNonNull(tags, "tags"));
        if (!classified && !tags.isEmpty()) {
            throw new IllegalArgumentException("Unclassified recipes cannot declare food tags.");
        }
    }

    /** Represents a recipe whose categories have not been declared. */
    public static FoodInfo unknown() {
        return new FoodInfo(false, Set.of());
    }

    /** Missing fields remain unknown; food/none explicitly declares no supported categories. */
    public static FoodInfo fromField(String value) {
        if (value == null) {
            return unknown();
        }
        if (value.trim().equalsIgnoreCase("none")) {
            return new FoodInfo(true, Set.of());
        }
        return new FoodInfo(true, FoodTag.parseCsv(value));
    }

    /** Formats the classification in a stable order for recipe displays. */
    public String describe() {
        if (!classified) {
            return "Unclassified";
        }
        return tags.isEmpty() ? "None declared" : tags.stream().map(FoodTag::label).sorted()
                .collect(Collectors.joining(", "));
    }
}
