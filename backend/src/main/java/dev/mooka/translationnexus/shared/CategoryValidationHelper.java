package dev.mooka.translationnexus.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CategoryValidationHelper {

    private CategoryValidationHelper() {}

    public static boolean hasExactlyOneWildcard(String pattern) {
        if (pattern == null) return false;
        int count = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '*') {
                count++;
            }
        }
        return count == 1;
    }

    public static String toRegex(String pattern) {
        if (pattern == null) return "";
        if ("*".equals(pattern)) {
            return "^(.*)$";
        }
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                sb.append("([^.]+)");
            } else if (c == '.') {
                sb.append("\\.");
            } else if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                sb.append(c);
            } else {
                sb.append("\\").append(c);
            }
        }
        sb.append("$");
        return sb.toString();
    }

    public static boolean matchPath(String keyCode, String pattern) {
        if (pattern == null || keyCode == null) {
            return false;
        }
        try {
            return Pattern.compile(toRegex(pattern)).matcher(keyCode).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractAlias(String keyCode, String pattern) {
        if (pattern == null || keyCode == null) {
            return null;
        }
        try {
            Pattern p = Pattern.compile(toRegex(pattern));
            Matcher m = p.matcher(keyCode);
            if (m.matches()) {
                return m.group(1);
            }
        } catch (Exception e) {
            // ignore
        }
        return keyCode; // fallback if matching failed somehow
    }
}
