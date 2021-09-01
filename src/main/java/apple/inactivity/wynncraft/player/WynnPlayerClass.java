package apple.inactivity.wynncraft.player;

import apple.inactivity.utils.Pretty;

public class WynnPlayerClass {
    public String name;
    public int level;
    public int itemsIdentified;
    public int mobsKilled;
    public int chestsFound;
    public int blocksWalked;
    public int logins;
    public int deaths;
    public long playtimes;
    public WynnPlayerClassGameMode gamemode;
    public WynnPlayerClassProfessions professions;

    public String getName() {
        StringBuilder pretty = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isAlphabetic(c)) pretty.append(c);
        }
        return Pretty.uppercaseFirst(pretty.toString());
    }
}
