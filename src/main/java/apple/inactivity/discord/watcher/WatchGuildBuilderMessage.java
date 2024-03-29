package apple.inactivity.discord.watcher;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.parameters.ParameterDefined;
import apple.discord.acd.parameters.ParameterVargs;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.buttons.GuiMenu;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.discord.acd.text.ACDChannelListenerFromUser;
import apple.discord.acd.text.DiscordChannelListener;
import apple.inactivity.discord.DiscordBot;
import apple.inactivity.discord.ParameterConverterNames;
import apple.inactivity.discord.clover.ManageServerCommand;
import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.Servers;
import apple.inactivity.manage.WatchGuildManager;
import apple.inactivity.manage.listeners.InactivityListener;
import apple.inactivity.manage.listeners.InactivityListenerPing;
import apple.inactivity.manage.listeners.WatchGuild;
import apple.inactivity.mojang.MojangService;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;
import apple.utilities.structures.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class WatchGuildBuilderMessage extends ACDGuiPageable {
    public static final String LISTENER_ADD_MENU = "listener_add_menu";
    private final TextChannel channel;
    private final User author;
    private final WynnGuildHeader guild;
    private final long serverId;
    private String extraMessage = null;
    private WatchGuild trigger;

    public WatchGuildBuilderMessage(ACD acd, TextChannel channel, User author, WynnGuildHeader guild) {
        super(acd, channel);
        this.channel = channel;
        this.author = author;
        this.guild = guild;
        this.serverId = channel.getGuild().getIdLong();
        addPage(this::confirmGuild);
        addPage(this::adjustInactivityWatch);
        addPage(this::addIgnoredMembers);
        addPage(this::saved);
        this.trigger = new WatchGuild(guild.name, guild.prefix, serverId);
    }

    public WatchGuildBuilderMessage(ACD acd, Message message, User author, WynnGuildHeader guild, WatchGuild watch) {
        super(acd, message);
        this.channel = message.getTextChannel();
        this.author = author;
        this.guild = guild;
        this.serverId = channel.getGuild().getIdLong();
        addPage(this::confirmGuild);
        addPage(this::adjustInactivityWatch);
        addPage(this::addIgnoredMembers);
        addPage(this::saved);
        this.trigger = watch;
    }

    private Message saved() {
        ServerManager manager = Servers.getOrMake(serverId);
        WatchGuildManager watchGuildManager = manager.getWatchGuildManager();
        watchGuildManager.addWatch(trigger);
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Saved the watch");
        embed.setAuthor(String.format("Use %s%s to see the watches for this Discord Server", DiscordBot.PREFIX, ManageServerCommand.CLOVER_MANAGE_SERVER_COMMAND));
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }

    private Message addIgnoredMembers() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Add members to not watch");
        if (extraMessage != null) embed.setAuthor(extraMessage);
        embed.setDescription(trigger.getIgnoreUUIDs().stream().map(p -> String.format("[**%s** (%s)]", p.getKey(), p.getValue().toString())).collect(Collectors.joining("\n")));
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows(ActionRow.of(
                new ButtonImpl("add_member", "Add members", ButtonStyle.PRIMARY, false, null)
        ));
        return messageBuilder.build();
    }

    @GuiButton(id = "add_member")
    public void addMember(ButtonClickEvent event) {
        new ACDChannelListenerFromUser.ListenerSimple(acd, channel, MillisTimeUnits.MINUTE_15, this::addMember, true, author);
        extraMessage = "Write a comma seperated list of all usernames to not include in the watch";
        editAsReply(event);
    }

    public void addMember(MessageReceivedEvent event, String membersAll) {
        Message msg = event.getMessage();
        msg.addReaction(DiscordEmoji.WORKING.getEmoji()).queue();
        extraMessage = null;
        String[] membersSplit = membersAll.split("[,\\s]");
        AtomicInteger count = new AtomicInteger(membersSplit.length);
        for (String member : membersSplit) {
            if (member.isBlank()) {
                decrementMemberReadCount(msg, count);
                continue;
            }
            member = member.trim();
            RequestPrioritySettingsBuilder<MojangService.ResponseUUID, MojangService.MojangPriority> settings = RequestPrioritySettingsBuilder.emptyPriority();
            settings.withPriority(MojangService.MojangPriority.HIGH);
            settings.withPriorityExceptionHandler((e) -> decrementMemberReadCount(msg, count));
            MojangService.getUUID(member, (uuid, name) -> {
                decrementMemberReadCount(msg, count);
                trigger.addIgnored(new Pair<>(name, UUID.fromString(Links.splitUUID(uuid))));
            }, settings);
        }
    }

    private void decrementMemberReadCount(Message msg, AtomicInteger count) {
        if (count.decrementAndGet() == 0) {
            editMessage();
            msg.delete().queue(e -> {
            }, f -> {
            });
        } else {
            editMessageOnTimer();
        }
    }

    private Message adjustInactivityWatch() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Edit the watch message");
        StringBuilder content = new StringBuilder();
        content.append(String.format("After **%d days** of a player in %s being inactive:\n", trigger.getDaysInactiveToTrigger(), guild.name));
        int i = 1;
        for (InactivityListener listener : trigger.getListeners()) {
            content.append(String.format("\t**%d.** %s\n", i++, listener.prettyString()));
        }
        if (trigger.getListeners().isEmpty()) {
            content.append("\tI will do nothing\n");
        }
        if (trigger.isShouldRepeat())
            content.append(String.format("\nThen I will repeat that every **%d days**", trigger.getDaysToRepeat()));
        else
            content.append("\nThese will not repeat if a player continues to be inactive");
        embed.setDescription(content);
        messageBuilder.setEmbeds(embed.build());

        // add menu to select a listener to add
        List<SelectOption> options = new ArrayList<>();
        for (InactivityListener.InactivityListenerType listenerType : InactivityListener.InactivityListenerType.values()) {
            options.add(SelectOption.of(listenerType.getPrettyName(), listenerType.getTypeId()));
        }
        SelectionMenuImpl addListener = new SelectionMenuImpl(LISTENER_ADD_MENU, "Add an action", 1, 1, false, options);

        // add menu to select a listener to add
        options = new ArrayList<>();
        for (int listenerIndex = 0, size = trigger.getListeners().size(); listenerIndex < size; listenerIndex++) {
            options.add(SelectOption.of(String.format("Listener #%d", listenerIndex + 1), String.valueOf(listenerIndex)));
        }
        SelectionMenuImpl editListeners = new SelectionMenuImpl("edit_listener_menu", "Edit listeners", 1, 1, false, options);
        SelectionMenuImpl removeListeners = new SelectionMenuImpl("remove_listener_menu", "Remove listeners", 1, 1, false, options);
        List<ActionRow> actionRows = new ArrayList<>(List.of(ActionRow.of(
                new ButtonImpl("decrease", "1st", ButtonStyle.PRIMARY, false, DiscordEmoji.LEFT.getDiscordEmoji()),
                new ButtonImpl("increase", "1st", ButtonStyle.PRIMARY, false, DiscordEmoji.RIGHT.getDiscordEmoji()),
                new ButtonImpl("decrease_repeat", "Repeat", ButtonStyle.PRIMARY, false, DiscordEmoji.LEFT.getDiscordEmoji()),
                new ButtonImpl("increase_repeat", "Repeat", ButtonStyle.PRIMARY, false, DiscordEmoji.RIGHT.getDiscordEmoji()),
                new ButtonImpl("toggle_repeat", "Toggle repeating", ButtonStyle.PRIMARY, false, null)
        ), ActionRow.of(
                addListener
        )));
        if (!options.isEmpty()) {
            actionRows.add(ActionRow.of(editListeners));
            actionRows.add(ActionRow.of(removeListeners));
        }

        messageBuilder.setActionRows(actionRows);
        // add button to remove last listener


        return messageBuilder.build();
    }

    @Override
    protected Collection<ActionRow> getNavigationRow() {
        if (page == 0) {
            return Collections.singleton(ActionRow.of(
                    getBackButton().asDisabled(),
                    getForwardButton(),
                    getTestButton().asDisabled(),
                    getDeleteButton()
            ));
        } else if (page == pagesList.size() - 2) {
            return Collections.singleton(ActionRow.of(
                    getBackButton(),
                    getSaveButton(),
                    getTestButton(),
                    getDeleteButton()
            ));
        } else if (page == pagesList.size() - 1) {
            return Collections.emptyList();
        }
        return Collections.singleton(ActionRow.of(
                getBackButton(),
                getForwardButton(),
                getTestButton(),
                getDeleteButton()
        ));
    }

    private ButtonImpl getDeleteButton() {
        return new ButtonImpl("delete", "Delete", ButtonStyle.DANGER, false, null);
    }

    private ButtonImpl getSaveButton() {
        return new ButtonImpl(getForwardButton().getId(), "Save", ButtonStyle.SUCCESS, false, null);
    }

    private ButtonImpl getTestButton() {
        return new ButtonImpl("testTrigger", "Test", ButtonStyle.SUCCESS, false, null);
    }

    @GuiButton(id = "toggle_repeat")
    public void toggleRepeat(ButtonClickEvent event) {
        trigger.toggleShouldRepeat();
        editAsReply(event);
    }

    @GuiButton(id = "testTrigger")
    public void testTrigger(ButtonClickEvent interaction) {
        trigger.callTestTrigger("[player here]");
        editAsReply(interaction);
    }

    @GuiButton(id = "increase")
    public void increaseTriggerDays(ButtonClickEvent interaction) {
        this.trigger.incrementDaysInactive(1);
        editAsReply(interaction);
    }

    @GuiButton(id = "decrease")
    public void decreaseTriggerDays(ButtonClickEvent interaction) {
        this.trigger.incrementDaysInactive(-1);
        editAsReply(interaction);
    }


    @GuiButton(id = "increase_repeat")
    public void decreaseRepeatDays(ButtonClickEvent interaction) {
        this.trigger.incrementDaysToRepeat(1);
        editAsReply(interaction);
    }

    @GuiButton(id = "decrease_repeat")
    public void increaseRepeatDays(ButtonClickEvent interaction) {
        this.trigger.incrementDaysToRepeat(-1);
        editAsReply(interaction);
    }

    @GuiButton(id = "delete")
    public void delete(ButtonClickEvent event) {
        ServerManager manager = Servers.getOrMake(serverId);
        WatchGuildManager watchGuildManager = manager.getWatchGuildManager();
        watchGuildManager.removeWatch(trigger.getUUID(), trigger.getGuildTag());
        event.editMessage(deletedPage()).queue();
        remove();
    }

    private Message deletedPage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("This watch has been deleted");
        embed.setAuthor("Deleted");
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }

    private Message confirmGuild() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("Confirm guild: %s [%s]", guild.getName(), guild.prefix == null ? "???" : guild.prefix));
        embed.setAuthor("Create Inactivity Watch");
        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }

    @GuiMenu(id = "edit_listener_menu")
    public void editListener(SelectionMenuEvent interaction) {
        List<SelectOption> selections = interaction.getSelectedOptions();
        if (selections != null && !selections.isEmpty()) {
            int index = Integer.parseInt(selections.get(0).getValue());
            InactivityListener currentListener = this.trigger.getListeners().get(index);
            ListenerPingSubPage subPage = new ListenerPingSubPage(acd, message, this, (InactivityListenerPing) currentListener, author);
            addSubPage(subPage);
            subPage.makeFirstMessage();
        }

        editAsReply(interaction);
    }

    @GuiMenu(id = "remove_listener_menu")
    public void removeListener(SelectionMenuEvent interaction) {
        List<SelectOption> selections = interaction.getSelectedOptions();
        if (selections != null && !selections.isEmpty()) {
            int index = Integer.parseInt(selections.get(0).getValue());
            List<InactivityListener> listeners = this.trigger.getListeners();
            if (listeners.size() > index) {
                this.trigger.removeListener(listeners.get(index));
            }
        }
        editAsReply(interaction);
    }

    @GuiMenu(id = LISTENER_ADD_MENU)
    public void onListenerAdd(SelectionMenuEvent interaction) {
        List<SelectOption> selections = interaction.getSelectedOptions();
        if (selections != null && !selections.isEmpty()) {
            String listenerId = selections.get(0).getValue();
            InactivityListener.InactivityListenerType listenerType = InactivityListener.InactivityListenerType.from(listenerId);
            InactivityListener currentListener = listenerType.getCreator();
            switch (listenerType) {
                case PING -> {
                    ((InactivityListenerPing) currentListener).setChannel(channel);
                    ListenerPingSubPage subPage = new ListenerPingSubPage(acd, message, this, (InactivityListenerPing) currentListener, author);
                    addSubPage(subPage);
                    subPage.makeFirstMessage();
                }
            }
        }
        editAsReply(interaction);
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    public static class ListenerPingSubPage extends ACDGui {
        private WatchGuildBuilderMessage parent;
        private InactivityListenerPing pingListener;
        private User author;
        private String extraMessage;

        public ListenerPingSubPage(ACD acd, Message message, WatchGuildBuilderMessage parent, InactivityListenerPing pingListener, User author) {
            super(acd, message, parent);
            this.parent = parent;
            this.pingListener = pingListener;
            this.author = author;
        }

        @Override
        protected void initButtons() {
        }

        @Override
        protected Message makeMessage() {
            MessageBuilder messageBuilder = new MessageBuilder();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Create a ping/log action");
            String content = pingListener.prettyString();
            if (extraMessage != null)
                embed.setDescription(content + "\n\n" + extraMessage);
            else
                embed.setDescription(content);
            messageBuilder.setEmbeds(embed.build());
            messageBuilder.setActionRows(ActionRow.of(
                    new ButtonImpl("set_channel", "Set channel", ButtonStyle.PRIMARY, false, null),
                    new ButtonImpl("set_message", "Set message", ButtonStyle.PRIMARY, false, null),
                    new ButtonImpl("submit", "Submit", ButtonStyle.SUCCESS, false, null)
            ));
            return messageBuilder.build();
        }

        @GuiButton(id = "set_channel")
        public void addPing(ButtonClickEvent event) {
            extraMessage = "Please mention the #channel you would like the message to be sent";
            editAsReply(event);
            new ChannelMentionListener(acd, event.getChannel(), author, (e, channel) -> {
                e.getMessage().delete().queue((s) -> {
                }, f -> {
                });
                pingListener.setChannel(channel[0]);
                extraMessage = null;
                editMessage();
            });
        }

        @GuiButton(id = "set_message")
        public void setMessage(ButtonClickEvent event) {
            extraMessage = """
                    Please type the message that will be used to ping people
                    Use %s to specify where the inactive member's name will be listed
                    Use ## to specify where days inactive will be listed
                    Use @@ to specify where the discord mention of the inactive player will be listed
                    Use normal pings to ping certain people every time""";
            editAsReply(event);
            new MessageListener(acd, event.getChannel(), (e, msg) -> {
                e.getMessage().delete().queue((s) -> {
                }, f -> {
                });
                pingListener.setMessage(msg);
                extraMessage = null;
                editMessage();
            }, author);
        }

        @GuiButton(id = "submit")
        public void submit(ButtonClickEvent event) {
            parent.trigger.addListener(this.pingListener);
            parent.removeSubPage();
            editAsReply(event);
        }

        @Override
        protected long getMillisToOld() {
            return MillisTimeUnits.HOUR;
        }
    }

    public static class ChannelMentionListener extends ACDChannelListenerFromUser {
        private final BiConsumer<MessageReceivedEvent, TextChannel[]> onPing;

        public ChannelMentionListener(ACD acd, MessageChannel channel, User user, BiConsumer<MessageReceivedEvent, TextChannel[]> onPing) {
            super(acd, channel, user);
            this.onPing = onPing;
        }

        @DiscordChannelListener
        public void onPing(MessageReceivedEvent event, @ParameterDefined(usage = "#channel", id = ParameterConverterNames.CHANNEL_PINGS) TextChannel[] pings) {
            onPing.accept(event, pings);
            remove();
        }


        @Override
        protected long getMillisToOld() {
            return MillisTimeUnits.MINUTE_15;
        }
    }

    public static class MessageListener extends ACDChannelListenerFromUser {
        private final BiConsumer<MessageReceivedEvent, String> runner;

        public MessageListener(ACD acd, MessageChannel channel, BiConsumer<MessageReceivedEvent, String> runner, User... users) {
            super(acd, channel, users);
            this.runner = runner;
        }

        @Override
        protected long getMillisToOld() {
            return MillisTimeUnits.MINUTE_15;
        }

        @DiscordChannelListener
        public void listen(MessageReceivedEvent e, @ParameterVargs(usage = "msg", nonEmpty = true) String msg) {
            runner.accept(e, msg);
            remove();
        }
    }
}
