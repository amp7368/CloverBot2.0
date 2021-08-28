package apple.inactivity.wynncraft.guild;

import apple.inactivity.CloverMain;
import apple.inactivity.wynncraft.FileIOService;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.utilities.request.AppleJsonFromFile;
import apple.utilities.request.AppleJsonToFile;
import apple.utilities.request.AppleRequestService;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class WynnGuildDatabase {
    private static final File GUILD_FOLDER;
    private final static WynnGuildDatabase instance = new WynnGuildDatabase();

    static {
        List<String> list = Arrays.asList(CloverMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        GUILD_FOLDER = new File(String.join("/", list.subList(0, list.size() - 1)) + "/wynncraft/guild");
        GUILD_FOLDER.mkdirs();
    }

    private final Set<String> guildsRequestedToBeUpdated = new HashSet<>();
    private final Map<String, WynnPlayer> members = new HashMap<>();
    private final Map<String, WynnGuildHeader> guilds = new HashMap<>(20000);

    private static WynnGuildDatabase get() {
        return instance;
    }

    public static List<WynnGuildHeader> getFromGuildName(String guildName) {
        synchronized (instance) {
            List<WynnGuildHeader> matches = new ArrayList<>();
            for (WynnGuildHeader guild : get().guilds.values()) {
                if (guild.matchesTag(guildName)) {
                    matches.add(guild);
                }
            }
            if (!matches.isEmpty()) return matches;
            for (WynnGuildHeader guild : get().guilds.values()) {
                if (guild.matchesTagIgnoreCase(guildName)) {
                    matches.add(guild);
                }
            }
            if (!matches.isEmpty()) return matches;
            for (WynnGuildHeader guild : get().guilds.values()) {
                if (guild.matchesGuildName(guildName)) {
                    matches.add(guild);
                }
            }
            if (!matches.isEmpty()) return matches;
            for (WynnGuildHeader guild : get().guilds.values()) {
                if (guild.matchesGuildNameIgnoreCase(guildName)) {
                    matches.add(guild);
                }
            }
            return matches;
        }
    }

    public static void loadDatabase() {
        File[] files = GUILD_FOLDER.listFiles();
        if (files != null) {
            AppleRequestService.RequestHandler<?>[] requests = new AppleRequestService.RequestHandler<?>[files.length];
            synchronized (instance) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (!file.isDirectory()) {
                        requests[i] = FileIOService.get().queue(new AppleJsonFromFile<>(file, new TypeToken<HashMap<String, WynnGuildHeader>>() {
                                }),
                                (map) -> {
                                    synchronized (instance) {
                                        for (Map.Entry<String, WynnGuildHeader> header : map.entrySet()) {
                                            get().guilds.put(header.getKey(), header.getValue());
                                        }
                                    }
                                }
                        );
                    }
                }
            }
            for (AppleRequestService.RequestHandler<?> request : requests) {
                if (request != null) request.completeAndRun();
            }
        }
    }

    public static void setGuilds(String[] guilds) {
        synchronized (instance) {
            int i = 0, j = 0;
            for (String guild : guilds) {
                WynnGuildHeader guildHeader = get().guilds.get(guild);
                if (guildHeader == null || guildHeader.prefix == null) {
                    i++;
                    get().guilds.put(guild, new WynnGuildHeader(guild));
                    if (get().guildsRequestedToBeUpdated.add(guild)) {
                        WynncraftService.queue(WynncraftService.WynnRequestPriority.BACKGROUND, guild, (wynnGuild) -> {
                            addGuild(wynnGuild);

                            HashMap<String, WynnGuildHeader> headersToSave = getHeaders();
                            File file = new File(GUILD_FOLDER, "guilds_all.json");
                            FileIOService.get().queueVoid(new AppleJsonToFile(file, headersToSave));
                        });
                    }
                } else {
                    j++;
                }
            }
        }
    }

    private static HashMap<String, WynnGuildHeader> getHeaders() {
        synchronized (instance) {
            return new HashMap<>(get().guilds);
        }
    }

    private static void addGuild(WynnGuild wynnGuild) {
        synchronized (instance) {
            WynnGuildHeader header = wynnGuild.toHeader();
            get().guilds.put(header.name, header);
            get().guildsRequestedToBeUpdated.remove(header.name);
        }
    }

    public static void addMember(WynnPlayer member) {
        synchronized (instance) {
            get().members.put(member.uuid, member);
        }
    }

    @Nullable
    public static WynnPlayer getPlayer(String uuid) {
        synchronized (instance) {
            WynnPlayer member = get().members.get(uuid);
            if (member == null) return null;
            if (member.isOld()) {
                get().members.remove(uuid);
                return null;
            }
            return member;
        }
    }

    public static int getSize() {
        synchronized (instance) {
            return get().guilds.size();
        }
    }
}
