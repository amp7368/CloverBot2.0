package apple.inactivity.discord.reactions;

import apple.inactivity.Pretty;
import apple.inactivity.data.PlayerWithInactivity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nullable;
import java.util.List;

public class InactivityMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 15;
    public static final long MILLIS_IN_DAY = 1000 * 60 * 60;
    private int page = 0;
    private final String guildName;
    private final List<PlayerWithInactivity> playersInGuild;
    private final Message message;
    private final long timeNow = System.currentTimeMillis();
    private long lastUpdated = System.currentTimeMillis();

    public InactivityMessage(String guildName, @Nullable List<PlayerWithInactivity> playersInGuild, Message progressMessage) {
        this.guildName = guildName;
        this.playersInGuild = playersInGuild;
        this.playersInGuild.sort((p1, p2) -> (int) (p1.lastJoined - p2.lastJoined));
        this.message = progressMessage;
        this.message.editMessage(makeMessage()).queue();
        AllReactables.add(this);
        this.message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        this.message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        this.message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        this.message.addReaction(AllReactables.Reactable.UP.getFirstEmoji()).queue();
        this.message.addReaction(AllReactables.Reactable.DOWN.getFirstEmoji()).queue();
    }

    private String makeMessage() {
        StringBuilder text = new StringBuilder();
        text.append("```ml\n");
        text.append(String.format("|%5s %-30s| %-25s| %-25s|\n", "", guildName + " Members", "Rank", "Time Inactive"));
        int upper = Math.min(playersInGuild.size(), (page + 1) * ENTRIES_PER_PAGE);
        for (int i = page * ENTRIES_PER_PAGE; i < upper; i++) {
            if (i % 5 == 0) {
                text.append(getDash());
            }
            PlayerWithInactivity player = playersInGuild.get(i);
            long days = (timeNow - player.lastJoined) / MILLIS_IN_DAY;
            String daysString;
            if (days < 0)
                daysString = "Error";
            else
                daysString = days + " day" + (days == 1 ? "" : "s");
            text.append(String.format("|%4d. %-30s| %-25s| %-25s|\n",
                    i + 1,
                    Pretty.limit(player.username, 30),
                    Pretty.uppercaseFirst(player.rank),
                    daysString));
        }
        text.append("\n```");
        return text.toString();
    }

    private String getDash() {
        return "+" + "-".repeat(36) + "+" + "-".repeat(26) + "+" + "-".repeat(26) + "+\n";
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null) return;
        switch (reactable) {
            case LEFT:
                left();
                event.getReaction().removeReaction(user).queue();
                break;
            case RIGHT:
                right();
                event.getReaction().removeReaction(user).queue();
                break;
            case TOP:
                top();
                event.getReaction().removeReaction(user).queue();
                break;
            case UP:
                orderUp();
                event.getReaction().removeReaction(user).queue();
                break;
            case DOWN:
                orderDown();
                event.getReaction().removeReaction(user).queue();
                break;
        }
    }

    private void orderDown() {
        page = 0;
        this.playersInGuild.sort((p1, p2) -> (int) (p2.lastJoined - p1.lastJoined));
        message.editMessage(makeMessage()).queue();
    }

    private void orderUp() {
        page = 0;
        this.playersInGuild.sort((p1, p2) -> (int) (p1.lastJoined - p2.lastJoined));
        message.editMessage(makeMessage()).queue();
    }

    private void top() {
        page = 0;
        message.editMessage(makeMessage()).queue();
        lastUpdated = System.currentTimeMillis();
    }

    private void left() {
        if (page != 0) {
            page--;
            message.editMessage(makeMessage()).queue();
        }
        lastUpdated = System.currentTimeMillis();
    }

    private void right() {
        if ((page + 1) * ENTRIES_PER_PAGE < playersInGuild.size()) {
            page++;
            message.editMessage(makeMessage()).queue();
        }
        lastUpdated = System.currentTimeMillis();
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
