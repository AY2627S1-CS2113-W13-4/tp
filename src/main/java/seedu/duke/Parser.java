package seedu.duke;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.duke.MealPlanViewer.MealSummary;

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

    public static final String VIEW_PLAN_USAGE = ViewPlanCommand.USAGE;
    public static final String VIEW_LIST_USAGE = ViewListCommand.USAGE;

    /**
     * Lists supported syntax for empty or unknown input.
     */
    public static final String COMMAND_USAGE = String.join(System.lineSeparator(),
            ADD_RECIPE_USAGE, VIEW_PLAN_USAGE, VIEW_LIST_USAGE);

    private static final String COMMAND_ADD_RECIPE = "add-recipe";
    private static final String COMMAND_VIEW_PLAN = "view-plan";
    private static final String COMMAND_VIEW_LIST = "view-list";
    private static final List<String> REQUIRED_FIELDS = List.of("n", "cal", "p", "c", "f");

    // Recognize unknown prefixes too, so they cannot become part of a name.
    private static final Pattern PREFIX_PATTERN =
            Pattern.compile("(?<!\\S)([^\\s/]+)/");

    // Ingredient quantities may be decimal, while units contain letters.
    private static final Pattern INGREDIENT_PATTERN =
            Pattern.compile("([^:]+):\\s*([0-9]+(?:\\.[0-9]+)?)\\s*([A-Za-z]+)");

    private static final Pattern DAY_PATTERN = Pattern.compile("d/\\s*([0-9]+)");

    /**
     * Supplies the active plan at command execution, allowing generation to connect its own data later.
     */
    private final Supplier<List<List<MealSummary>>> mealPlanSource;

    /**
     * Supplies the generated list without making viewing responsible for generation or storage.
     */
    private final Supplier<Optional<List<GroceryItem>>> groceryListSource;

    /**
     * Creates a parser with no generated data connected yet.
     */
    public Parser() {
        this(() -> List.of(), () -> Optional.empty());
    }

    /**
     * Connects viewing commands to the caller's existing data through functions called during execution.
     * An empty plan means no active plan; an empty Optional means no grocery list has been generated.
     *
     * @param mealPlanSource A function returning the active plan's meals grouped by day.
     * @param groceryListSource A function returning the generated grocery list, if available.
     * @throws NullPointerException If either source is null.
     */
    public Parser(Supplier<List<List<MealSummary>>> mealPlanSource,
            Supplier<Optional<List<GroceryItem>>> groceryListSource) {
        this.mealPlanSource = Objects.requireNonNull(mealPlanSource, "mealPlanSource");
        this.groceryListSource = Objects.requireNonNull(groceryListSource, "groceryListSource");
    }

    /**
     * Parses input without modifying application data or calling the generated-data sources.
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
        String arguments = parts.length == 2 ? parts[1] : "";
        if (parts[0].equals(COMMAND_VIEW_LIST)) {
            return parseViewList(arguments);
        }
        if (parts[0].equals(COMMAND_VIEW_PLAN)) {
            return parseViewPlan(arguments);
        }
        if (!parts[0].equals(COMMAND_ADD_RECIPE)) {
            throw new MishMashException("Unknown command: " + parts[0]);
        }
        try {
            return parseAddRecipe(arguments);
        } catch (IllegalArgumentException e) {
            throw new MishMashException(e.getMessage()
                    + System.lineSeparator() + ADD_RECIPE_USAGE);
        }
    }

    /**
     * Creates a grocery viewing command after rejecting unexpected arguments.
     *
     * @param arguments The text after view-list.
     * @return A command displaying the currently generated groceries.
     * @throws MishMashException If arguments are supplied.
     */
    private Command parseViewList(String arguments) throws MishMashException {
        if (!arguments.isBlank()) {
            throw new MishMashException("view-list does not accept arguments."
                    + System.lineSeparator() + VIEW_LIST_USAGE);
        }
        return new ViewListCommand(groceryListSource);
    }

    /**
     * Parses an optional day while leaving checks against the active plan to command execution.
     *
     * @param arguments The text after view-plan.
     * @return A command for all days or one positive day number.
     * @throws MishMashException If the day syntax or value is invalid.
     */
    private Command parseViewPlan(String arguments) throws MishMashException {
        if (arguments.isBlank()) {
            return new ViewPlanCommand(mealPlanSource);
        }
        Matcher matcher = DAY_PATTERN.matcher(arguments.trim());
        if (!matcher.matches()) {
            throw new MishMashException("Use one optional d/ argument with a positive integer."
                    + System.lineSeparator() + VIEW_PLAN_USAGE);
        }
        try {
            return new ViewPlanCommand(mealPlanSource, Integer.parseInt(matcher.group(1)));
        } catch (IllegalArgumentException e) {
            throw new MishMashException("Day number must be a positive integer from 1 to "
                    + Integer.MAX_VALUE + "." + System.lineSeparator() + VIEW_PLAN_USAGE);
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
        collectRecipeArguments(arguments, fields, ingredients);
        validateRequiredFields(fields, ingredients);

        Recipe recipe = new Recipe(fields.get("n"),
                parseNutrition(fields.get("cal"), "Calories"),
                parseNutrition(fields.get("p"), "Protein"),
                parseNutrition(fields.get("c"), "Carbs"),
                parseNutrition(fields.get("f"), "Fats"),
                ingredients);
        return new AddRecipeCommand(recipe);
    }

    /**
     * Reads each prefixed argument into recipe fields or ingredients.
     *
     * @param arguments The arguments following add-recipe.
     * @param fields The destination for single-value fields.
     * @param ingredients The destination for repeated ingredient fields.
     */
    private void collectRecipeArguments(String arguments, Map<String, String> fields,
            List<GroceryItem> ingredients) {
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
    }

    /**
     * Checks that every required field and at least one ingredient were supplied.
     *
     * @param fields The collected single-value recipe fields.
     * @param ingredients The collected ingredients.
     */
    private void validateRequiredFields(Map<String, String> fields, List<GroceryItem> ingredients) {
        for (String required : REQUIRED_FIELDS) {
            if (!fields.containsKey(required)) {
                throw new IllegalArgumentException("Missing required field: " + required + "/");
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one ingredient is required. Use i/NAME:QUANTITY_UNIT.");
        }
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
        if (value.contains("/")) {
            throw new IllegalArgumentException(
                    "Unexpected '/': separate fields with spaces and use each prefix once."
                    + " Use i/ for each ingredient; names and units must not contain '/'.");
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
        if (value.startsWith(":")) {
            throw new IllegalArgumentException("Ingredient name must not be blank.");
        }
        Matcher matcher = INGREDIENT_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Ingredient must use NAME:QUANTITY_UNIT, for example Chicken Breast:200g.");
        }

        BigDecimal quantity = new BigDecimal(matcher.group(2));
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Ingredient quantity must be greater than zero.");
        }

        if (matcher.group(1).isBlank()) {
            throw new IllegalArgumentException("Ingredient name must not be blank.");
        }
        return new GroceryItem(matcher.group(1), quantity, matcher.group(3));
    }
}


