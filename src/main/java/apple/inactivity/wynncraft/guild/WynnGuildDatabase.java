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
    private Map<String, WynnGuildHeader> guilds = new HashMap<>();

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
            int i = 0;
            synchronized (instance) {
                for (File file : files) {
                    requests[i++] = FileIOService.get().queue(new AppleJsonFromFile<>(file, new TypeToken<HashMap<String, WynnGuildHeader>>() {
                            }),
                            (map) -> {
                                synchronized (instance) {
                                    for (WynnGuildHeader header : map.values()) {
                                        get().guilds.put(header.name, header);
                                    }
                                }
                            }
                    );
                }
            }
            for (i = 0; i < requests.length; i++) {
                requests[i].completeAndRun();
            }
        }
    }

    public static void setGuilds(String[] guilds) {
        synchronized (instance) {
            Map<String, WynnGuildHeader> wynncraftGuilds = new HashMap<>();
            for (String guild : guilds) {
                WynnGuildHeader guildHeader = get().guilds.get(guild);
                if (guildHeader == null) {
                    guildHeader = new WynnGuildHeader(guild);
                    wynncraftGuilds.putIfAbsent(guild, guildHeader);
                    if (get().guildsRequestedToBeUpdated.add(guild)) {
                        File file = new File(GUILD_FOLDER, guild.charAt(0) + "-guilds.json");
                        WynncraftService.queue(WynncraftService.WynnRequestPriority.BACKGROUND, guild, (wynnGuild) -> {
                            addGuild(wynnGuild);
                            HashMap<String, WynnGuildHeader> headersToSave = getHeaders(String.valueOf(wynnGuild.name.charAt(0)));
                            FileIOService.get().queueVoid(new AppleJsonToFile(file, headersToSave));
                        });
                    }
                }
                wynncraftGuilds.put(guildHeader.name, guildHeader);
            }
            get().guilds = wynncraftGuilds;
        }
        System.out.println("done setting guilds");
    }

    private static HashMap<String, WynnGuildHeader> getHeaders(String startingLetter) {
        synchronized (instance) {
            HashMap<String, WynnGuildHeader> headers = new HashMap<>();
            for (WynnGuildHeader guild : get().guilds.values()) {
                if (guild.name.startsWith(startingLetter))
                    headers.put(guild.name, guild);
            }
            return headers;
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
}
