package apple.inactivity.discord.clover;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.discord.acd.reaction.gui.GuiPageMessageable;
import apple.inactivity.discord.watcher.WatchGuildBuilderMessage;
import apple.inactivity.manage.LinkedAccount;
import apple.inactivity.manage.LinkedAccountsManager;
import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.WatchGuildManager;
import apple.inactivity.manage.listeners.WatchGuild;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import apple.utilities.util.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageServerMessage extends ACDGuiPageable {
    private static final int ACCOUNTS_PER_PAGE = 10;
    private final List<WatchGuild> watches;
    private final WatchGuildManager watchManager;
    private final LinkedAccountsManager linkedAccounts;
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
        this.linkedAccounts = manager.getLinkedAccounts();
        this.watches = watchManager.getWatches();
        this.watches.sort(WatchGuild.COMPARATOR);
        this.discordGuild = discordGuild;
        addPage(this::contents);
        int i = 0;
        for (WatchGuild watch : watchManager.getWatches()) {
            int finalI = i++;
            addPage(() -> this.watchMessage(watch, finalI));
        }
        if (watchManager.getWatches().isEmpty()) {
            addPage(this::emptyWatchMessage);
        }
        List<LinkedAccount> accounts = linkedAccounts.listAccounts();
        List<LinkedAccount> accountsThisPage = new ArrayList<>();
        int pageIndex = 0;
        for (LinkedAccount account : accounts) {
            accountsThisPage.add(account);
            if (accountsThisPage.size() == ACCOUNTS_PER_PAGE) {
                addPage(new LinkedAccountsMessage(accountsThisPage, pageIndex, channel.getGuild()));
            }
        }
        if (!accountsThisPage.isEmpty())
            addPage(new LinkedAccountsMessage(accountsThisPage, pageIndex, channel.getGuild()));
        if (accounts.isEmpty()) {
            addPage(this::emptyLinkedAccountsMessage);
        }

    }

    private Message emptyWatchMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(String.format("Guild watch (%d)", 1));
        setEmbedTitle(embed);
        embed.addField("c!watch [guild_name]", "Add a watch on a guild for this discord server", false);
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
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


    private Message emptyLinkedAccountsMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(String.format("Minecraft/Discord Accounts (%d)", 1));
        setEmbedTitle(embed);
        embed.addField("c!link account [minecraft] [discord]", "Link a minecraft account to a discord account for this discord server", false);
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    private void editMessageOnTimerIfShowing(LinkedAccountsMessage linkedAccountsMessage) {
        if (getPage() == linkedAccountsMessage) { // == because they're references to the same object
            editMessageOnTimer();
        }
    }

    private class LinkedAccountsMessage implements GuiPageMessageable {
        private final Map<LinkedAccount, Member> accountsThisPage;
        private final boolean isShowing = false;
        private final int indexInPage;

        public LinkedAccountsMessage(List<LinkedAccount> accountsThisPage, int indexInPage, Guild guild) {
            this.indexInPage = indexInPage;
            this.accountsThisPage = new HashMap<>();
            synchronized (this) {
                AtomicInteger count = new AtomicInteger(accountsThisPage.size());
                for (LinkedAccount account : accountsThisPage) {
                    this.accountsThisPage.put(account, null);
                    guild.retrieveMemberById(account.getDiscord()).queue(user -> {
                        synchronized (this) {
                            this.accountsThisPage.put(account, user);
                            if (isShowing) {
                                if (count.decrementAndGet() == 0) {
                                    editMessage();
                                } else {
                                    editMessageOnTimerIfShowing(this);
                                }
                            }
                        }
                    });
                }
            }
        }

        @Override
        public Message asMessage() {
            MessageBuilder messageBuilder = new MessageBuilder();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(String.format("Minecraft/Discord Accounts (%d)", indexInPage + 1));
            setEmbedTitle(embed);
            synchronized (this) {
                for (Map.Entry<LinkedAccount, Member> linkedAccount : accountsThisPage.entrySet()) {
                    Member member = linkedAccount.getValue();
                    String discordName = member == null ? "Not available" : member.getEffectiveName();
                    embed.addField("Minecraft: " + linkedAccount.getKey().getMinecraftUsername(), "Discord: " + discordName, true);
                }
            }
            messageBuilder.setEmbeds(embed.build());
            return messageBuilder.build();
        }
    }
}
