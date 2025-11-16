package com.danielele;

public class PathSanitizer
{
    private static final java.util.regex.Pattern RESERVED_NAMES =
            java.util.regex.Pattern.compile("\\b(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])\\b",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    public static String sanitize(String filename)
    {
        String[] parts = filename.split("[/\\\\]+");

        for (int i = 0; i < parts.length; i++)
        {
            parts[i] = sanitizePart(parts[i]);
        }

        String sanitized = String.join("\\", parts);
        return sanitized.length() > 240 ? sanitized.substring(0, 240) : sanitized;
    }

    private static String sanitizePart(String part)
    {
        part = part.trim();
        part = part.replaceAll("[<>:\"|?*]", "_")
                .replaceAll("\\s+", " ")
                .replaceAll("(?<= )$|^(?= )", "")
                .replaceAll("\\.+$", "");
        part = part.replaceAll("\\.\\{[0-9a-fA-F\\-]{36}\\}", "_GUID");

        java.util.regex.Matcher matcher = RESERVED_NAMES.matcher(part);
        StringBuffer result = new StringBuffer();
        while (matcher.find())
        {
            matcher.appendReplacement(result, "_ops_");
        }
        matcher.appendTail(result);

        return result.toString().isEmpty() ? "_" : result.toString();
    }
}