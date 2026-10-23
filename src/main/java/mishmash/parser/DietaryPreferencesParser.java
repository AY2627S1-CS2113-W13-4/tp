package mishmash.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mishmash.model.Diet;
import mishmash.model.DietaryPreferences;
import mishmash.model.FoodTag;

/** Parses dietary preferences without reading or changing application state. */
public final class DietaryPreferencesParser {
    public static final String USAGE = "Format: set-diet [diet/DIET] [avoid/TAG[,TAG...]]"
            + " [exclude/INGREDIENT[,INGREDIENT...]]";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([^\\s/]+)/");

    /** Builds an immutable profile and rejects malformed or unsupported dietary arguments. */
    public DietaryPreferences parse(String arguments) {
        Map<String, String> fields = collectFields(Objects.requireNonNull(arguments, "arguments"));
        Diet diet = fields.containsKey("diet") ? Diet.parse(fields.get("diet")) : Diet.NONE;
        Set<FoodTag> avoided = fields.containsKey("avoid")
                ? FoodTag.parseCsv(fields.get("avoid")) : Set.of();
        Set<String> excluded = new HashSet<>();
        if (fields.containsKey("exclude")) {
            for (String name : fields.get("exclude").split(",", -1)) {
                if (name.isBlank()) {
                    throw new IllegalArgumentException("Excluded ingredient names must not be blank.");
                }
                if (!excluded.add(name)) {
                    throw new IllegalArgumentException("Repeated excluded ingredient: " + name + ".");
                }
            }
        }
        return new DietaryPreferences(diet, avoided, excluded);
    }

    /** Requires supported prefixes and rejects text before the first field. */
    private Map<String, String> collectFields(String arguments) {
        Matcher matcher = PREFIX_PATTERN.matcher(arguments);
        if (!matcher.find() || !arguments.substring(0, matcher.start()).isBlank()) {
            throw new IllegalArgumentException("Supply at least one diet/, avoid/, or exclude/ field.");
        }
        Map<String, String> fields = new HashMap<>();
        String prefix = matcher.group(1);
        int valueStart = matcher.end();
        while (matcher.find()) {
            readField(fields, prefix, arguments.substring(valueStart, matcher.start()).trim());
            prefix = matcher.group(1);
            valueStart = matcher.end();
        }
        readField(fields, prefix, arguments.substring(valueStart).trim());
        return fields;
    }

    /** Validates one supported field and rejects repeated prefixes. */
    private void readField(Map<String, String> fields, String prefix, String value) {
        if (!Set.of("diet", "avoid", "exclude").contains(prefix)) {
            throw new IllegalArgumentException("Unknown dietary field: " + prefix + "/");
        }
        if (value.isBlank() || value.contains("/")) {
            throw new IllegalArgumentException("Invalid value for " + prefix + "/");
        }
        if (fields.putIfAbsent(prefix, value) != null) {
            throw new IllegalArgumentException("Repeated field: " + prefix + "/");
        }
    }
}
