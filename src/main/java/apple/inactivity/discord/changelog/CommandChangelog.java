package apple.inactivity.discord.changelog;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandChangelog extends ACDCommand {
    public CommandChangelog(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "changelog")
    public void helpAdmin(MessageReceivedEvent event) {
        new MessageChangelog(acd, event.getChannel()).makeFirstMessage();
    }
}
