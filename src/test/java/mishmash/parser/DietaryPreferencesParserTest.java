package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.model.Diet;
import mishmash.model.DietaryPreferences;
import mishmash.model.FoodTag;

/** Exercises dietary command syntax, spelling errors, duplicate fields, and malformed lists. */
class DietaryPreferencesParserTest {
    private final DietaryPreferencesParser parser = new DietaryPreferencesParser();

    @TestFactory
    Stream<DynamicTest> parse_invalidInput_rejectsEveryMalformedCase() {
        return Stream.of("", " ", "vegan", "vegan diet/vegan", "diet/", "avoid/", "exclude/",
                "diet/keto", "diet/vegan diet/vegetarian", "avoid/milk avoid/egg", "exclude/a exclude/b",
                "allergy/milk", "Diet/vegan", "avoid/nuts", "avoid/milk,", "avoid/,milk", "avoid/milk,,egg",
                "avoid/milk,dairy", "exclude/salt,", "exclude/,salt", "exclude/salt,,pepper",
                "exclude/salt,salt", "exclude/Salt, SALT", "diet/vegan/extra", "avoid/none",
                "diet/vegan unknown/yes", "diet/vegan stray-text")
                .map(input -> DynamicTest.dynamicTest("Reject: " + input,
                        () -> assertThrows(IllegalArgumentException.class, () -> parser.parse(input))));
    }

    @TestFactory
    Stream<DynamicTest> parse_supportedDietLabels_acceptsEveryDiet() {
        return Arrays.stream(Diet.values()).map(diet -> DynamicTest.dynamicTest(diet.label(), () ->
                assertEquals(diet, parser.parse("diet/" + diet.label()).diet())));
    }

    @Test
    void parse_combinedFields_acceptsAnyOrderAndNormalizesValues() {
        assertEquals(new DietaryPreferences(Diet.VEGAN, Set.of(FoodTag.MILK, FoodTag.PEANUT),
                Set.of("bell pepper", "mushroom")), parser.parse(
                " exclude/ Bell   Pepper, Mushroom  avoid/ Dairy, PEANUT diet/ VEGAN "));
    }

    @Test
    void parse_omittedFields_defaultToUnrestrictedValues() {
        assertEquals(Diet.NONE, parser.parse("avoid/egg").diet());
        assertEquals(Set.of(), parser.parse("diet/vegetarian").excludedIngredients());
        assertEquals(Set.of(), parser.parse("exclude/mushroom").avoidedTags());
        assertEquals(DietaryPreferences.none(), parser.parse("diet/omnivore"));
        assertEquals(Diet.VEGETARIAN, parser.parse("diet/lacto-ovo-vegetarian").diet());
    }

    @Test
    void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
