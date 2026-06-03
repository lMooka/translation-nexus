package dev.mooka.translationnexus.shared;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates that a translated string contains exactly the same set of
 * {placeholder} tokens as the original English base value.
 */
public final class PlaceholderValidator {

    private static final Pattern PATTERN = Pattern.compile("\\{([^}]+)}");

    private PlaceholderValidator() {}

    public static Set<String> extract(String text) {
        Set<String> placeholders = new HashSet<>();
        if (text == null || text.isBlank()) return placeholders;
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    /**
     * Returns true if both texts contain identical placeholder sets.
     */
    public static boolean isValid(String baseValue, String translatedValue) {
        return extract(baseValue).equals(extract(translatedValue));
    }
}
