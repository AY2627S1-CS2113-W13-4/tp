package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.model.RecipeBook;
import mishmash.testutil.LogCapture;
import mishmash.ui.MealPlanViewer.MealSummary;

/**
 * Checks meal-plan viewing logs, selected-day failures, and recovery after invalid source data.
 */
class ViewPlanDiagnosticsTest {
    private final RecipeBook recipeBook = new RecipeBook();
    private final MealSummary meal = new MealSummary("Private meal", 100, 10, 20, 5);

    @Test
    void execute_allAndSelectedDays_logsSelectionWithoutMealContents() throws MishMashException {
        List<List<MealSummary>> days = List.of(List.of(meal), List.of(meal));
        try (LogCapture logs = new LogCapture(ViewPlanCommand.class)) {
            new ViewPlanCommand(() -> days).execute(recipeBook);
            new ViewPlanCommand(() -> days, 2).execute(recipeBook);

            assertEquals(2, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("displayed all 2 days"));
            assertTrue(logs.getMessages().contains("displayed day 2"));
            assertFalse(logs.getMessages().contains(meal.name()));
            assertTrue(logs.getRecords().stream().allMatch(record -> record.getLevel() == Level.FINE));
        }
    }

    @Test
    void execute_missingPlan_logsAbsenceWithoutClaimingDisplay() throws MishMashException {
        try (LogCapture logs = new LogCapture(ViewPlanCommand.class)) {
            new ViewPlanCommand(List::of).execute(recipeBook);

            assertEquals(1, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("no active meal plan"));
            assertFalse(logs.getMessages().contains("displayed"));
        }
    }

    @Test
    void execute_invalidDay_logsCurrentBoundsAndSuccessfulRetry() throws MishMashException {
        AtomicReference<List<List<MealSummary>>> days = new AtomicReference<>(List.of(List.of(meal)));
        ViewPlanCommand command = new ViewPlanCommand(days::get, 2);
        try (LogCapture logs = new LogCapture(ViewPlanCommand.class)) {
            MishMashException error = assertThrows(MishMashException.class, () -> command.execute(recipeBook));

            assertTrue(error.getMessage().endsWith(ViewPlanCommand.USAGE));
            assertTrue(logs.getMessages().contains("rejected day 2: active plan contains 1 days"));
            assertFalse(logs.getMessages().contains("displayed"));
            days.set(List.of(List.of(meal), List.of(meal)));
            command.execute(recipeBook);
            assertEquals(2, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("displayed day 2"));
        }
    }

    @Test
    void execute_invalidMeal_logsSuccessOnlyAfterValidReplacement() throws MishMashException {
        AtomicReference<List<List<MealSummary>>> days =
                new AtomicReference<>(List.of(Arrays.asList(meal, null)));
        ViewPlanCommand command = new ViewPlanCommand(days::get);
        try (LogCapture logs = new LogCapture(ViewPlanCommand.class)) {
            assertThrows(NullPointerException.class, () -> command.execute(recipeBook));

            assertTrue(logs.getRecords().isEmpty());
            days.set(List.of(List.of(meal)));
            assertTrue(command.execute(recipeBook).contains(meal.name()));
            assertEquals(1, logs.getRecords().size());
            assertTrue(logs.getMessages().contains("displayed all 1 days"));
        }
    }

    @Test
    void execute_sourceFailure_preservesOriginalExceptionWithoutSuccessLog() {
        IllegalStateException failure = new IllegalStateException("Source unavailable");
        Command command = new ViewPlanCommand(() -> {
            throw failure;
        });
        try (LogCapture logs = new LogCapture(ViewPlanCommand.class)) {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> command.execute(recipeBook)));
            assertTrue(logs.getRecords().isEmpty());
        }
    }
}
