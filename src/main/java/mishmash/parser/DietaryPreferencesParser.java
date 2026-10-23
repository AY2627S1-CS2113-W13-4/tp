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

/**
 * Parses dietary preferences without reading or changing application state.
 */
public final class DietaryPreferencesParser {
    /** Supported set-diet syntax shown after recoverable input errors. */
    public static final String USAGE = "Format: set-diet [diet/DIET] [avoid/TAG[,TAG...]]"
            + " [exclude/INGREDIENT[,INGREDIENT...]]";
    /** Matches supported and unsupported prefixes so malformed fields cannot become values. */
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([^\\s/]+)/");

    /**
     * Parses dietary fields into an immutable replacement profile without changing session state.
     *
     * <p>Each supported prefix may appear once; omitted fields become unrestricted defaults.
     *
     * @param arguments The text following {@code set-diet}.
     * @return The complete proposed dietary profile.
     * @throws NullPointerException If the arguments are null.
     * @throws IllegalArgumentException If syntax, diet values, categories, or exclusions are invalid.
     */
    public DietaryPreferences parse(String arguments) {
        Map<String, String> fields = collectFields(Objects.requireNonNull(arguments, "arguments"));
        Diet diet = fields.containsKey("diet") ? Diet.parse(fields.get("diet")) : Diet.NONE;
        Set<FoodTag> avoidedTags = fields.containsKey("avoid")
                ? FoodTag.parseCsv(fields.get("avoid")) : Set.of();
        Set<String> excludedIngredients = new HashSet<>();
        if (fields.containsKey("exclude")) {
            for (String name : fields.get("exclude").split(",", -1)) {
                if (name.isBlank()) {
                    throw new IllegalArgumentException("Excluded ingredient names must not be blank.");
                }
                if (!excludedIngredients.add(name)) {
                    throw new IllegalArgumentException("Repeated excluded ingredient: " + name + ".");
                }
            }
        }
        return new DietaryPreferences(diet, avoidedTags, excludedIngredients);
    }

    /**
     * Collects supported prefixed fields and rejects text before the first field.
     *
     * @param arguments The non-null command arguments.
     * @return A map containing each supplied prefix once.
     * @throws IllegalArgumentException If fields are missing, unsupported, duplicated, or malformed.
     */
    private Map<String, String> collectFields(String arguments) {
        Matcher matcher = PREFIX_PATTERN.matcher(arguments);
        if (!matcher.find() || !arguments.substring(0, matcher.start()).isBlank()) {
            String problem = arguments.isBlank() ? "Missing dietary input."
                    : "Invalid dietary input '" + arguments.trim() + "'.";
            throw new IllegalArgumentException(problem + System.lineSeparator()
                    + "Use diet/, avoid/, or exclude/ before each value." + System.lineSeparator()
                    + Diet.getInputHelp() + System.lineSeparator() + "Example: set-diet diet/vegan");
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

    /**
     * Validates and stores one prefixed dietary field.
     *
     * @param fields The destination map containing previously validated fields.
     * @param prefix The field prefix without its slash.
     * @param value The trimmed field value.
     * @throws IllegalArgumentException If the prefix or value is invalid, or the prefix is repeated.
     */
    private void readField(Map<String, String> fields, String prefix, String value) {
        if (!Set.of("diet", "avoid", "exclude").contains(prefix)) {
            throw new IllegalArgumentException("Invalid dietary field '" + prefix + "/'."
                    + System.lineSeparator() + "Valid fields: diet/, avoid/, exclude/."
                    + System.lineSeparator() + Diet.getInputHelp());
        }
        if (value.isBlank() || value.contains("/")) {
            throw new IllegalArgumentException("Invalid value for " + prefix + "/."
                    + System.lineSeparator() + getValidValues(prefix));
        }
        if (fields.putIfAbsent(prefix, value) != null) {
            throw new IllegalArgumentException("Repeated field: " + prefix + "/");
        }
    }

    /**
     * Returns value guidance for a field that failed validation.
     *
     * @param prefix A supported dietary field prefix.
     * @return Supported diet values, supported food tags, or ingredient-name guidance.
     */
    private String getValidValues(String prefix) {
        return switch (prefix) {
            case "diet" -> Diet.getInputHelp();
            case "avoid" -> "Valid food tags: " + FoodTag.getSupportedLabels() + ".";
            default -> "Provide one or more ingredient names, separated by commas.";
        };
    }
}
