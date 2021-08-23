package apple.inactivity.discord.reactions;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.inactivity.data.PlayerData;
import apple.inactivity.utils.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static apple.inactivity.discord.reactions.MessageInactivity.MILLIS_IN_DAY;

public class MessageStats extends ACDGuiPageable {
    private static final Map<String, Integer> rankColors = new HashMap<>() {
        {
            put("DEFAULT", 0x656665);
            put("HERO", 0xba36d1);
            put("VIP+", 0x21ccd9);
            put("VIP", 0X36d158);
        }
    };
    private final PlayerData player;

    public MessageStats(ACD acd, PlayerData player, MessageChannel channel) {
        super(acd, channel);
        this.player = player;
    }

    @Override
    protected long getMillisToOld() {
        return 0;
    }

    public Message makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("%s %s", player.userName, Pretty.uppercaseFirst(player.rank == null ? "" : String.format("[%s]", player.rank))));
        switch (page) {
            case 0:
                embed.setDescription(buildPage1());
        }
        embed.setColor(rankColors.get(Objects.requireNonNullElse(player.rank, "DEFAULT")));
        return new MessageBuilder(embed.build()).build();
    }

    private String buildPage1() {
        long days = (System.currentTimeMillis() - player.lastJoined) / MILLIS_IN_DAY;

        StringBuilder text = new StringBuilder();
        text.append(String.format("**%d day%s** since last active\n", days, days == 1 ? "" : "s"));
        text.append(String.format("**%s hr%s** played\n", Pretty.commas(player.playtimeInHours), player.playtimeInHours == 1 ? "" : "s"));
        text.append(String.format("**%s chests** found\n", Pretty.commas(player.chestsFound)));
        text.append(String.format("**%s blocks** walked\n", Pretty.commas(player.blocksWalked)));
        text.append(String.format("**%s mobs** killed\n", Pretty.commas(player.mobsKilled)));
        return text.toString();
    }

    @Override
    public long getId() {
        return message.getIdLong();
    }
}
