package apple.inactivity.wynncraft;

import java.util.ArrayList;
import java.util.List;

public class WynncraftGuildListResponse {
    private String[] guilds;

    public String[] getGuilds() {
        return new ArrayList<>(List.of(guilds)).toArray(String[]::new);
    }
}
