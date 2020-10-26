package apple.inactivity.wynncraft;

import apple.inactivity.Links;
import apple.inactivity.data.PlayerData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GetPlayer {
    public static PlayerData getPlayer(String playerName) {
        try {
            InputStreamReader url = new InputStreamReader(new URL(String.format(Links.PLAYER_STATS, playerName)).openConnection().getInputStream());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) ((JSONArray) ((JSONObject) parser.parse(url)).get("data")).get(0);
            return new PlayerData(response);
        } catch (IOException | ParseException | NullPointerException| ClassCastException e) {
            return null;
        }
    }
}
