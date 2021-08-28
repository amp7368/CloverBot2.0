package apple.inactivity.listeners;

import apple.utilities.structures.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WatchGuild {
    private final ArrayList<InactivityListener> listeners = new ArrayList<>();
    private int daysInactiveToTrigger = 14;
    private int daysToRepeat = 7;
    private boolean shouldRepeat = true;
    private ArrayList<Pair<String, String>> ignoreUUIDs = new ArrayList<>();

    public int getDaysInactiveToTrigger() {
        return daysInactiveToTrigger;
    }

    public int getDaysToRepeat() {
        return daysToRepeat;
    }

    public ArrayList<Pair<String, String>> getIgnoreUUIDs() {
        return ignoreUUIDs;
    }

    public List<InactivityListener> getListeners() {
        return listeners;
    }

    public void addListener(InactivityListener listener) {
        listeners.add(listener);
    }

    public void incrementDaysInactive(int i) {
        this.daysInactiveToTrigger += i;
    }

    public void incrementDaysToRepeat(int i) {
        this.daysToRepeat += i;
    }

    public void callTestTrigger(String inactivityPlayer) {
        for (InactivityListener listener : listeners) {
            listener.trigger(daysInactiveToTrigger, inactivityPlayer);
        }
    }

    public void addIgnored(List<Pair<String, String>> usernameAndUUID) {
        this.ignoreUUIDs.addAll(usernameAndUUID);
        this.ignoreUUIDs = new ArrayList<>(new HashSet<>(this.ignoreUUIDs));
        this.ignoreUUIDs.sort((p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(p1.getKey(), p2.getKey()));
    }

    public boolean isShouldRepeat() {
        return shouldRepeat;
    }

    public void toggleShouldRepeat() {
        this.shouldRepeat = !this.shouldRepeat;
    }
}
