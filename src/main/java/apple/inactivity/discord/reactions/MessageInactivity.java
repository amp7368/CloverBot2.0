package apple.inactivity.discord.reactions;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.GuiReactionEmoji;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.discord.acd.reaction.gui.ACDGuiEntryList;
import apple.discord.acd.reaction.gui.GuiEntryBorder;
import apple.inactivity.utils.Pretty;
import apple.inactivity.data.PlayerWithInactivity;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nullable;
import java.util.List;

@GuiEntryBorder
public class MessageInactivity extends ACDGuiEntryList<PlayerWithInactivity> {
    private static final int ENTRIES_PER_PAGE = 15;
    public static final long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private int page = 0;
    private final String guildName;
    private final List<PlayerWithInactivity> playersInGuild;
    private final Message message;
    private final long timeNow = System.currentTimeMillis();
    private long lastUpdated = System.currentTimeMillis();

    public MessageInactivity(ACD acd, String guildName, @Nullable List<PlayerWithInactivity> playersInGuild, Message progressMessage) {
        super(acd, progressMessage, playersInGuild);
        this.guildName = guildName;
        this.playersInGuild = playersInGuild;
        if (this.playersInGuild != null)
            this.playersInGuild.sort((p1, p2) -> {
                long time = (p1.lastJoined - p2.lastJoined);
                if (time > 0) return 1;
                else if (time == 0) return 0;
                return -1;
            });
        this.message = progressMessage;
        if (this.playersInGuild == null) return;
        this.border = String.format("```ml\n|%5s %-30s| %-25s| %-25s|", "", guildName + " Members", "Rank", "Time Inactive");
    }

    @Override
    protected void initButtons() {
        super.initButtons();
        this.message.addReaction(DiscordEmoji.TOP.getEmoji()).queue();
        this.message.addReaction(DiscordEmoji.UP.getEmoji()).queue();
        this.message.addReaction(DiscordEmoji.DOWN.getEmoji()).queue();
    }

    @Override
    protected int getEntriesPerPage() {
        return 15;
    }

    @Override
    protected int getEntriesPerSection() {
        return 5;
    }

    @Override
    protected long getMillisToOld() {
        return 0;
    }

    @Override
    protected int getFirstDashIndex() {
        return 0;
    }

    @Override
    protected Message makeMessage() {
        if (this.playersInGuild == null) {
            return new MessageBuilder("That guild was not found").build();
        }
        return super.makeMessage();
    }

    @Override
    protected String getDivider() {
        return "+" + "-".repeat(36) + "+" + "-".repeat(26) + "+" + "-".repeat(26) + "+";
    }


    @GuiReactionEmoji(emote = DiscordEmoji.DOWN)
    public void orderDown(MessageReactionAddEvent event) {
        page = 0;
        this.playersInGuild.sort((p1, p2) -> {
            long time = (p2.lastJoined - p1.lastJoined);
            if (time > 0) return 1;
            else if (time == 0) return 0;
            return -1;
        });
        editMessage();
    }

    @GuiReactionEmoji(emote = DiscordEmoji.UP)
    public void orderUp(MessageReactionAddEvent event) {
        page = 0;
        this.playersInGuild.sort((p1, p2) -> {
            long time = (p1.lastJoined - p2.lastJoined);
            if (time > 0) return 1;
            else if (time == 0) return 0;
            return -1;
        });
        editMessage();
    }

    @GuiReactionEmoji(emote = DiscordEmoji.TOP)
    public void top(MessageReactionAddEvent event) {
        page = 0;
        editMessage();
    }


    @Override
    public long getId() {
        return message.getIdLong();
    }
}
