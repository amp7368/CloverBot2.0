package apple.inactivity.wynncraft;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.guild.WynnGuildDatabase;
import apple.utilities.request.AppleJsonFromURL;

public class GuildListThread implements Runnable {
    @Override
    public void run() {
        while (true) {
            WynncraftService.get().queue(new AppleJsonFromURL<>(Links.GUILD_LIST, WynncraftGuildListResponse.class), response -> {
                WynnGuildDatabase.setGuilds(response.getGuilds());
            }).completeAndRun();
            System.out.println("Guild names retrieved");
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
