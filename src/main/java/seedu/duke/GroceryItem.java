package seedu.duke;

import java.math.BigDecimal;
import java.util.Objects;

/** Represents an ingredient and its required quantity on a grocery list. */
public class GroceryItem {
    private final String name;
    private final BigDecimal quantity;
    private final String unit;

    /** Creates a grocery item with the given name, quantity, and unit. */
    public GroceryItem(String name, BigDecimal quantity, String unit) {
        this.name = Objects.requireNonNull(name, "name").trim();
        this.quantity = Objects.requireNonNull(quantity, "quantity");
        this.unit = Objects.requireNonNull(unit, "unit").trim();
        if (this.name.isEmpty() || this.unit.isEmpty() || quantity.signum() < 0) {
            throw new IllegalArgumentException("Name and unit must be non-empty and quantity must not be negative.");
        }
    }

    /** Returns the ingredient name. */
    public String getName() {
        return name;
    }

    /** Returns the required amount. */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /** Returns the quantity unit, such as g or ml. */
    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return name + ": " + quantity.stripTrailingZeros().toPlainString() + unit;
    }
}
