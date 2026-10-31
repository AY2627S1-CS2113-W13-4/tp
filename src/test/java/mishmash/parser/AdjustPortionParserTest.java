package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import mishmash.model.PortionAdjustment;

/**
 * Checks complete and defensive parsing of adjust-portion fields.
 */
class AdjustPortionParserTest {
    private final AdjustPortionParser parser = new AdjustPortionParser();

    @Test
    void parse_validFields_acceptsAnyOrderWhitespaceAndLeadingZeros() {
        assertEquals(new PortionAdjustment(2, 3, 4), parser.parse("d/2 m/3 portions/4"));
        assertEquals(new PortionAdjustment(2, 3, 4), parser.parse(" portions/004  d/ 002 m/03 "));
    }

    @Test
    void parse_missingDuplicateUnknownOrUnprefixedFields_rejectsInput() {
        List<String> invalidArguments = List.of(
                "", "d/1 m/2", "d/1 portions/2", "m/1 portions/2",
                "d/1 d/2 m/1 portions/2", "d/1 m/1 portions/2 extra",
                "d/1 m/1 p/2", "unexpected d/1 m/1 portions/2");

        for (String arguments : invalidArguments) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(arguments), arguments);
        }
    }

    @Test
    void parse_invalidNumbers_rejectsNonPositiveNonWholeAndOverflowValues() {
        List<String> invalidValues = List.of("0", "-1", "+1", "1.5", "one", "2147483648");
        for (String value : invalidValues) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse("d/1 m/1 portions/" + value), value);
            assertTrue(error.getMessage().startsWith("Portion count"), value);
        }
    }
}
