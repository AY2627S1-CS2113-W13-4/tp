package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.FoodInfo;
import mishmash.model.GroceryItem;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.model.RecipeChanges;
import mishmash.parser.Parser;
import mishmash.testutil.TestRecipes;
import mishmash.ui.RecipeViewer;

/** Checks positional edits, atomic failures, execution-time lookup, and generated snapshots. */
class EditRecipeCommandTest {
    /** Builds a protein-only change for direct command tests. */
    private RecipeChanges proteinChange() {
        return new RecipeChanges(null, null, 25, null, null, List.of(), null);
    }

    @Test
    void execute_duplicateNames_editsOnlyChosenPositionAndPreservesBookSnapshot() throws MishMashException {
        RecipeBook book = new RecipeBook();
        Recipe first = TestRecipes.recipe("Rice", 200, 4, 45, 1);
        Recipe second = TestRecipes.recipe("Rice", 300, 6, 60, 2);
        Recipe third = TestRecipes.recipe("Oats", 150, 5, 27, 3);
        book.add(first);
        book.add(second);
        book.add(third);
        List<Recipe> before = book.getRecipes();

        String result = new EditRecipeCommand(2, proteinChange()).execute(book);

        assertEquals(3, book.size());
        assertSame(first, book.getRecipes().getFirst());
        assertSame(third, book.getRecipes().getLast());
        Recipe edited = book.getRecipes().get(1);
        assertEquals(300, edited.getCalories());
        assertEquals(25, edited.getProtein());
        assertEquals(6, second.getProtein());
        assertEquals(List.of(first, second, third), before);
        assertTrue(result.startsWith("Recipe updated!" + System.lineSeparator()
                + new RecipeViewer().view(edited)), result);
        assertTrue(result.contains("Generate a new plan and grocery list"), result);
    }

    @Test
    void execute_firstAndLastPositions_preservesOtherRecipes() throws MishMashException {
        RecipeBook book = new RecipeBook();
        book.add(TestRecipes.recipe("First", 1, 1, 1, 1));
        Recipe middle = TestRecipes.recipe("Middle", 2, 2, 2, 2);
        book.add(middle);
        book.add(TestRecipes.recipe("Last", 3, 3, 3, 3));

        new EditRecipeCommand(1, proteinChange()).execute(book);
        new EditRecipeCommand(3, proteinChange()).execute(book);

        assertEquals(25, book.getRecipes().getFirst().getProtein());
        assertSame(middle, book.getRecipes().get(1));
        assertEquals(25, book.getRecipes().getLast().getProtein());
    }

    @Test
    void execute_parsedBeforeOtherChanges_usesCurrentRecipeAndPreservesItsNewValues() throws MishMashException {
        Parser parser = new Parser();
        RecipeBook book = new RecipeBook();
        book.add(TestRecipes.recipe("Original", 200, 4, 45, 1));
        Command pending = parser.parseCommand("edit-recipe 1 p/25");
        parser.parseCommand("edit-recipe 1 n/New Name cal/350").execute(book);

        pending.execute(book);

        assertEquals("New Name", book.getRecipes().getFirst().getName());
        assertEquals(350, book.getRecipes().getFirst().getCalories());
        assertEquals(25, book.getRecipes().getFirst().getProtein());
    }

    @Test
    void execute_missingTarget_reportsErrorAndLeavesBookUnchanged() {
        RecipeBook book = new RecipeBook();
        MishMashException empty = assertThrows(MishMashException.class,
                () -> new EditRecipeCommand(1, proteinChange()).execute(book));
        assertEquals("There are no recipes to edit.", empty.getMessage());
        Recipe recipe = TestRecipes.recipe("Rice", 200, 4, 45, 1);
        book.add(recipe);
        for (int index : new int[] {2, Integer.MAX_VALUE}) {
            MishMashException error = assertThrows(MishMashException.class,
                    () -> new EditRecipeCommand(index, proteinChange()).execute(book));
            assertEquals("Recipe index must be between 1 and 1.", error.getMessage());
        }
        assertEquals(List.of(recipe), book.getRecipes());
    }

    @Test
    void constructorAndExecute_invalidDependencies_failClearly() {
        assertThrows(IllegalArgumentException.class, () -> new EditRecipeCommand(0, proteinChange()));
        assertThrows(IllegalArgumentException.class, () -> new EditRecipeCommand(-1, proteinChange()));
        assertThrows(NullPointerException.class, () -> new EditRecipeCommand(1, null));
        assertThrows(NullPointerException.class, () -> new EditRecipeCommand(1, proteinChange()).execute(null));
    }

    @Test
    void execute_planAndGroceriesKeepSnapshots_regenerationUsesUpdatedRecipe() throws MishMashException {
        ActiveMealPlan active = new ActiveMealPlan();
        Parser parser = new Parser(new MealPlanGenerator(new Random(1)), active);
        RecipeBook book = new RecipeBook();
        for (String name : List.of("A", "B", "C")) {
            parser.parseCommand("add-recipe n/" + name + " cal/600 p/30 c/50 f/10 i/Rice:100g food/none")
                    .execute(book);
        }
        parser.parseCommand("generate-plan d/1").execute(book);
        parser.parseCommand("generate-list").execute(book);
        var plan = active.get().orElseThrow();
        var request = active.getRequest();
        var groceries = active.getGroceryList().orElseThrow();

        parser.parseCommand("edit-recipe 1 cal/800 p/50 i/Tofu:200g").execute(book);

        assertSame(plan, active.get().orElseThrow());
        assertEquals(request, active.getRequest());
        assertSame(groceries, active.getGroceryList().orElseThrow());
        assertEquals(1800, plan.getDays().getFirst().getTotalCalories());
        assertTrue(parser.parseCommand("check-shortfall cal/2000").execute(book).contains("short by 200 kcal"));
        parser.parseCommand("generate-list").execute(book);
        assertEquals(groceries.stream().map(Object::toString).toList(),
                active.getGroceryList().orElseThrow().stream().map(Object::toString).toList());

        parser.parseCommand("generate-plan d/1").execute(book);
        parser.parseCommand("generate-list").execute(book);
        assertEquals(2000, active.get().orElseThrow().getDays().getFirst().getTotalCalories());
        assertEquals(110, active.get().orElseThrow().getDays().getFirst().getTotalProtein());
        assertTrue(active.getGroceryList().orElseThrow().stream().anyMatch(item ->
                item.getName().equals("Tofu") && item.getQuantity().compareTo(new BigDecimal("200")) == 0
                        && item.getUnit().equals("g")));
    }

    @Test
    void execute_updatedFoodDeclaration_isUsedByDietaryFiltering() throws MishMashException {
        Parser parser = new Parser();
        RecipeBook book = new RecipeBook();
        for (String name : List.of("A", "B", "C")) {
            book.add(new Recipe(name, 600, 30, 50, 10,
                    List.of(new GroceryItem("Rice", BigDecimal.ONE, "g")), FoodInfo.parseField("none")));
        }
        parser.parseCommand("set-diet diet/vegetarian").execute(book);
        parser.parseCommand("edit-recipe 1 food/poultry").execute(book);

        assertThrows(MishMashException.class, () -> parser.parseCommand("generate-plan d/1").execute(book));
        parser.parseCommand("edit-recipe 1 food/none").execute(book);
        parser.parseCommand("generate-plan d/1").execute(book);
    }
}
