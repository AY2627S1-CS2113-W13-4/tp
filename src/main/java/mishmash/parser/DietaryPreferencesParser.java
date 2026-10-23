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
            String problem = arguments.isBlank() ? "Missing dietary input."
                    : "Invalid dietary input '" + arguments.trim() + "'.";
            throw new IllegalArgumentException(problem + System.lineSeparator()
                    + "Use diet/, avoid/, or exclude/ before each value." + System.lineSeparator()
                    + Diet.inputHelp() + System.lineSeparator() + "Example: set-diet diet/vegan");
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
            throw new IllegalArgumentException("Invalid dietary field '" + prefix + "/'."
                    + System.lineSeparator() + "Valid fields: diet/, avoid/, exclude/."
                    + System.lineSeparator() + Diet.inputHelp());
        }
        if (value.isBlank() || value.contains("/")) {
            throw new IllegalArgumentException("Invalid value for " + prefix + "/."
                    + System.lineSeparator() + validValues(prefix));
        }
        if (fields.putIfAbsent(prefix, value) != null) {
            throw new IllegalArgumentException("Repeated field: " + prefix + "/");
        }
    }

    /** Explains the values accepted by the field that failed validation. */
    private String validValues(String prefix) {
        return switch (prefix) {
        case "diet" -> Diet.inputHelp();
        case "avoid" -> "Valid food tags: " + FoodTag.supportedLabels() + ".";
        default -> "Provide one or more ingredient names, separated by commas.";
        };
    }
}
