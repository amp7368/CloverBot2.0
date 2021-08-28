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
    private final String[] messages = new String[]{
            """
            Hey! I made some changes to Cloverbot!
                            
            For starters, the activity command has been updated.
            - You can now use Guild tags instead needing the full name
            - This is case insensitive unless there are multiple guilds with the same tag in which case it is case sensitive
            You can also now use previous activity commands
            Do 'c!help -legacy' for a list of commands including legacy commands
            """,
            """
            Next is a very old request
            Guild watches can now be set up!
            If you want to get notified when your members become inactive, you can now type 
            """
    };

    public MessageChangelog(ACD acd, MessageChannel channel) {
        super(acd, channel);
        for (int i = 0; i < messages.length; i++)
            addPage(this::header);
    }

    private Message header() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Changelog Page(" + (page + 1) + ")");
        embed.setAuthor("appleptr16#5054");
        embed.setDescription(messages[page]);
        embed.setFooter(String.format("\"%s\" %s", DiscordBot.PREFIX + "changelog", "is the command to show this message if you want to read this again in the future"));
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }


    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.HOUR;
    }
}
