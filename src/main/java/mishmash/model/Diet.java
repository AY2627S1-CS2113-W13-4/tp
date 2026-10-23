package mishmash.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Named diets expressed as exclusions; MEAT_DERIVATIVE means land-animal meat derivatives. */
public enum Diet {
    NONE(),
    PESCATARIAN(FoodTag.BEEF, FoodTag.PORK, FoodTag.POULTRY,
            FoodTag.OTHER_MEAT, FoodTag.MEAT_DERIVATIVE, FoodTag.OTHER_ANIMAL_PRODUCT),
    VEGETARIAN(PESCATARIAN, FoodTag.FISH, FoodTag.CRUSTACEAN, FoodTag.MOLLUSC, FoodTag.GELATIN),
    LACTO_VEGETARIAN(VEGETARIAN, FoodTag.EGG),
    OVO_VEGETARIAN(VEGETARIAN, FoodTag.MILK, FoodTag.LACTOSE),
    VEGAN(VEGETARIAN, FoodTag.MILK, FoodTag.LACTOSE, FoodTag.EGG, FoodTag.HONEY);

    private final Set<FoodTag> excludedTags;

    Diet(FoodTag... tags) {
        excludedTags = Set.of(tags);
    }

    /** Extends an existing diet with additional excluded categories. */
    Diet(Diet base, FoodTag... additionalTags) {
        Set<FoodTag> combined = EnumSet.noneOf(FoodTag.class);
        combined.addAll(base.excludedTags);
        for (FoodTag tag : additionalTags) {
            combined.add(tag);
        }
        excludedTags = Set.copyOf(combined);
    }

    public boolean excludes(FoodTag tag) {
        return excludedTags.contains(Objects.requireNonNull(tag, "tag"));
    }

    /** Returns the command-line spelling of this diet. */
    public String label() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Lists canonical diet values directly from the supported enum constants. */
    public static String supportedLabels() {
        return Arrays.stream(values()).map(Diet::label).collect(Collectors.joining(", "));
    }

    /** Shares valid values and aliases between command errors and general help. */
    public static String inputHelp() {
        return "Valid diets: " + supportedLabels() + "." + System.lineSeparator()
                + "Aliases: omnivore (none), lacto-ovo-vegetarian (vegetarian).";
    }

    /** Accepts supported diet labels and explicit aliases; unsupported diets are errors. */
    public static Diet parse(String text) {
        String name = Objects.requireNonNull(text, "text").trim()
                .toUpperCase(Locale.ROOT).replace('-', '_');
        if (name.equals("OMNIVORE")) {
            return NONE;
        }
        if (name.equals("LACTO_OVO_VEGETARIAN")) {
            return VEGETARIAN;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid diet '" + text + "'."
                    + System.lineSeparator() + inputHelp(), e);
        }
    }
}
