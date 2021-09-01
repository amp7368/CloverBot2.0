package apple.inactivity;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.logging.LoggingNames;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.WynncraftGuildListResponse;
import apple.inactivity.wynncraft.WynncraftService;
import apple.utilities.request.AppleJsonFromURL;
import apple.utilities.util.ExceptionUnpackaging;
import org.slf4j.event.Level;

public class GuildListDaemon extends Thread {
    @Override
    public void run() {
        CloverMain.log("Daemon GuildList started", Level.INFO, LoggingNames.DAEMON);
        while (true) {
            try {
                WynncraftService.get().queue(new AppleJsonFromURL<>(Links.GUILD_LIST, WynncraftGuildListResponse.class), response -> {
                    WynnDatabase.setGuilds(response.getGuilds());
                }).completeAndRun();
            } catch (Exception e) {
                CloverMain.log("Exception in guild list daemon" + "\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.DAEMON);
            }
            try {
                Thread.sleep(MillisTimeUnits.DAY);
            } catch (InterruptedException e) {
                CloverMain.log("Exception sleeping in guild list daemon" + "\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.DAEMON);
                // if we can't sleep, abort trying to
                break;
            }
        }
        CloverMain.log("Daemon GuildList ended", Level.ERROR, LoggingNames.DAEMON);
    }
}
