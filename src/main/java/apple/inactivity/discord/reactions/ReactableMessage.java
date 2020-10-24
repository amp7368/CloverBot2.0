package apple.inactivity.discord.reactions;

import apple.inactivity.discord.reactions.AllReactables;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface ReactableMessage {
    void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event);
    Long getId();
    long getLastUpdated();
}
