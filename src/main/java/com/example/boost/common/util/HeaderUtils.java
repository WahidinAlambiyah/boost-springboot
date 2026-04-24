package com.example.boost.common.util;

/**
 * Domain-neutral helper for working with HTTP header values.
 *
 * <p>Usage: call {@link #normalize(String)} before passing header values into services so whitespace-only
 * content is converted to {@code null} and validation can be handled consistently by the application layer.</p>
 */
public final class HeaderUtils {

    private HeaderUtils() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
