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

    public MessageHelp(ACD acd, MessageChannel channel, boolean showLegacy, boolean isAdmin) {
        super(acd, channel);
        this.showLegacy = showLegacy;
        if (showLegacy) {
            addPage(this::legacy);
        }
        addPage(this::activity);
        if (isAdmin) {
            addPage(this::admin);
        }
    }


    private Message legacy() {
        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help Page(" + (1 + page) + ")");
        embed.addField("c!activity v1 [guild]", "Request an inactivity report in a series of messages", false);
        message.setEmbeds(embed.build());
        return message.build();
    }

    private Message activity() {
        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help Page(" + (1 + page) + ")");
        embed.addField("c!activity [guild]", "Request an inactivity report with pages", false);
        embed.addField("c!stats [player_name or uuid]", "Gives some stats about a player", false);
        embed.addField("c!suggest", "Send a message with an optional attachment to appleptr16#5054, for an idea or bug", false);
        message.setEmbeds(embed.build());
        return message.build();
    }

    private Message admin() {
        MessageBuilder message = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help Page(" + (1 + page) + ")");
        embed.addField("c!clover", "Open a gui to manage your server settings", false);
        embed.addField("c!watch [guild]", "Open a gui to create a watch on a guild", false);
        message.setEmbeds(embed.build());
        return message.build();
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}
