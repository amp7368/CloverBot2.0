package apple.inactivity.discord.activity;

import apple.discord.acd.reaction.gui.GuiEntryStringable;
import apple.inactivity.utils.Pretty;
import apple.inactivity.wynncraft.player.WynnPlayer;

public class InactivityWynnPlayer implements GuiEntryStringable {
    public WynnPlayer player;

    public InactivityWynnPlayer(WynnPlayer player) {
        this.player = player;
    }

    @Override
    public String asEntryString(int indexInPage, int indexInList) {
        long days = player.inactivity();
        String daysString;
        if (days < 0)
            daysString = "Error";
        else
            daysString = days + " day" + (days == 1 ? "" : "s");
        return String.format("|%4d. %-30s| %-25s| %-25s|",
                indexInList + 1,
                Pretty.limit(player.username, 30),
                Pretty.uppercaseFirst(player.guildMember.rank),
                daysString);
    }
}
