package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.command.Command;
import mishmash.command.EditRecipeCommand;
import mishmash.command.HelpCommand;
import mishmash.exception.MishMashException;
import mishmash.model.FoodInfo;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;

/** Checks edit syntax, shared recipe validation, and preservation of omitted fields. */
class ParserEditRecipeTest {
    private final Parser parser = new Parser();

    /** Creates a classified recipe so omitted food information can be checked too. */
    private RecipeBook createBook() throws MishMashException {
        RecipeBook book = new RecipeBook();
        parser.parseCommand("add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g food/none").execute(book);
        return book;
    }

    @Test
    void parse_partialEdit_preservesOmittedFieldsAndDoesNotChangeBookUntilExecution() throws MishMashException {
        RecipeBook book = createBook();
        Recipe original = book.getRecipes().getFirst();

        Command command = parser.parseCommand("edit-recipe 1 p/12");

        assertInstanceOf(EditRecipeCommand.class, command);
        assertSame(original, book.getRecipes().getFirst());
        command.execute(book);
        Recipe edited = book.getRecipes().getFirst();
        assertEquals("Rice", edited.getName());
        assertEquals(200, edited.getCalories());
        assertEquals(12, edited.getProtein());
        assertEquals(45, edited.getCarbs());
        assertEquals(1, edited.getFats());
        assertEquals(original.getIngredients(), edited.getIngredients());
        assertEquals(original.getFoodInfo(), edited.getFoodInfo());
        assertEquals(4, original.getProtein());
    }

    @Test
    void parse_allFieldsInDifferentOrder_replacesEntireIngredientsAndFoodDeclaration() throws MishMashException {
        RecipeBook book = createBook();
        parser.parseCommand("edit-recipe 01 i/Chicken Breast:220.5g f/15 c/70 p/50 cal/650"
                + " n/Chicken Rice i/Water:0.25L food/poultry").execute(book);

        Recipe edited = book.getRecipes().getFirst();
        assertEquals("Chicken Rice", edited.getName());
        assertEquals(650, edited.getCalories());
        assertEquals(50, edited.getProtein());
        assertEquals(70, edited.getCarbs());
        assertEquals(15, edited.getFats());
        assertEquals(List.of("Chicken Breast: 220.5g", "Water: 0.25L"),
                edited.getIngredients().stream().map(Object::toString).toList());
        assertEquals(FoodInfo.parseField("poultry"), edited.getFoodInfo());
    }

    @Test
    void parse_ingredientOnlyEdit_replacesOldItemsWhilePreservingNutritionAndFood() throws MishMashException {
        RecipeBook book = createBook();
        parser.parseCommand("edit-recipe 1 i/Brown Rice:175g").execute(book);

        Recipe edited = book.getRecipes().getFirst();
        assertEquals(List.of("Brown Rice: 175g"),
                edited.getIngredients().stream().map(Object::toString).toList());
        assertEquals(200, edited.getCalories());
        assertEquals(FoodInfo.parseField("none"), edited.getFoodInfo());
    }

    @Test
    void parse_nameAndFoodOnlyEdits_preserveAllOtherFields() throws MishMashException {
        RecipeBook book = createBook();
        parser.parseCommand("  edit-recipe\t1\tn/ Brown Rice  ").execute(book);
        parser.parseCommand("edit-recipe 1 food/dairy").execute(book);
        parser.parseCommand("edit-recipe 1 food/none").execute(book);

        Recipe edited = book.getRecipes().getFirst();
        assertEquals("Brown Rice", edited.getName());
        assertEquals(200, edited.getCalories());
        assertEquals(4, edited.getProtein());
        assertEquals(45, edited.getCarbs());
        assertEquals(1, edited.getFats());
        assertEquals(FoodInfo.parseField("none"), edited.getFoodInfo());
        assertEquals("Rice", edited.getIngredients().getFirst().getName());
    }

    @Test
    void parse_nutritionBoundaries_acceptsZeroAndMaximumInteger() throws MishMashException {
        RecipeBook book = createBook();
        parser.parseCommand("edit-recipe 1 cal/0 p/2147483647 c/000 f/0").execute(book);

        Recipe edited = book.getRecipes().getFirst();
        assertEquals(0, edited.getCalories());
        assertEquals(Integer.MAX_VALUE, edited.getProtein());
        assertEquals(0, edited.getCarbs());
        assertEquals(0, edited.getFats());
    }

    /** Exercises malformed edits that must never publish a partially changed recipe. */
    @TestFactory
    Stream<DynamicTest> parse_invalidEdits_reportsEditUsageAndPreservesRecipe() {
        return Stream.of("", "1", "0 p/5", "-1 p/5", "+1 p/5", "1.0 p/5", "2147483648 p/5",
                "p/5", "1 2 p/5", "1 text p/5", "1 n/", "1 i/", "1 cal/", "1 p/", "1 c/", "1 f/",
                "1 food/", "1 n/A n/B", "1 cal/1 cal/2", "1 p/1 p/2", "1 c/1 c/2", "1 f/1 f/2",
                "1 food/none food/dairy", "1 x/5", "1 p/-1", "1 p/+1", "1 p/1.5", "1 p/5g",
                "1 p/2147483648", "1 cal/2147483648", "1 c/-1", "1 f/1.5", "1 i/Rice:0g",
                "1 i/Rice:-1g", "1 i/Rice:1", "1 i/:1g", "1 i/Rice:1g n/Updated p/bad",
                "1 p/5c/3", "1 n/Rice/Bowl", "1 food/unsupported", "1 food/dairy,dairy")
                .map(arguments -> DynamicTest.dynamicTest("edit-recipe " + arguments, () -> {
                    RecipeBook book = createBook();
                    Recipe original = book.getRecipes().getFirst();
                    MishMashException error = assertThrows(MishMashException.class,
                            () -> parser.parseCommand("edit-recipe " + arguments));
                    assertTrue(error.getMessage().endsWith(Parser.EDIT_RECIPE_USAGE), error.getMessage());
                    assertInstanceOf(IllegalArgumentException.class, error.getCause());
                    assertEquals(List.of(original), book.getRecipes());
                }));
    }

    @Test
    void helpAndGeneralUsage_includeEditing() {
        String help = new HelpCommand().execute(new RecipeBook());
        assertTrue(help.contains(Parser.EDIT_RECIPE_USAGE));
        assertTrue(Parser.COMMAND_USAGE.contains(Parser.EDIT_RECIPE_USAGE));
    }
}
