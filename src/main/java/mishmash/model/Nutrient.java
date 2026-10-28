package mishmash.model;

//@@author neil0618
/**
 * The four nutrients that can have a daily target, with the command prefix and wording used for each.
 *
 * <p>Keeping these in one enum lets the parser, the stored targets, and the viewer treat the
 * nutrients uniformly instead of repeating four near-identical branches.</p>
 */
public enum Nutrient {
    CALORIES("cal", "Calories", "kcal"),
    PROTEIN("p", "Protein", "g"),
    CARBS("c", "Carbs", "g"),
    FATS("f", "Fats", "g");

    private final String prefix;
    private final String label;
    private final String unit;

    /**
     * Creates a nutrient constant.
     *
     * @param prefix The command prefix without its slash.
     * @param label The display name.
     * @param unit The unit shown after a target.
     */
    Nutrient(String prefix, String label, String unit) {
        this.prefix = prefix;
        this.label = label;
        this.unit = unit;
    }

    /**
     * Returns the command prefix without its slash, e.g. {@code "cal"} for {@code cal/2000}.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the display name, e.g. {@code "Calories"}.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the unit of the target, {@code "kcal"} for calories and {@code "g"} otherwise.
     */
    public String getUnit() {
        return unit;
    }
}
//@@author
