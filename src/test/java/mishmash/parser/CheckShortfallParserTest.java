package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.model.NutritionTargets;

/** Exercises each target prefix, numerical boundaries, and malformed command arguments. */
class CheckShortfallParserTest {
    private final CheckShortfallParser parser = new CheckShortfallParser();

    @Test
    void parse_reorderedTargetsAndWhitespace_preservesValuesAndOmissions() {
        NutritionTargets targets = parser.parse(" \tf/60  p/100 cal/2000\t");

        assertEquals(2000, targets.getCalories());
        assertEquals(100, targets.getProtein());
        assertEquals(60, targets.getFats());
        assertNull(targets.getCarbs());
    }

    @Test
    void parse_minimumAndMaximumTargets_acceptsBoundariesAndLeadingZeros() {
        NutritionTargets minimum = parser.parse("cal/01 p/1 c/1 f/1");
        assertEquals(1, minimum.getCalories());
        assertEquals(1, minimum.getProtein());
        assertEquals(1, minimum.getCarbs());
        assertEquals(1, minimum.getFats());

        NutritionTargets maximum = parser.parse("cal/2147483647 p/2147483647 c/2147483647 f/2147483647");
        assertEquals(Integer.MAX_VALUE, maximum.getCalories());
        assertEquals(Integer.MAX_VALUE, maximum.getProtein());
        assertEquals(Integer.MAX_VALUE, maximum.getCarbs());
        assertEquals(Integer.MAX_VALUE, maximum.getFats());
    }

    @Test
    void parse_eachSingleTarget_setsOnlyRequestedNutrient() {
        List<NutritionTargets> targets = List.of(parser.parse("cal/1"), parser.parse("p/1"),
                parser.parse("c/1"), parser.parse("f/1"));
        for (int index = 0; index < targets.size(); index++) {
            NutritionTargets actual = targets.get(index);
            assertEquals(index == 0 ? 1 : null, actual.getCalories());
            assertEquals(index == 1 ? 1 : null, actual.getProtein());
            assertEquals(index == 2 ? 1 : null, actual.getCarbs());
            assertEquals(index == 3 ? 1 : null, actual.getFats());
        }
    }

    @Test
    void parse_missingTargets_rejectsNullEmptyAndWhitespace() {
        for (String arguments : new String[]{null, "", " ", "\t\r\n"}) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(arguments));
        }
    }

    @TestFactory
    Stream<DynamicTest> parse_malformedValues_rejectsEveryNutrient() {
        List<String> invalidValues = List.of("", "0", "000", "-1", "+1", "1.5", "1e3", "NaN", "Infinity",
                "1_000", "1,000", "2000kcal", "\u0661", "2147483648", "99999999999999999999999", "1/2");
        return Stream.of("cal/", "p/", "c/", "f/").flatMap(prefix -> invalidValues.stream()
                .map(value -> DynamicTest.dynamicTest(prefix + value,
                        () -> assertThrows(IllegalArgumentException.class, () -> parser.parse(prefix + value)))));
    }

    @Test
    void parse_duplicateAndUnexpectedArguments_rejectsWithoutKeepingPreviousValues() {
        for (String prefix : List.of("cal/", "p/", "c/", "f/")) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(prefix + "1 " + prefix + "2"));
        }
        for (String arguments : List.of("d/1", "CAL/2000", "P/100", "c/1 extra", "cal2000", "/2000",
                "cal/2000p/100", "cal/2000/p/100", "cal/ 2000", "cal/2000 trailing/1")) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(arguments), arguments);
        }
        NutritionTargets next = parser.parse("p/100");
        assertNull(next.getCalories());
        assertEquals(100, next.getProtein());
    }
}
