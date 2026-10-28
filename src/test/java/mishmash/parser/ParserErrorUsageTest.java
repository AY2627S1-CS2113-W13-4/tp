package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import mishmash.exception.MishMashException;

/**
 * Checks structured command guidance across parser errors without examining wording to identify usage.
 */
class ParserErrorUsageTest {
    @TestFactory
    Stream<DynamicTest> parseCommand_invalidArguments_attachesSpecificUsageOnce() {
        return Stream.of(
                new String[]{"add-recipe", Parser.ADD_RECIPE_USAGE},
                new String[]{"edit-recipe", Parser.EDIT_RECIPE_USAGE},
                new String[]{"delete-recipe", Parser.DELETE_RECIPE_USAGE},
                new String[]{"list-recipes 0", Parser.LIST_RECIPE_USAGE},
                new String[]{"generate-plan", Parser.GENERATE_PLAN_USAGE},
                new String[]{"generate-list extra", Parser.GENERATE_LIST_USAGE},
                new String[]{"view-plan d/0", Parser.VIEW_PLAN_USAGE},
                new String[]{"view-list extra", Parser.VIEW_LIST_USAGE},
                new String[]{"set-diet diet/invalid", DietaryPreferencesParser.USAGE},
                new String[]{"view-diet extra", "Format: view-diet"},
                new String[]{"record-meal 0", Parser.RECORD_MEAL_USAGE},
                new String[]{"view-meals extra", Parser.VIEW_MEALS_USAGE},
                new String[]{"help extra", "Format: help"})
                .map(example -> DynamicTest.dynamicTest(example[0], () -> {
                    MishMashException error = assertThrows(MishMashException.class,
                            () -> new Parser().parseCommand(example[0]));

                    assertEquals(example[1], error.getUsage().orElseThrow());
                    assertFalse(error.getExplanation().contains(example[1]));
                    assertEquals(error.getExplanation() + System.lineSeparator() + example[1], error.getMessage());
                }));
    }

    @TestFactory
    Stream<DynamicTest> parse_overflowingGenerationNumbers_retainsCauseWithoutEmbeddingUsage() {
        return Stream.of("d/2147483648", "d/1 cal/2147483648")
                .map(input -> DynamicTest.dynamicTest(input, () -> {
                    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                            () -> new GeneratePlanParser().parse(input));

                    assertInstanceOf(NumberFormatException.class, error.getCause());
                    assertFalse(error.getMessage().contains(GeneratePlanParser.USAGE));
                }));
    }
}
