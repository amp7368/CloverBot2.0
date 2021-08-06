package apple.inactivity.wynncraft;

import apple.inactivity.utils.Links;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GetUUID {
    public static String getUuid(String playerName) {
        try {
            InputStreamReader url = new InputStreamReader(new URL(String.format(Links.GET_UUID, playerName, System.currentTimeMillis())).openConnection().getInputStream());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(url);
            return expandUuid(response.get("id").toString());
        } catch (IOException | ParseException | NullPointerException | ClassCastException e) {
            return null;
        }
    }

    private static String expandUuid(String id) {
        StringBuilder expanded = new StringBuilder(id);
        expanded.insert(8, '-');
        expanded.insert(13, '-');
        expanded.insert(18, '-');
        expanded.insert(23, '-');
        return expanded.toString();
    }
}
