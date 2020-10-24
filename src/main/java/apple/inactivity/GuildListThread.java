package apple.inactivity;

import apple.inactivity.wynncraft.GetGuildList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GuildListThread extends Thread {
    private static final long DAY = 1000 * 60 * 60 * 24;
    private static Set<String> guildNames = Collections.emptySet();

    @Override
    public void run() {
        while (true) {
//            guildNames = GetGuildList.getGuildList();
            System.out.println("Guild names retrieved");
            for (String guildName : guildNames) {
                System.out.println(guildName);
            }
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
