package apple.inactivity.wynncraft;

import apple.inactivity.Links;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GetGuildList {
    public static Set<String> getGuildList() {
        try {
            InputStreamReader url = new InputStreamReader(new URL(Links.GUILD_LIST).openConnection().getInputStream());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(url);
            JSONArray guilds = (JSONArray) response.get("guilds");
            Set<String> guildList = new HashSet<>();
            for (Object guild : guilds) {
                guildList.add(guild.toString());
            }
            return guildList;
        } catch (IOException | ParseException| ClassCastException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
}
