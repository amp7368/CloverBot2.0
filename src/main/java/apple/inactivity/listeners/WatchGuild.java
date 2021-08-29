package apple.inactivity.listeners;

import apple.inactivity.manage.Servers;
import apple.inactivity.wynncraft.WynnPlayerInactivity;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.guild.WynnGuild;
import apple.inactivity.wynncraft.guild.WynnGuildMember;
import apple.inactivity.wynncraft.player.WynnInactivePlayer;
import apple.utilities.structures.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class WatchGuild {
    public static final Comparator<? super WatchGuild> COMPARATOR = ((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.guildName, o2.guildName));

    private final ArrayList<InactivityListener> listeners = new ArrayList<>();
    private final HashMap<UUID, WatchedPlayer> players = new HashMap<>();
    private int daysInactiveToTrigger = 14;
    private int daysToRepeat = 7;
    private boolean shouldRepeat = true;
    private ArrayList<Pair<String, String>> ignoreUUIDs = new ArrayList<>();
    private final UUID uuid = UUID.randomUUID();
    private String guildTag;
    private String guildName;
    private long discordServerId;

    // for gson
    public WatchGuild() {
    }

    public WatchGuild(String guildName, String guildTag, long serverId) {
        this.guildTag = guildTag;
        this.guildName = guildName;
        this.discordServerId = serverId;
    }

    public synchronized void watchPlayers() {
        WynncraftService.queue(WynncraftService.WynnRequestPriority.PRIMARY, guildName, this::verifyPlayerList);
    }

    private synchronized void verifyPlayerList(WynnGuild wynnGuild) {
        // make sure players exist if they are in the guild
        // and are removed if they are not in the guild
        HashSet<UUID> memberList = new HashSet<>(Arrays.asList(wynnGuild.members)).stream().map(o -> UUID.fromString(o.uuid)).collect(Collectors.toCollection(HashSet::new));
        players.keySet().removeIf(o -> !memberList.contains(o));
        for (WynnGuildMember member : wynnGuild.members) {
            players.computeIfAbsent(UUID.fromString(member.uuid), (k) -> new WatchedPlayer());
        }
        // do the actual check
        for (Map.Entry<UUID, WatchedPlayer> player : players.entrySet()) {
            WynnPlayerInactivity.get().getPlayer(player.getKey(), this::checkPlayer);
        }
    }

    private synchronized void checkPlayer(WynnInactivePlayer player) {
        WatchedPlayer watchedPlayer = players.computeIfAbsent(player.getUUID(), (k) -> new WatchedPlayer());
        int nextCall = getNextInactiveDays(watchedPlayer.getLastCalled());
        if (player.getPossibleDaysInactive() > nextCall) {
            WynnPlayerInactivity.get().updatePlayer(player.getUUID(), this::doInactivityCheck);
        }
    }

    private synchronized void doInactivityCheck(WynnInactivePlayer player) {
        WatchedPlayer watchedPlayer = players.computeIfAbsent(player.getUUID(), (k) -> new WatchedPlayer());
        verifyInactive(player, watchedPlayer);
        int daysInactive = player.getDaysInactive();
        System.out.println(daysInactive + " " + watchedPlayer.getLastCalled() + " " + getNextInactiveDays(watchedPlayer.getLastCalled()));

        if (daysInactive >= getNextInactiveDays(watchedPlayer.getLastCalled())) {
            callTrigger(watchedPlayer, player.getName(), daysInactive);
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
                nextCall = daysInactiveToTrigger + ((lastCalled - daysInactiveToTrigger) / daysToRepeat+2) * (daysToRepeat );
            else
                nextCall = daysInactiveToTrigger + ((lastCalled - daysInactiveToTrigger) / daysToRepeat+ 1) * (daysToRepeat );
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

    public synchronized ArrayList<Pair<String, String>> getIgnoreUUIDs() {
        return ignoreUUIDs;
    }

    public synchronized List<InactivityListener> getListeners() {
        return listeners;
    }

    public synchronized void addListener(InactivityListener listener) {
        listeners.add(listener);
    }

    public synchronized void incrementDaysInactive(int i) {
        this.daysInactiveToTrigger += i;
    }

    public synchronized void incrementDaysToRepeat(int i) {
        this.daysToRepeat += i;
    }

    public synchronized void callTestTrigger(String inactivityPlayer) {
        for (InactivityListener listener : listeners) {
            listener.trigger(null, daysInactiveToTrigger, inactivityPlayer);
        }
    }

    private synchronized void callTrigger(WatchedPlayer watchedPlayer, String inactivityPlayer, int daysInactive) {
        watchedPlayer.setLastCalled(daysInactive);
        save();
        for (InactivityListener listener : listeners) {
            listener.trigger(watchedPlayer, daysInactive, inactivityPlayer);
        }
    }

    public synchronized void addIgnored(List<Pair<String, String>> usernameAndUUID) {
        this.ignoreUUIDs.addAll(usernameAndUUID);
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

    public long getServerId() {
        return discordServerId;
    }
}
