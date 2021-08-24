package apple.inactivity.discord.changelog;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.inactivity.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MessageChangelog extends ACDGuiPageable {
    public MessageChangelog(ACD acd, MessageChannel channel) {
        super(acd, channel);
        addPage(this::header);
    }

    private Message header() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Changelog Page(" + (page + 1) + ")");
        embed.setAuthor("appleptr16#5054");
        embed.setDescription("""
                Hey! I made some changes to Cloverbot!
                For starters, the activity command has been updated.
                You can also use previous activity commands
                c!help -legacy for a list of commands including legacy commands
                """);
        embed.setFooter(String.format("\"%s\" %s", DiscordBot.PREFIX + "changelog", "is the command to show this message if you want to read this again in the future"));
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }


    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.HOUR;
    }
}
