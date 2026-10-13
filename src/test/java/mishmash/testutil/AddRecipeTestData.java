package mishmash.testutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Shares recipe input fixtures between parser and application tests.
 */
public final class AddRecipeTestData {
    public static final String VALID_COMMAND = "add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g";

    private AddRecipeTestData() {
    }

    /**
     * Loads named invalid commands and their expected diagnostic fragments from the shared resource.
     *
     * @return The tab-separated case name, diagnostic, and command for each invalid input.
     * @throws IOException If the resource cannot be read.
     */
    public static List<String[]> invalidCases() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(AddRecipeTestData.class
                        .getResourceAsStream("/add-recipe-invalid.tsv")), StandardCharsets.UTF_8))) {
            return reader.lines().filter(line -> !line.startsWith("#"))
                    .map(line -> line.split("\t", 3)).toList();
        }
    }
}
