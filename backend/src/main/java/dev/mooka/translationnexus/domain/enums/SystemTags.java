package dev.mooka.translationnexus.domain.enums;

public final class SystemTags {
    private SystemTags() {}

    public static final String SKIP = "Skip";
    public static final String OBSOLETE = "Obsolete";
    public static final String SOURCE_CHANGED = "Source Changed";
    public static final String MISSING_SOURCE = "Missing Source";
    public static final String COMPLETE = "Complete";

    public static boolean isSystemTag(String tag) {
        return SKIP.equalsIgnoreCase(tag)
                || OBSOLETE.equalsIgnoreCase(tag)
                || SOURCE_CHANGED.equalsIgnoreCase(tag)
                || MISSING_SOURCE.equalsIgnoreCase(tag)
                || COMPLETE.equalsIgnoreCase(tag);
    }
}
