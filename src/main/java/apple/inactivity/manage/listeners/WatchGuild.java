package apple.inactivity.manage.listeners;

import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.Servers;
import apple.inactivity.wynncraft.WynnPlayerInactivity;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.guild.WynnGuild;
import apple.inactivity.wynncraft.guild.WynnGuildMember;
import apple.inactivity.wynncraft.player.WynnInactivePlayer;
import apple.utilities.structures.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class WatchGuild {
    public static final Comparator<? super WatchGuild> COMPARATOR = ((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.guildName, o2.guildName));

    private final Map<UUID, InactivityListener> listeners = new HashMap<>();
    private final HashMap<UUID, WatchedPlayer> players = new HashMap<>();
    private int daysInactiveToTrigger = 14;
    private int daysToRepeat = 7;
    private boolean shouldRepeat = true;
    private ArrayList<Pair<String, UUID>> ignoreUUIDs = new ArrayList<>();
    private final UUID uuid = UUID.randomUUID();
    private String guildTag;
    private String guildName;
    private long discordServerId;

    private transient ServerManager serverManager = null;

    // for gson
    public WatchGuild() {
    }

    public WatchGuild(String guildName, String guildTag, long serverId) {
        this.guildTag = guildTag;
        this.guildName = guildName;
        this.discordServerId = serverId;
    }

    public synchronized void verifyServerManager() {
        if (serverManager == null) {
            serverManager = Servers.getOrMake(discordServerId);
        }
    }

    public synchronized void watchPlayers(AggregatedWatchesByGuild callback) {
        WynncraftService.queue(WynncraftService.WynnRequestPriority.PRIMARY, guildName, wynnGuild -> verifyPlayerList(wynnGuild, callback));
    }

    private synchronized void verifyPlayerList(WynnGuild wynnGuild, AggregatedWatchesByGuild callback) {
        // make sure players exist if they are in the guild
        // and are removed if they are not in the guild
        HashSet<UUID> memberList = new HashSet<>(Arrays.asList(wynnGuild.members)).stream().map(o -> UUID.fromString(o.uuid)).collect(Collectors.toCollection(HashSet::new));
        players.keySet().removeIf(o -> !memberList.contains(o));
        for (WynnGuildMember member : wynnGuild.members) {
            players.computeIfAbsent(UUID.fromString(member.uuid), (k) -> new WatchedPlayer());
        }
        // do the actual check
        for (Map.Entry<UUID, WatchedPlayer> player : players.entrySet()) {
            WynnPlayerInactivity.get().getPlayer(player.getKey(), p -> checkPlayer(p, callback));
        }
    }

    private synchronized void checkPlayer(WynnInactivePlayer player, AggregatedWatchesByGuild callback) {
        WatchedPlayer watchedPlayer = players.computeIfAbsent(player.getUUID(), (k) -> new WatchedPlayer());
        int nextCall = getNextInactiveDays(watchedPlayer.getLastCalled());
        callback.setNextInactiveDays(player.getPossibleDaysInactive());
        if (player.getPossibleDaysInactive() > nextCall) {
            WynnPlayerInactivity.get().updatePlayer(player.getUUID(), p -> doInactivityCheck(p, callback));
        }
    }

    private synchronized void doInactivityCheck(WynnInactivePlayer player, AggregatedWatchesByGuild callback) {
        WatchedPlayer watchedPlayer = players.computeIfAbsent(player.getUUID(), (k) -> new WatchedPlayer());
        verifyInactive(player, watchedPlayer);
        int daysInactive = player.getDaysInactive();
        callback.setNextInactiveDays(daysInactive);

        if (daysInactive >= getNextInactiveDays(watchedPlayer.getLastCalled())) {
            callTrigger(watchedPlayer, player.getName(), player.getUUID(), daysInactive);
        }
    }

    private synchronized void verifyInactive(WynnInactivePlayer player, WatchedPlayer watchedPlayer) {
        if (player.getDaysInactive() < watchedPlayer.getLastCalled()) {
            watchedPlayer.setLastCalled(0);
            save();
        }
    }

    public void save() {
        Servers.get().save(this);
    }


    private synchronized int getNextInactiveDays(int lastCalled) {
        int nextCall;
        if (lastCalled >= daysInactiveToTrigger) {
            // this is a player that has been inactive
            if ((lastCalled - daysInactiveToTrigger) % daysToRepeat == 0)
                nextCall = daysInactiveToTrigger + ((lastCalled - daysInactiveToTrigger) / daysToRepeat + 2) * (daysToRepeat);
            else
                nextCall = daysInactiveToTrigger + ((lastCalled - daysInactiveToTrigger) / daysToRepeat + 1) * (daysToRepeat);
        } else {
            nextCall = daysInactiveToTrigger;
        }
        return nextCall;
    }

    public synchronized int getDaysInactiveToTrigger() {
        return daysInactiveToTrigger;
    }

    public synchronized int getDaysToRepeat() {
        return daysToRepeat;
    }

    public synchronized ArrayList<Pair<String, UUID>> getIgnoreUUIDs() {
        return ignoreUUIDs;
    }

    public synchronized List<InactivityListener> getListeners() {
        ArrayList<InactivityListener> sorted = new ArrayList<>(listeners.values());
        sorted.sort(Comparator.comparing(InactivityListener::getUUID, UUID::compareTo));
        return sorted;
    }

    public synchronized void addListener(InactivityListener listener) {
        listeners.put(listener.getUUID(), listener);
    }

    public synchronized void incrementDaysInactive(int i) {
        this.daysInactiveToTrigger = Math.max(1, i + this.daysInactiveToTrigger);
    }

    public synchronized void incrementDaysToRepeat(int i) {
        this.daysToRepeat = Math.max(1, i + this.daysToRepeat);
    }

    public synchronized void callTestTrigger(String inactivityPlayer) {
        verifyServerManager();
        for (InactivityListener listener : listeners.values()) {
            listener.trigger(serverManager, daysInactiveToTrigger, inactivityPlayer, null);
        }
    }

    private synchronized void callTrigger(WatchedPlayer watchedPlayer, String inactivityPlayer, UUID uuid, int daysInactive) {
        watchedPlayer.setLastCalled(daysInactive);
        save();
        verifyServerManager();
        if (isIgnored(uuid)) return;
        for (InactivityListener listener : listeners.values()) {
            listener.trigger(serverManager, daysInactive, inactivityPlayer, uuid);
        }
    }

    private boolean isIgnored(@NotNull UUID uuid) {
        String uuidString = uuid.toString().replace("-", "");
        for (Pair<String, UUID> ignore : ignoreUUIDs) {
            if (ignore.getValue().equals(uuidString)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addIgnored(List<Pair<String, UUID>> usernameAndUUID) {
        this.ignoreUUIDs.addAll(usernameAndUUID);
        this.ignoreUUIDs = new ArrayList<>(new HashSet<>(this.ignoreUUIDs));
        this.ignoreUUIDs.sort((p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(p1.getKey(), p2.getKey()));
    }

    public synchronized void addIgnored(Pair<String, UUID> usernameAndUUID) {
        this.ignoreUUIDs.add(usernameAndUUID);
        this.ignoreUUIDs = new ArrayList<>(new HashSet<>(this.ignoreUUIDs));
        this.ignoreUUIDs.sort((p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(p1.getKey(), p2.getKey()));
    }

    public synchronized boolean isShouldRepeat() {
        return shouldRepeat;
    }

    public synchronized void toggleShouldRepeat() {
        this.shouldRepeat = !this.shouldRepeat;
    }

    public synchronized UUID getUUID() {
        return uuid;
    }

    public synchronized String getGuildName() {
        return this.guildName;
    }

    public synchronized String getGuildTag() {
        return this.guildTag;
    }

    public synchronized HashMap<UUID, WatchedPlayer> getPlayers() {
        return new HashMap<>(this.players);
    }

    public long getServerId() {
        return discordServerId;
    }

    public synchronized List<Long> getChannelIds() {
        List<Long> channels = new ArrayList<>();
        for (InactivityListener listener : listeners.values()) {
            channels.add(listener.getChannelId());
        }
        return channels;
    }

    public synchronized void removeListener(InactivityListener inactivityListener) {
        listeners.remove(inactivityListener.getUUID());
    }
}
