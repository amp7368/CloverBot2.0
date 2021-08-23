package apple.inactivity.wynncraft;

import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.guild.WynnGuildDatabase;
import apple.utilities.request.AppleJsonFromURL;

public class GuildListThread implements Runnable {
    private static final int REQUESTS_PER_20_MIN = 1000;
    public static final long REQUEST_SLEEP = 20 * 60 * 1000 / REQUESTS_PER_20_MIN;
    private static final long DAY = 1000 * 60 * 60 * 24;

    @Override
    public void run() {
        while (true) {
            WynncraftService.get().queue(new AppleJsonFromURL<>(Links.GUILD_LIST, WynncraftGuildListResponse.class), response -> {
                System.out.println("Guild names retrieved");
                WynnGuildDatabase.setGuilds(response.getGuilds());
                System.out.println("done");
            }).completeAndRun();
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
