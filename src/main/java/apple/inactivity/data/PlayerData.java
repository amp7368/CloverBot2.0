package apple.inactivity.data;

import apple.inactivity.utils.GetFromObject;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static apple.inactivity.discord.reactions.MessageInactivity.MILLIS_IN_DAY;

public class PlayerData {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    @Nullable
    public final String rank;
    public final int playtimeInHours;
    public final int chestsFound;
    public final int blocksWalked;
    public final int mobsKilled;
    public long lastJoined;
    public final String userName;

    public PlayerData(JSONObject response) {
        this.userName = response.get("username").toString();
        JSONObject metaObject = (JSONObject) response.get("meta");
        try {
            this.lastJoined = dateFormat.parse(metaObject.get("lastJoin").toString()).getTime();
        } catch (ParseException e) {
            this.lastJoined = System.currentTimeMillis() + MILLIS_IN_DAY + MILLIS_IN_DAY / 2;
        }
        Object tag = ((JSONObject) metaObject.get("tag")).get("value");
        rank = tag == null ? null : tag.toString();
        long playtimeInHoursTemp = GetFromObject.getLong(metaObject.get("playtime"));
        if (GetFromObject.longFail(playtimeInHoursTemp)) {
            playtimeInHours = -1;
        } else {
            playtimeInHours = (int) Math.floor(playtimeInHoursTemp / 60.0 * 4.7);
        }

        // global section

        JSONObject globalObject = (JSONObject) response.get("global");
        int chestsFoundTemp = GetFromObject.getInt(globalObject.get("chestsFound"));
        chestsFound = GetFromObject.intFail(chestsFoundTemp) ? -1 : chestsFoundTemp;
        int blocksWalkedTemp = GetFromObject.getInt(globalObject.get("blocksWalked"));
        blocksWalked = GetFromObject.intFail(blocksWalkedTemp) ? -1 : blocksWalkedTemp;
        int mobsKilledTemp = GetFromObject.getInt(globalObject.get("mobsKilled"));
        mobsKilled = GetFromObject.intFail(mobsKilledTemp) ? -1 : mobsKilledTemp;
    }
}
