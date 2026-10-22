package mishmash.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mishmash.model.NutritionTargets;

/**
 * Parses explicit daily targets for check-shortfall without modifying the targets used for generation.
 */
public class CheckShortfallParser {
    private static final List<String> PREFIXES = List.of("cal", "p", "c", "f");

    /**
     * Reads one or more targets in any order; omitted nutrients are not checked.
     *
     * @param arguments Text after check-shortfall, such as {@code "cal/2000 p/100"}.
     * @return Validated positive integer targets.
     * @throws IllegalArgumentException If no targets are given, or any argument is invalid or repeated.
     */
    public NutritionTargets parse(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            throw new IllegalArgumentException("Supply at least one daily nutrition target.");
        }
        Map<String, Integer> values = new HashMap<>();
        for (String token : arguments.trim().split("\\s+")) {
            int separator = token.indexOf('/');
            String prefix = separator < 0 ? "" : token.substring(0, separator);
            if (!PREFIXES.contains(prefix)) {
                throw new IllegalArgumentException("Unrecognised parameter: " + token);
            }
            if (values.containsKey(prefix)) {
                throw new IllegalArgumentException("Parameter " + prefix + "/ was given more than once.");
            }
            values.put(prefix, parseTarget(token.substring(separator + 1), prefix));
        }
        return new NutritionTargets(values.get("cal"), values.get("p"), values.get("c"), values.get("f"));
    }

    /** Rejects signs, decimals, missing values, zero, and values beyond the supported integer range. */
    private int parseTarget(String value, String prefix) {
        String error = "Target for " + prefix + "/ must be a positive whole number from 1 to "
                + Integer.MAX_VALUE + ".";
        if (!value.matches("[0-9]+")) {
            throw new IllegalArgumentException(error);
        }
        try {
            int target = Integer.parseInt(value);
            if (target < 1) {
                throw new IllegalArgumentException(error);
            }
            return target;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(error, e);
        }
    }
}
