package apple.inactivity.data;

import org.json.simple.parser.JSONParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static apple.inactivity.discord.reactions.InactivityMessage.MILLIS_IN_DAY;

public class PlayerWithInactivity {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println(TimeZone.getTimeZone("GMT").getDisplayName());
    }

    public long lastJoined;
    public String username;
    public String rank;

    public PlayerWithInactivity(String userName, String lastJoined, String rank) {
        try {
            this.lastJoined = dateFormat.parse(lastJoined).getTime();
        } catch (ParseException e) {
            this.lastJoined = System.currentTimeMillis() + MILLIS_IN_DAY + MILLIS_IN_DAY / 2;
        }
        this.username = userName;
        this.rank = rank;
    }
}
