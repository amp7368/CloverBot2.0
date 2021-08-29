package apple.inactivity.discord.clover;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.inactivity.discord.watcher.WatchGuildBuilderMessage;
import apple.inactivity.listeners.WatchGuild;
import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.WatchGuildManager;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import apple.utilities.string.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ManageServerMessage extends ACDGuiPageable {
    private final List<WatchGuild> watches;
    private final WatchGuildManager watchManager;
    private TextChannel channel;
    private User author;
    private ServerManager manager;
    private Guild discordGuild;

    public ManageServerMessage(ACD acd, TextChannel channel, User author, ServerManager manager, Guild discordGuild) {
        super(acd, channel);
        this.channel = channel;
        this.author = author;
        this.manager = manager;
        this.watchManager = manager.getWatchGuildManager();
        this.watches = watchManager.getWatches();
        this.watches.sort(WatchGuild.COMPARATOR);
        this.discordGuild = discordGuild;
        addPage(this::contents);
        int i = 0;
        for (WatchGuild watch : watchManager.getWatches()) {
            int finalI = i++;
            addPage(() -> this.watchMessage(watch, finalI));
        }
    }

    private Message watchMessage(WatchGuild watch, int index) {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(String.format("Guild watch (%d)", index + 1));
        setEmbedTitle(embed);
        embed.addField(String.format("%s [%s]", watch.getGuildName(), watch.getGuildTag()),
                String.format(
                        Pretty.pluralFormat("Triggers on %c% day%s%, Repeats on %s", watch.getDaysInactiveToTrigger()),
                        watch.isShouldRepeat() ?
                                Pretty.pluralFormat("%c% day%s%", watch.getDaysToRepeat()) :
                                "Never"
                ), false);
        messageBuilder.setEmbeds(embed.build());
        addManualButton((e) -> {
            List<WynnGuildHeader> fromGuildName = WynnDatabase.getFromGuildName(watch.getGuildTag());
            if (fromGuildName.isEmpty()) {
                // the guild doesn't exist
                e.getChannel().sendMessage("The guild specified isn't in the database. Let appleptr16#5054 know").queue();
                return;
            }
            WynnGuildHeader guildHeader = fromGuildName.get(0);
            remove();
            WatchGuildBuilderMessage newMessage = new WatchGuildBuilderMessage(acd, message, author, guildHeader, watch);
            newMessage.makeFirstMessage();
            newMessage.editAsReply(e);
        }, "edit_watch");
        messageBuilder.setActionRows(ActionRow.of(
                new ButtonImpl("edit_watch", "Edit watch", ButtonStyle.SUCCESS, false, null)
        ));
        return messageBuilder.build();
    }

    private Message contents() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Table of Contents");
        setEmbedTitle(embed);
        embed.setDescription(
                String.format(
                        """
                                **Table of Contents**
                                Guild watches (%d)
                                Player name registration
                                """,
                        watches.size()
                )
        );
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows();
        return messageBuilder.build();
    }

    private void setEmbedTitle(EmbedBuilder embed) {
        embed.setTitle(String.format("Clover Admin Panel Page (%d)", page + 1));
    }

    @Override
    protected Collection<ActionRow> getNavigationRow() {
        return Collections.singleton(
                ActionRow.of(
                        getBackButton(),
                        getForwardButton(),
                        getTopButton()
                )
        );

    }

    private ButtonImpl getTopButton() {
        return new ButtonImpl("top", "ToC", ButtonStyle.SECONDARY, false, null);
    }

    @GuiButton(id = "top")
    public void top(ButtonClickEvent event) {
        page = 0;
        editAsReply(event);
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}
