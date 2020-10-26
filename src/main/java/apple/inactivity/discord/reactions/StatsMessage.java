package apple.inactivity.discord.reactions;

import apple.inactivity.Pretty;
import apple.inactivity.data.PlayerData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static apple.inactivity.discord.reactions.InactivityMessage.MILLIS_IN_DAY;

public class StatsMessage implements ReactableMessage {
    private static final Map<String, Integer> rankColors = new HashMap<>() {
        {
            put("DEFAULT", 0x656665);
            put("HERO", 0xba36d1);
            put("VIP+", 0x21ccd9);
            put("VIP", 0X36d158);
        }
    };
    private Message message;
    private PlayerData player;
    private int page = 0;
    private long lastUpdated = System.currentTimeMillis();

    public StatsMessage(PlayerData player, MessageChannel channel) {
        this.player = player;
        message = channel.sendMessage(makeMessage()).complete();
    }

    private MessageEmbed makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("%s %s", player.userName, Pretty.uppercaseFirst(player.rank == null ? "" : String.format("[%s]", player.rank))));
        switch (page) {
            case 0:
                embed.setDescription(buildPage1());
        }
        embed.setColor(rankColors.get(Objects.requireNonNullElse(player.rank, "DEFAULT")));
        return embed.build();
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
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {

    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }
}
