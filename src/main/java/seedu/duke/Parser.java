package seedu.duke;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts raw user input into a validated command.
 */
public class Parser {
    /**
     * Describes the required recipe fields and repeated ingredient syntax.
     */
    public static final String ADD_RECIPE_USAGE =
        "Format: add-recipe n/NAME cal/CALORIES p/PROTEIN c/CARBS f/FATS"
                + " i/INGREDIENT:QUANTITY_UNIT [i/INGREDIENT:QUANTITY_UNIT ...]";

    private static final String ADD_RECIPE_COMMAND = "add-recipe";
    private static final List<String> REQUIRED_FIELDS = List.of("n", "cal", "p", "c", "f");

    // Prefixes must occur at the start of the arguments or after whitespace.
    private static final Pattern PREFIX_PATTERN =
        Pattern.compile("(?<!\\S)([A-Za-z][A-Za-z0-9_-]*)/");

    // Ingredient quantities may be decimal, while units contain letters.
    private static final Pattern INGREDIENT_PATTERN =
        Pattern.compile("([^:]+):\\s*([0-9]+(?:\\.[0-9]+)?)\\s*([A-Za-z]+)");

    /**
     * Parses input without modifying the recipe book.
     *
     * @param input The raw command entered by the user.
     * @return The validated command.
     * @throws MishMashException If the command or its arguments are invalid.
     */
    public Command parseCommand(String input) throws MishMashException {
        if (input == null || input.isBlank()) {
            throw new MishMashException("Enter a command.");
        }

        String[] parts = input.trim().split("\\s+", 2);
        if (!parts[0].equals(ADD_RECIPE_COMMAND)) {
            throw new MishMashException("Unknown command: " + parts[0]);
        }

        String arguments = parts.length == 2 ? parts[1] : "";
        try {
            return parseAddRecipe(arguments);
        } catch (IllegalArgumentException e) {
            throw new MishMashException(e.getMessage()
                    + System.lineSeparator() + ADD_RECIPE_USAGE);
        }
    }

    /**
     * Collects the arguments and creates a command after all fields are valid.
     *
     * @param arguments The arguments following the command word.
     * @return The command containing the validated recipe.
     */
    private Command parseAddRecipe(String arguments) {
        Map<String, String> fields = new HashMap<>();
        List<GroceryItem> ingredients = new ArrayList<>();
        Matcher matcher = PREFIX_PATTERN.matcher(arguments);

        if (!matcher.find() || !arguments.substring(0, matcher.start()).isBlank()) {
            throw new IllegalArgumentException("Expected prefixed recipe arguments.");
        }

        String prefix = matcher.group(1);
        int valueStart = matcher.end();

        while (matcher.find()) {
            readArgument(prefix, arguments.substring(valueStart, matcher.start()).trim(),
                    fields, ingredients);
            prefix = matcher.group(1);
            valueStart = matcher.end();
        }
        readArgument(prefix, arguments.substring(valueStart).trim(), fields, ingredients);

        for (String required : REQUIRED_FIELDS) {
            if (!fields.containsKey(required)) {
                throw new IllegalArgumentException("Missing required field: " + required + "/");
            }
        }

        Recipe recipe = new Recipe(fields.get("n"),
            parseNutrition(fields.get("cal"), "Calories"),
            parseNutrition(fields.get("p"), "Protein"),
            parseNutrition(fields.get("c"), "Carbs"),
            parseNutrition(fields.get("f"), "Fats"),
            ingredients);
        return new AddRecipeCommand(recipe);
    }

    /**
     * Accepts repeated ingredients and rejects repeated single-value fields.
     *
     * @param prefix The argument prefix without its slash.
     * @param value The trimmed argument value.
     * @param fields The single-value fields collected so far.
     * @param ingredients The ingredients collected so far.
     */
    private void readArgument(String prefix, String value, Map<String, String> fields,
             List<GroceryItem> ingredients) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("Value must not be blank: " + prefix + "/");
        }
        if (prefix.equals("i")) {
            ingredients.add(parseIngredient(value));
            return;
        }
        if (!REQUIRED_FIELDS.contains(prefix)) {
            throw new IllegalArgumentException("Unknown field: " + prefix + "/");
        }
        if (fields.putIfAbsent(prefix, value) != null) {
            throw new IllegalArgumentException("Repeated field: " + prefix + "/");
        }
    }

    /**
     * Converts a nutrition field to a non-negative integer.
     *
     * @param value The nutrition value to parse.
     * @param fieldName The field name to use in error messages.
     * @return The parsed nutrition value.
     */
    private int parseNutrition(String value, String fieldName) {
        if (!value.matches("[0-9]+")) {
            throw new IllegalArgumentException(fieldName + " must be a non-negative integer.");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must not exceed "
                    + Integer.MAX_VALUE + ".");
        }
    }

    /**
     * Parses an ingredient such as Chicken Breast:200g.
     *
     * @param value The ingredient name, quantity, and unit.
     * @return The parsed ingredient.
     */
    private GroceryItem parseIngredient(String value) {
        Matcher matcher = INGREDIENT_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Ingredient must use NAME:QUANTITY_UNIT, for example Chicken Breast:200g.");
        }

        BigDecimal quantity = new BigDecimal(matcher.group(2));
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Ingredient quantity must be greater than zero.");
        }

        return new GroceryItem(matcher.group(1), quantity, matcher.group(3));
    }
}

