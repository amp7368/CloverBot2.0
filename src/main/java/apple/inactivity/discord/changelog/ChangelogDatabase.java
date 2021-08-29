package apple.inactivity.discord.changelog;

import apple.inactivity.CloverMain;
import apple.inactivity.utils.FileIOService;
import apple.utilities.database.AppleJsonDatabase;
import apple.utilities.request.AppleRequestQueue;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;

public class ChangelogDatabase implements AppleJsonDatabase<ChangelogDatabase> {
    private static ChangelogDatabase instance;
    private final HashSet<Long> usersThatHeardChangelog = new HashSet<>();

    public static void loadNow() {
        @Nullable ChangelogDatabase instanceTemp = new ChangelogDatabase().loadNow(ChangelogDatabase.class);
        if (instanceTemp == null) instance = new ChangelogDatabase();
        else instance = instanceTemp;
    }

    public static ChangelogDatabase get() {
        return instance;
    }

    public static void addMember(User member) {
        synchronized (get().usersThatHeardChangelog) {
            get().usersThatHeardChangelog.add(member.getIdLong());
            get().save();
        }
    }

    public static boolean hasHeardChangelog(long userId) {
        synchronized (get().usersThatHeardChangelog) {
            return get().usersThatHeardChangelog.contains(userId);
        }
    }

    @NotNull
    private static File getDbFolder() {
        return new File(AppleJsonDatabase.getDBFolder(CloverMain.class), "changelog");
    }

    @Override
    public File getDBFile() {
        return new File(getDbFolder(), "changelog.json");
    }

    @Override
    public AppleRequestQueue getSavingService() {
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
}
