package apple.inactivity.discord.linked;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.*;
import apple.inactivity.manage.LinkedAccountsManager;
import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.listeners.WatchGuild;
import apple.inactivity.mojang.MojangService;
import apple.utilities.request.ExceptionHandler;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MissingLinkedAccountsMessage extends ACDGuiEntryList {
    public static final Comparator<LinkedPlayer> ACCOUNTS_COMPARATOR = Comparator.nullsLast(
            (o1, o2) -> o1.playerName == null || o2.playerName == null ? 0 :
                    String.CASE_INSENSITIVE_ORDER.compare(o1.playerName, o2.playerName)
    );

    public MissingLinkedAccountsMessage(ACD acd, TextChannel channel, @NotNull ServerManager serverManager) {
        super(acd, channel);
        List<WatchGuild> watches = serverManager.getWatchGuildManager().getWatches();
        for (WatchGuild watch : watches) {
            LinkedAccountsManager linkedAccounts = serverManager.getLinkedAccounts();

            List<LinkedPlayer> players = watch.getPlayers().keySet().stream()
                    .map(LinkedPlayer::new)
                    .filter(acc -> !linkedAccounts.hasAccount(acc.uuid))
                    .sorted(ACCOUNTS_COMPARATOR).collect(Collectors.toList());
            addPage(new MissingLinkedAccountsPage(this, watch.getGuildTag(), watch.getGuildName(), players));
        }
    }

    @Override
    public void makeFirstMessage() {
        super.makeFirstMessage();
        for (DynamicPage<?> page : pagesList) {
            if (page.getPage() instanceof MissingLinkedAccountsPage p) {
                p.initGetUsernames();
            }
        }
    }

    @Override
    protected Message emptyPage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("No unlinked accounts");
        embed.setDescription("There are no guilds to have linked accounts");
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    private static class LinkedPlayer implements GuiEntryStringable {
        private final UUID uuid;
        private String playerName = null;

        public LinkedPlayer(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public String asEntryString(int i, int i1) {
            return String.format("%s", playerName == null ? uuid : playerName);
        }

        public void getMinecraft(Runnable updateMsg, ExceptionHandler exceptionHandler) {
            if (playerName != null) return;
            RequestPrioritySettingsBuilder<MojangService.ResponseMinecraftUsername[], MojangService.MojangPriority> settings = RequestPrioritySettingsBuilder.emptyPriority();
            settings.withPriority(MojangService.MojangPriority.HIGH);
            settings.withPriorityExceptionHandler(exceptionHandler.andThen((e) -> this.playerName = uuid + " notfound"));
            MojangService.getPlayerName(this.uuid, (username) -> {
                this.playerName = username[username.length-1];
                updateMsg.run();
            }, settings);
        }
    }

    private class MissingLinkedAccountsPage extends ACDEntryPage<LinkedPlayer> {
        private final String guildTag;
        private final String guildName;
        private final List<LinkedPlayer> players;

        public MissingLinkedAccountsPage(ACDGui parent, String guildTag, String guildName, List<LinkedPlayer> players) {
            super(parent, ACCOUNTS_COMPARATOR, 15);
            this.guildTag = guildTag;
            this.guildName = guildName;
            this.players = players;
            addAllEntry(this.players);
        }

        public void initGetUsernames() {
            AtomicInteger count = new AtomicInteger(this.players.size());

            for (LinkedPlayer player : this.players) {
                player.getMinecraft(() -> {
                    sort();
                    if (count.decrementAndGet() == 0) editMessage();
                    else editMessageOnTimer();
                }, (e) -> {
                    sort();
                    if (count.decrementAndGet() == 0) editMessage();
                    else editMessageOnTimer();
                });
            }
        }


        @Override
        protected Message asMessage(List<GuiEntryNumbered<LinkedPlayer>> entriesThisPage) {
            MessageBuilder messageBuilder = new MessageBuilder();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(String.format("%s [%s]",guildName,guildTag));
            embed.setTitle(String.format("Unlinked Minecraft accounts (%s)", getPageNumber() + 1));
            int i = 0;
            for (GuiEntryNumbered<LinkedPlayer> player : entriesThisPage) {
                embed.addField(player.asString(i++), "", true);
            }
            if (entriesThisPage.isEmpty()) {
                embed.setDescription("There are no unlinked accounts");
            }
            messageBuilder.setEmbeds(embed.build());
            addManualButton(this::forward, "forward2");
            addManualButton(this::back, "back2");
            messageBuilder.setActionRows(
                    ActionRow.of(
                            Button.primary("back2", "Accounts Back"),
                            Button.secondary("forward2", "Accounts Forward")
                    )
            );
            return messageBuilder.build();
        }
    }
}
