package apple.inactivity;

import apple.inactivity.wynncraft.GetGuildPlayers;

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
}
