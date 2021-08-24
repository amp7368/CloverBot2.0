package apple.inactivity.discord.help;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MessageHelp extends ACDGuiPageable {
    private final boolean showLegacy;

    public MessageHelp(ACD acd, MessageChannel channel, boolean showLegacy) {
        super(acd, channel);
        this.showLegacy = showLegacy;
        if (showLegacy) {
            addPage(this::legacy);
        }
        addPage(this::activity);
    }

    private Message legacy() {
        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help Page(" + (1 + page) + ")");
        embed.addField("c!activity v1 [guild]", "Request an inactivity report in a series of messages", false);
        embed.addField("c!activity v2 [guild]", "Request an inactivity report with pages", false);
        message.setEmbeds(embed.build());
        return message.build();
    }

    private Message activity() {
        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help Page(" + (1 + page) + ")");
        StringBuilder content = new StringBuilder();
        embed.addField("c!activity [guild]", "Gives an inactivity report for a guild. The guild name is case insensitive and a full guild name is not required", false);
        embed.addField("c!stats [player_name or uuid]", "Gives some stats about a player", false);
        embed.addField("c!suggest", "Send a message with an optional attachment to appleptr16#5054, for an idea or bug", false);
        embed.setDescription(content);
        message.setEmbeds(embed.build());
        return message.build();
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}
