package mishmash.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mishmash.command.AddRecipeCommand;
import mishmash.command.CheckShortfallCommand;
import mishmash.command.Command;
import mishmash.command.DeleteRecipeCommand;
import mishmash.command.GenerateListCommand;
import mishmash.command.GeneratePlanCommand;
import mishmash.command.HelpCommand;
import mishmash.command.ListRecipeCommand;
import mishmash.command.ViewListCommand;
import mishmash.command.ViewPlanCommand;
import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;
import mishmash.ui.MealPlanViewer.MealSummary;

/**
 * Converts raw user input into a validated command.
 */
public class Parser {
    /**
     * Describes the required recipe fields and repeated ingredient syntax.
     */
    public static final String ADD_RECIPE_USAGE =
            "Format: add-recipe n/DISHNAME cal/CALORIES p/PROTEIN c/CARBS f/FATS"
                    + " i/INGREDIENT:QUANTITYUNIT"
                    + System.lineSeparator()
                    + "Include at least one ingredient. To add more, repeat i/INGREDIENT:QUANTITYUNIT,"
                    + " separated by spaces."
                    + System.lineSeparator()
                    + "Example: add-recipe n/Chicken Rice cal/650 p/50 c/70 f/15"
                    + " i/Chicken:200g i/Rice:150g";

    /**
     * Describes how to choose the recipe to delete.
     */
    public static final String DELETE_RECIPE_USAGE = "Format: delete-recipe INDEX";

    /**
     * Describes how to list all recipes or show one in full.
     */
    public static final String LIST_RECIPE_USAGE = "Format: list-recipes [INDEX]";

    public static final String VIEW_PLAN_USAGE = ViewPlanCommand.USAGE;
    public static final String VIEW_LIST_USAGE = ViewListCommand.USAGE;
    public static final String GENERATE_PLAN_USAGE = GeneratePlanParser.USAGE;
    public static final String CHECK_SHORTFALL_USAGE = CheckShortfallCommand.USAGE;

    /**
     * Describes how to generate groceries for the active meal plan.
     */
    public static final String GENERATE_LIST_USAGE = GenerateListCommand.USAGE;

    /**
     * Lists supported syntax for empty or unknown input.
     */
    public static final String COMMAND_USAGE = String.join(System.lineSeparator(),
            ADD_RECIPE_USAGE, LIST_RECIPE_USAGE, DELETE_RECIPE_USAGE, GENERATE_PLAN_USAGE,
            VIEW_PLAN_USAGE, CHECK_SHORTFALL_USAGE, GENERATE_LIST_USAGE, VIEW_LIST_USAGE,
            "Format: help", "Format: bye");

    /**
     * Records parsing attempts and validated command details without logging full recipe input.
     */
    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    private static final String COMMAND_ADD_RECIPE = "add-recipe";
    private static final String COMMAND_LIST_RECIPE = "list-recipes";
    private static final String COMMAND_DELETE_RECIPE = "delete-recipe";
    private static final String COMMAND_GENERATE_PLAN = GeneratePlanParser.COMMAND_WORD;
    private static final String COMMAND_GENERATE_LIST = "generate-list";

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
     * Supplies the active plan at command execution, so view-plan always shows the latest generated plan.
     */
    private final Supplier<List<List<MealSummary>>> mealPlanSource;

    /**
     * Supplies the generated list without making viewing responsible for generation or storage.
     */
    private final Supplier<Optional<List<GroceryItem>>> groceryListSource;

    /**
     * Picks the meals for each generate-plan command.
     */
    private final MealPlanGenerator mealPlanGenerator;

    /**
     * Where generate-plan stores its plan; view-plan reads from the same object when built by the
     * constructors that share it.
     */
    private final ActiveMealPlan activeMealPlan;

    /**
     * Creates a parser sharing one active plan and grocery snapshot across generation and viewing.
     */
    public Parser() {
        this(new MealPlanGenerator(new Random()), new ActiveMealPlan());
    }

    /**
     * Connects meal-plan and grocery generation and viewing through one shared active plan.
     *
     * @param mealPlanGenerator The generator used by generate-plan commands.
     * @param activeMealPlan The shared plan and its generated grocery snapshot.
     * @throws NullPointerException If either argument is null.
     */
    public Parser(MealPlanGenerator mealPlanGenerator, ActiveMealPlan activeMealPlan) {
        this(mealPlanGenerator, activeMealPlan,
                Objects.requireNonNull(activeMealPlan, "activeMealPlan")::getGroceryList);
    }

    /**
     * Connects viewing commands to the caller's existing data through functions called during execution.
     * An empty plan means no active plan; an empty Optional means no grocery list has been generated.
     * Plans created by generate-plan on this parser are stored privately and are not visible to
     * the supplied sources. Use {@link #Parser(MealPlanGenerator, ActiveMealPlan)} to connect
     * both generation commands to their viewers.
     *
     * @param mealPlanSource A function returning the active plan's meals grouped by day.
     * @param groceryListSource A function returning the generated grocery list, if available.
     * @throws NullPointerException If either source is null.
     */
    public Parser(Supplier<List<List<MealSummary>>> mealPlanSource,
            Supplier<Optional<List<GroceryItem>>> groceryListSource) {
        this(new MealPlanGenerator(new Random()), new ActiveMealPlan(), mealPlanSource, groceryListSource);
    }

    /**
     * Connects generate-plan and view-plan through one shared active plan.
     * Grocery viewing uses the caller's source; use the two-argument constructor to view
     * the grocery snapshot created by this parser's generate-list commands.
     *
     * @param mealPlanGenerator The generator used by generate-plan commands.
     * @param activeMealPlan Where generate-plan stores its plan and view-plan reads it from.
     * @param groceryListSource A function returning the generated grocery list, if available.
     * @throws NullPointerException If any argument is null.
     */
    public Parser(MealPlanGenerator mealPlanGenerator, ActiveMealPlan activeMealPlan,
            Supplier<Optional<List<GroceryItem>>> groceryListSource) {
        this(mealPlanGenerator, activeMealPlan,
                () -> activeMealPlan.get().map(MealPlan::toMealSummaries).orElse(List.of()),
                groceryListSource);
    }

    /**
     * Initializes generation and viewing dependencies shared by the public constructors.
     *
     * @param mealPlanGenerator The generator used by generate-plan commands.
     * @param activeMealPlan The destination for newly generated plans and groceries.
     * @param mealPlanSource The source read by meal-plan viewing commands.
     * @param groceryListSource The source of generated groceries, if available.
     * @throws NullPointerException If any dependency is null.
     */
    private Parser(MealPlanGenerator mealPlanGenerator, ActiveMealPlan activeMealPlan,
            Supplier<List<List<MealSummary>>> mealPlanSource,
            Supplier<Optional<List<GroceryItem>>> groceryListSource) {
        this.mealPlanGenerator = Objects.requireNonNull(mealPlanGenerator, "mealPlanGenerator");
        this.activeMealPlan = Objects.requireNonNull(activeMealPlan, "activeMealPlan");
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
        LOGGER.fine(() -> "Parsing command: " + parts[0]);
        String arguments = parts.length == 2 ? parts[1] : "";
        return parseNamedCommand(parts[0], arguments);
    }

    /**
     * Dispatches a command word to its argument parser without executing the resulting command.
     *
     * @param commandWord The command name without surrounding whitespace.
     * @param arguments The text after the command name.
     * @return The validated command.
     * @throws MishMashException If the command name or arguments are invalid.
     */
    private Command parseNamedCommand(String commandWord, String arguments) throws MishMashException {
        if (commandWord.equals("help")) {
            if (!arguments.isBlank()) {
                throw new MishMashException("help does not accept arguments."
                        + System.lineSeparator() + "Format: help");
            }
            return new HelpCommand();
        }
        if (commandWord.equals(COMMAND_GENERATE_LIST)) {
            return parseGenerateList(arguments);
        }
        if (commandWord.equals(COMMAND_VIEW_LIST)) {
            return parseViewList(arguments);
        }
        if (commandWord.equals(COMMAND_VIEW_PLAN)) {
            return parseViewPlan(arguments);
        }
        if (commandWord.equals(COMMAND_GENERATE_PLAN)) {
            return parseGeneratePlan(arguments);
        }
        if (commandWord.equals("check-shortfall")) {
            return parseCheckShortfall(arguments);
        }
        if (commandWord.equals(COMMAND_LIST_RECIPE)) {
            return parseListRecipe(arguments);
        }
        if (commandWord.equals(COMMAND_DELETE_RECIPE)) {
            return parseDeleteRecipe(arguments);
        }
        if (commandWord.equals(COMMAND_ADD_RECIPE)) {
            return parseAddRecipeCommand(arguments);
        }
        throw new MishMashException("Unknown command: " + commandWord);
    }

    /**
     * Creates a read-only shortfall check and attaches command usage to invalid target errors.
     *
     * @param arguments Explicit daily nutrition targets supplied after check-shortfall.
     * @return A check that reads the current active plan when executed.
     * @throws MishMashException If no target is supplied or target syntax is invalid.
     */
    private Command parseCheckShortfall(String arguments) throws MishMashException {
        try {
            return new CheckShortfallCommand(activeMealPlan, new CheckShortfallParser().parse(arguments));
        } catch (IllegalArgumentException e) {
            throw new MishMashException(e.getMessage()
                    + System.lineSeparator() + CHECK_SHORTFALL_USAGE, e);
        }
    }

    //@@author peilingggg
    /**
     * Parses a recipe addition and attaches its usage instructions to recoverable validation errors.
     *
     * @param arguments The text after add-recipe.
     * @return The validated recipe addition command.
     * @throws MishMashException If recipe validation fails.
     */
    private Command parseAddRecipeCommand(String arguments) throws MishMashException {
        try {
            return parseAddRecipe(arguments);
        } catch (IllegalArgumentException e) {
            throw new MishMashException(e.getMessage()
                    + System.lineSeparator() + ADD_RECIPE_USAGE, e);
        }
    }
    //@@author

    /**
     * Creates a grocery generation command after rejecting unexpected arguments.
     *
     * @param arguments The text after generate-list.
     * @return A command that reads the active plan only when executed.
     * @throws MishMashException If arguments are supplied.
     */
    private Command parseGenerateList(String arguments) throws MishMashException {
        if (!arguments.isBlank()) {
            LOGGER.warning("generate-list rejected: unexpected arguments");
            throw new MishMashException("generate-list does not accept arguments."
                    + System.lineSeparator() + GENERATE_LIST_USAGE);
        }
        LOGGER.fine("Parsed generate-list with no arguments");
        return new GenerateListCommand(activeMealPlan);
    }

    /**
     * Validates the days and optional nutrition targets of a generate-plan command.
     * The plan itself is only generated, and stored, when the command executes.
     *
     * @param arguments The text after generate-plan.
     * @return A command that generates a plan and makes it the active plan.
     * @throws MishMashException If the arguments are invalid; the message always ends with the usage.
     */
    private Command parseGeneratePlan(String arguments) throws MishMashException {
        try {
            GeneratePlanRequest request = new GeneratePlanParser().parse(arguments);
            LOGGER.fine(() -> "Parsed generate-plan for " + request.days() + " days");
            return new GeneratePlanCommand(request, mealPlanGenerator, activeMealPlan);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (!message.contains(GENERATE_PLAN_USAGE)) {
                message += System.lineSeparator() + GENERATE_PLAN_USAGE;
            }
            throw new MishMashException(message, e);
        }
    }

    /**
     * Creates a command that deletes the recipe at the typed position.
     *
     * @param arguments The text after delete-recipe.
     * @return A command that deletes the recipe at that position.
     * @throws MishMashException If the index is missing or is not a positive whole number.
     */
    private Command parseDeleteRecipe(String arguments) throws MishMashException {
        try {
            int index = parseRecipeIndex(arguments);
            DeleteRecipeCommand command = new DeleteRecipeCommand(index);
            LOGGER.fine(() -> "Parsed delete-recipe for index " + index);
            return command;
        } catch (IllegalArgumentException e) {
            throw new MishMashException(e.getMessage()
                    + System.lineSeparator() + DELETE_RECIPE_USAGE, e);
        }
    }

    /**
     * Creates a command that lists all recipes, or shows one in full if an index is given.
     *
     * @param arguments The text after list-recipe; empty to list every recipe.
     * @return A command that shows the recipe list or one recipe's details.
     * @throws MishMashException If an index is given but is not a positive whole number.
     */
    private Command parseListRecipe(String arguments) throws MishMashException {
        if (arguments.isBlank()) {
            LOGGER.fine("Parsed list-recipes for all recipes");
            return new ListRecipeCommand();
        }
        try {
            int index = parseRecipeIndex(arguments);
            ListRecipeCommand command = new ListRecipeCommand(index);
            LOGGER.fine(() -> "Parsed list-recipes for index " + index);
            return command;
        } catch (IllegalArgumentException e) {
            throw new MishMashException(e.getMessage()
                    + System.lineSeparator() + LIST_RECIPE_USAGE, e);
        }
    }

    /**
     * Reads the single recipe index that follows delete-recipe or list-recipe.
     * Whether a recipe exists at that position is only known when the command runs.
     *
     * @param arguments The text after the command word.
     * @return The index, counting from 1.
     */
    private int parseRecipeIndex(String arguments) {
        if (arguments.isBlank()) {
            throw new IllegalArgumentException("Recipe index is required.");
        }
        // Digits only: this also rejects signs, decimals, and extra words such as "1 2".
        if (!arguments.matches("[0-9]+")) {
            throw new IllegalArgumentException("Recipe index must be a single positive whole number.");
        }

        int index;
        try {
            index = Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Recipe index must not exceed " + Integer.MAX_VALUE + ".");
        }
        if (index < 1) {
            throw new IllegalArgumentException("Recipe index must be at least 1.");
        }
        return index;
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
        LOGGER.fine("Parsed view-list with no arguments");
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
            LOGGER.fine("Parsed view-plan for all days");
            return new ViewPlanCommand(mealPlanSource);
        }
        Matcher matcher = DAY_PATTERN.matcher(arguments.trim());
        if (!matcher.matches()) {
            throw new MishMashException("Use one optional d/ argument with a positive integer."
                    + System.lineSeparator() + VIEW_PLAN_USAGE);
        }
        try {
            int dayNumber = Integer.parseInt(matcher.group(1));
            ViewPlanCommand command = new ViewPlanCommand(mealPlanSource, dayNumber);
            LOGGER.fine(() -> "Parsed view-plan for day " + dayNumber);
            return command;
        } catch (IllegalArgumentException e) {
            throw new MishMashException("Day number must be a positive integer from 1 to "
                    + Integer.MAX_VALUE + "." + System.lineSeparator() + VIEW_PLAN_USAGE, e);
        }
    }

    //@@author peilingggg
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
        LOGGER.fine(() -> "Parsed add-recipe with " + ingredients.size() + " ingredients");
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
                    "At least one ingredient is required. Use i/INGREDIENT:QUANTITYUNIT, for example i/Rice:150g.");
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
    //@@author

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
                    + Integer.MAX_VALUE + ".", e);
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
                    "Invalid ingredient format. Use i/INGREDIENT:QUANTITYUNIT, for example i/Chicken Breast:200g.");
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


