package apple.inactivity.data;

import org.json.simple.parser.JSONParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PlayerWithInactivity {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public long lastJoined;
    public String username;
    public String rank;

    public PlayerWithInactivity(String userName, String lastJoined, String rank) {
        try {
            this.lastJoined = dateFormat.parse(lastJoined).getTime();
        } catch (ParseException e) {
            this.lastJoined = 0;
        }
        this.username = userName;
        this.rank = rank;
    }
}
