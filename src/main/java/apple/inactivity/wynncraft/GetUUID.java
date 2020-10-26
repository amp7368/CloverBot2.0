package apple.inactivity.wynncraft;

import apple.inactivity.Links;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GetUUID {
    public static String getUuid(String playerName) {
        try {
            InputStreamReader url = new InputStreamReader(new URL(String.format(Links.PLAYER_STATS, playerName)).openConnection().getInputStream());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(url);
            return response.get("id").toString();
        } catch (IOException | ParseException | NullPointerException | ClassCastException e) {
            return null;
        }
    }
}
