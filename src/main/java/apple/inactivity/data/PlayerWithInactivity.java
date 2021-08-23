package apple.inactivity.data;

import apple.discord.acd.reaction.gui.GuiEntryStringable;
import apple.inactivity.utils.Pretty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static apple.inactivity.discord.reactions.MessageInactivity.MILLIS_IN_DAY;

public class PlayerWithInactivity implements GuiEntryStringable {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final long OLD_TIME = 1000 * 60 * 30;

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String uuid;
    public long lastJoined;
    public String username;
    public String rank;
    private final long timeRetrieved;

    public PlayerWithInactivity(String userName, String lastJoined, String rank, String uuid) {
        try {
            this.lastJoined = dateFormat.parse(lastJoined).getTime();
        } catch (ParseException e) {
            this.lastJoined = System.currentTimeMillis() + MILLIS_IN_DAY + MILLIS_IN_DAY / 2;
        }
        this.uuid = uuid;
        this.username = userName;
        this.rank = rank;
        this.timeRetrieved = System.currentTimeMillis();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerWithInactivity && uuid.equals(((PlayerWithInactivity) obj).uuid);
    }

    public boolean isOld() {
        return System.currentTimeMillis() - timeRetrieved > OLD_TIME;
    }

    @Override
    public String asEntryString(int i, int i1) {
        long days = (System.currentTimeMillis() - lastJoined) / MILLIS_IN_DAY;
        String daysString;
        if (days < 0)
            daysString = "Error";
        else
            daysString = days + " day" + (days == 1 ? "" : "s");
        return String.format("|%4d. %-30s| %-25s| %-25s|",
                i + 1,
                Pretty.limit(username, 30),
                Pretty.uppercaseFirst(rank),
                daysString);
    }
}
