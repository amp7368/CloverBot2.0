package apple.inactivity.wynncraft;

import apple.inactivity.GuildListThread;
import apple.inactivity.Links;
import apple.inactivity.Pretty;
import apple.inactivity.data.PlayerWithInactivity;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetGuildPlayers {

    @Nullable
    public static List<PlayerWithInactivity> getGuildPlayers(String guildName, Message progressMessage) {
        try {
            InputStreamReader url = new InputStreamReader(new URL(String.format(Links.GUILD_STATS, guildName)).openConnection().getInputStream());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(url);
            JSONArray membersObject = (JSONArray) response.get("members");
            List<PlayerWithInactivity> players = new ArrayList<>();
            double progress = 0;
            int size = membersObject.size();
            for (Object memberObject : membersObject) {
                String uuid = ((JSONObject) memberObject).get("uuid").toString();
                String rank = ((JSONObject) memberObject).get("rank").toString();
                players.add(getPlayer(uuid, rank));
                progressMessage.editMessage(Pretty.getProgress(progress++ / size)).queue();
            }
            players.removeIf(Objects::isNull);
            return players;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PlayerWithInactivity getPlayer(String uuid, String rank) {
        try {
            InputStreamReader url = new InputStreamReader(new URL(String.format(Links.PLAYER_STATS, uuid)).openConnection().getInputStream());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) ((JSONArray) ((JSONObject) parser.parse(url)).get("data")).get(0);
            String userName = response.get("username").toString();
            JSONObject metaObject = (JSONObject) response.get("meta");
            String lastJoined = metaObject.get("lastJoin").toString();
            try {
                Thread.sleep(GuildListThread.REQUEST_SLEEP);
            } catch (InterruptedException ignored) {
            }
            return new PlayerWithInactivity(userName, lastJoined, rank);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }

    }
}
