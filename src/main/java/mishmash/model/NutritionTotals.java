package mishmash.model;

/**
 * Holds immutable nutrition totals, using long values so sums can exceed the maximum integer value.
 *
 * @param calories The total energy in kilocalories.
 * @param protein The total protein in grams.
 * @param carbs The total carbohydrates in grams.
 * @param fats The total fats in grams.
 */
public record NutritionTotals(long calories, long protein, long carbs, long fats) {
}
