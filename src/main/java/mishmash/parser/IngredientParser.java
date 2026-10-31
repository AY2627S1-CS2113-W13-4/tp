package mishmash.parser;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mishmash.model.GroceryItem;

/**
 * Parses one ingredient value shared by recipe and planned-meal commands.
 */
public final class IngredientParser {
    private static final Pattern INGREDIENT_PATTERN =
            Pattern.compile("([^:]+):\\s*([0-9]+(?:\\.[0-9]+)?)\\s*([A-Za-z]+)");

    /**
     * Parses an ingredient such as {@code Chicken Breast:200g}.
     *
     * @param value The ingredient name, quantity, and unit without an {@code i/} prefix.
     * @return The validated ingredient.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If its name, quantity, unit, or format is invalid.
     */
    public GroceryItem parse(String value) {
        if (value.startsWith(":")) {
            throw new IllegalArgumentException("Ingredient name must not be blank.");
        }
        Matcher matcher = INGREDIENT_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid ingredient format. Use i/INGREDIENT:QUANTITYUNIT, for example i/Chicken Breast:200g.");
        }

        BigDecimal quantity = new BigDecimal(matcher.group(2));
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Ingredient quantity must be greater than zero.");
        }
        if (matcher.group(1).isBlank()) {
            throw new IllegalArgumentException("Ingredient name must not be blank.");
        }
        return new GroceryItem(matcher.group(1), quantity, matcher.group(3));
    }
}
