package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import mishmash.model.GroceryItem;
import mishmash.model.RecipeBook;
import mishmash.testutil.LogCapture;

/**
 * Checks grocery-list viewing logs, generated-empty lists, and recovery after invalid source data.
 */
class ViewListDiagnosticsTest {
    private final RecipeBook recipeBook = new RecipeBook();
    private final GroceryItem ingredient = new GroceryItem("Private ingredient", BigDecimal.ONE, "g");

    @Test
    void execute_absentAndGeneratedEmptyGroceries_logsDifferentStates() {
        try (LogCapture logs = new LogCapture(ViewListCommand.class)) {
            new ViewListCommand(Optional::empty).execute(recipeBook);
            assertFalse(logs.getMessages().contains("displayed"));
            new ViewListCommand(() -> Optional.of(List.of())).execute(recipeBook);

            assertEquals(2, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("before a grocery list was generated"));
            assertTrue(logs.getMessages().contains("displayed 0 items"));
        }
    }

    @Test
    void execute_groceryEntries_logsEntryCountWithoutIngredientContents() {
        List<GroceryItem> groceries = List.of(ingredient, ingredient,
                new GroceryItem("Water", new BigDecimal("0.001"), "ml"));
        try (LogCapture logs = new LogCapture(ViewListCommand.class)) {
            new ViewListCommand(() -> Optional.of(groceries)).execute(recipeBook);

            assertEquals(1, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("displayed 3 items"));
            assertFalse(logs.getMessages().contains(ingredient.getName()));
            assertEquals(Level.FINE, logs.getRecords().getFirst().getLevel());
        }
    }

    @Test
    void execute_invalidGrocery_logsSuccessOnlyAfterValidReplacement() {
        AtomicReference<Optional<List<GroceryItem>>> groceries =
                new AtomicReference<>(Optional.of(Arrays.asList(ingredient, null)));
        ViewListCommand command = new ViewListCommand(groceries::get);
        try (LogCapture logs = new LogCapture(ViewListCommand.class)) {
            assertThrows(NullPointerException.class, () -> command.execute(recipeBook));

            assertTrue(logs.getRecords().isEmpty());
            groceries.set(Optional.of(List.of(ingredient)));
            assertTrue(command.execute(recipeBook).contains(ingredient.getName()));
            assertEquals(1, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("displayed 1 items"));
        }
    }

    @Test
    void execute_sourceFailure_preservesOriginalExceptionWithoutSuccessLog() {
        IllegalStateException failure = new IllegalStateException("Source unavailable");
        Command command = new ViewListCommand(() -> {
            throw failure;
        });
        try (LogCapture logs = new LogCapture(ViewListCommand.class)) {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> command.execute(recipeBook)));
            assertTrue(logs.getRecords().isEmpty());
        }
    }
}
