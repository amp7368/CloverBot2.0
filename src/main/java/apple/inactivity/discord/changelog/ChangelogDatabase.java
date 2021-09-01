package apple.inactivity.discord.changelog;

import apple.inactivity.CloverMain;
import apple.inactivity.logging.LoggingNames;
import apple.inactivity.utils.FileIOService;
import apple.utilities.database.AppleJsonDatabase;
import apple.utilities.database.SaveFileable;
import apple.utilities.database.queue.AppleJsonDatabaseManager;
import apple.utilities.request.AppleRequestQueue;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class ChangelogDatabase implements AppleJsonDatabaseManager<ChangelogDatabase>, SaveFileable {
    private static ChangelogDatabase instance;
    private final HashSet<Long> usersThatHeardChangelog = new HashSet<>();

    public static void loadNow() {
        @NotNull Collection<ChangelogDatabase> instanceTemp = new ChangelogDatabase().loadAllNow(ChangelogDatabase.class);
        instance = instanceTemp.stream().findFirst().orElseGet(ChangelogDatabase::new);
        CloverMain.log("Discord Changelog DB loaded", Level.INFO, LoggingNames.CLOVER);
    }

    public static ChangelogDatabase get() {
        return instance;
    }

    public static void addMember(User member) {
        synchronized (get().usersThatHeardChangelog) {
            get().usersThatHeardChangelog.add(member.getIdLong());
            get().save(get());
        }
    }

    public static boolean hasHeardChangelog(long userId) {
        synchronized (get().usersThatHeardChangelog) {
            return get().usersThatHeardChangelog.contains(userId);
        }
    }

    @Override
    public File getDBFolder() {
        return new File(AppleJsonDatabase.getDBFolder(CloverMain.class), "changelog");
    }

    @Override
    public AppleRequestQueue getIOService() {
        return FileIOService.get();
    }

    @Override
    public RequestSettingsBuilderVoid getSavingSettings() {
        return RequestSettingsBuilderVoid.VOID;
    }

    @Override
    public RequestSettingsBuilder<ChangelogDatabase> getLoadingSettings() {
        return RequestSettingsBuilder.empty();
    }

    @Override
    public String getSaveFileName() {
        return "changelog.json";
    }
}
