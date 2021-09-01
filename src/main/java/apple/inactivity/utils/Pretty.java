package apple.inactivity.utils;

import java.text.NumberFormat;

public class Pretty {
    private static final int NUM_OF_CHARS_PROGRESS = 40;

    public static String getProgress(double progress) {
        StringBuilder result = new StringBuilder();
        int length = (int) (progress * NUM_OF_CHARS_PROGRESS);
        result.append("\u2588".repeat(Math.max(0, length)));
        length = NUM_OF_CHARS_PROGRESS - length;
        result.append("\u2591".repeat(Math.max(0, length)));
        return result.toString();
    }

    public static String uppercaseFirst(String s) {
        if (s.isEmpty()) return s;
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            chars[i] = Character.toLowerCase(chars[i]);
        }
        return new String(chars);
    }

    public static String limit(String s, int limit) {
        if (s == null) return "";
        if (s.length() > limit) return s.substring(0, limit - 3) + "...";
        return s;
    }

    public static String commas(long n) {
        return NumberFormat.getIntegerInstance().format(n);
    }

}
