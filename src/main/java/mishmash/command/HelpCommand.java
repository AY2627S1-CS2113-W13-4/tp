package mishmash.command;

import mishmash.model.Diet;
import mishmash.model.FoodTag;
import mishmash.model.RecipeBook;
import mishmash.parser.DietaryPreferencesParser;
import mishmash.parser.Parser;

/**
 * Displays every supported command with its purpose and syntax.
 */
public class HelpCommand implements Command {
    @Override
    public String execute(RecipeBook recipeBook) {
        return String.join(System.lineSeparator(),
                "Available commands (names are case-sensitive):",
                "add-recipe - Add a recipe with nutrition and ingredients.",
                Parser.ADD_RECIPE_USAGE,
                "",
                "list-recipes - List all recipes, or show one recipe in full.",
                Parser.LIST_RECIPE_USAGE,
                "",
                "edit-recipe - Update selected fields of a recipe by its displayed index.",
                Parser.EDIT_RECIPE_USAGE,
                "Existing plans and groceries keep their snapshots; regenerate them to use the edit.",
                "",
                "delete-recipe - Delete a recipe by its displayed index, after confirmation.",
                Parser.DELETE_RECIPE_USAGE,
                "",
                "generate-plan - Generate a meal plan with optional daily nutrition targets.",
                Parser.GENERATE_PLAN_USAGE,
                "",
                "view-plan - View the active meal plan, or one day.",
                Parser.VIEW_PLAN_USAGE,
                "",
                CheckShortfallCommand.COMMAND_WORD
                        + " - Check each day's planned nutrition against explicit daily targets.",
                Parser.CHECK_SHORTFALL_USAGE,
                "Supply at least one target; omitted nutrients are not checked.",
                "",
                "generate-list - Generate a grocery list from the active meal plan.",
                Parser.GENERATE_LIST_USAGE,
                "",
                "view-list - View the generated grocery list.",
                Parser.VIEW_LIST_USAGE,
                "",
                "set-diet - Replace dietary preferences and update conflicting planned days.",
                DietaryPreferencesParser.USAGE,
                "Supply at least one field. Values are case-insensitive; field prefixes are case-sensitive.",
                Diet.getInputHelp(),
                "Food tags: " + FoodTag.getSupportedLabels(),
                "Recipes must declare every applicable category, including those in compound ingredients.",
                "Unclassified recipes are excluded while restrictions are active.",
                "Ingredient exclusions match complete names, ignoring case and extra spaces.",
                "",
                "view-diet - View current dietary preferences.",
                "Format: view-diet",
                "",
                "help - Show all commands and what they do.",
                "Format: help",
                "bye - Exit MishMash.",
                "Format: bye",
                "",
                "Square brackets indicate optional arguments. Use list-recipes to find recipe indexes.");
    }
}
