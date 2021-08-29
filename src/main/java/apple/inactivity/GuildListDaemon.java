package apple.inactivity;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.WynncraftGuildListResponse;
import apple.inactivity.wynncraft.WynncraftService;
import apple.utilities.request.AppleJsonFromURL;

public class GuildListDaemon implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                WynncraftService.get().queue(new AppleJsonFromURL<>(Links.GUILD_LIST, WynncraftGuildListResponse.class), response -> {
                    WynnDatabase.setGuilds(response.getGuilds());
                }).completeAndRun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(MillisTimeUnits.DAY);
            } catch (InterruptedException e) {
                e.printStackTrace(); // todo tell appleptr16
                // if we can't sleep, abort trying to
                break;
            }
        }
    }
}
