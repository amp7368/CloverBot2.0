package apple.inactivity.data;

import org.json.simple.parser.JSONParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static apple.inactivity.discord.reactions.InactivityMessage.MILLIS_IN_DAY;

public class PlayerWithInactivity {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final long OLD_TIME = 1000 * 60 * 30;

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String uuid;
    public long lastJoined;
    public String username;
    public String rank;
    private long timeRetrieved;

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
}
