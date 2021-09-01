package apple.inactivity;

import apple.inactivity.cache.VerifyDiscordCache;
import apple.inactivity.discord.DiscordBot;
import apple.inactivity.discord.changelog.ChangelogDatabase;
import apple.inactivity.logging.DailyStatistics;
import apple.inactivity.logging.ErrorLogger;
import apple.inactivity.logging.LoggingNames;
import apple.inactivity.manage.Servers;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.WynnPlayerInactivity;
import apple.utilities.logging.AppleLoggerManager;
import apple.utilities.logging.AppleLoggerName;
import apple.utilities.util.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloverMain {
    public static final CloverConfig CONFIG = CloverConfig.load();
    private static final AppleLoggerManager LOGGER;

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("--yy.MM.dd--hh'h'mm'm'");
        String dateString = CONFIG.getVersionIsDate() ? dateFormat.format(new Date()) : "";
        System.setProperty("logFile.version", dateString);
        System.setProperty("logFile.shouldAppend", String.valueOf(CONFIG.isShouldAppend()));
        LOGGER = new AppleLoggerManager(
                ArrayUtils.combine(LoggingNames.values(), new ErrorLogger(), AppleLoggerName[]::new),
                LoggerFactory.getLogger("clover")
        );
    }

    public static void main(String[] args) throws LoginException, SQLException, ClassNotFoundException, IOException {
        System.out.println("CloverBot starting");
        log("CloverBot starting", Level.INFO, LoggingNames.CLOVER);
        VerifyDiscordCache.connect();
        new WynnPlayerInactivity().loadAllNow();
        Servers.loadNow();
        WynnDatabase.loadDatabase();
        ChangelogDatabase.loadNow();
        WatchGuildDaemon.get().start();
        new DiscordBot();
        DailyStatistics.get().init();
        new GuildListDaemon().start();
        log("CloverBot started", Level.INFO, LoggingNames.CLOVER);
        System.out.println("CloverBot started");
    }

    public static void log(String msg, Level lvl, LoggingNames... loggerName) {
        LOGGER.log(msg, lvl, loggerName);
    }
}
