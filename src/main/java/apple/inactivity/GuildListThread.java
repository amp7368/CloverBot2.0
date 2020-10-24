package apple.inactivity;

import apple.inactivity.wynncraft.GetGuildList;

import java.util.*;
import java.util.regex.Pattern;

public class GuildListThread extends Thread {
    private static final int REQUESTS_PER_20_MIN = 1000;
    public static final long REQUEST_SLEEP = 20 * 60 * 1000 / REQUESTS_PER_20_MIN;
    private static final long DAY = 1000 * 60 * 60 * 24;
    private static Set<String> guildNames = Collections.emptySet();

    public static List<String> getGuildName(String guildName) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + guildName + ".*", Pattern.CASE_INSENSITIVE);
        for (String name : guildNames) {
            if (pattern.matcher(name).matches()) {
                matches.add(name);
            }
        }
        return matches;
    }

    @Override
    public void run() {
        while (true) {
            guildNames = GetGuildList.getGuildList();
            System.out.println("Guild names retrieved");
            try {
                Thread.sleep(DAY);
            } catch (InterruptedException e) {
                e.printStackTrace(); // todo tell appleptr16
                // if we can't sleep, abort trying to
                break;
            }
        }
    }
}
