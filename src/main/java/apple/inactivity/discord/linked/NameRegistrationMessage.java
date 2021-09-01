package apple.inactivity.discord.linked;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.buttons.GuiMenu;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.inactivity.manage.LinkedAccount;
import apple.inactivity.manage.Servers;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.player.WynnPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NameRegistrationMessage extends ACDGuiPageable {
    private long serverId;

    private WynnPlayer player;
    private Member discordPlayer;

    private List<WynnPlayer> players = null;
    private List<Member> discordPlayers = null;
    private String givenDiscordPlayer;
    private String givenMinecraftPlayer;

    public NameRegistrationMessage(ACD acd,
                                   MessageChannel channel,
                                   long serverId,
                                   @Nullable WynnPlayer player,
                                   @Nullable Member discordPlayer,
                                   String givenMinecraftPlayer,
                                   String givenDiscordPlayer) {
        super(acd, channel);
        this.serverId = serverId;
        this.player = player;
        this.discordPlayer = discordPlayer;
        this.givenMinecraftPlayer = givenMinecraftPlayer;
        this.givenDiscordPlayer = givenDiscordPlayer;
        if (player == null) {
            addPage(this::choosePlayer);
        }
        if (discordPlayer == null) {
            addPage(this::chooseDiscordUser);
        }
        addPage(this::submit);
        addPage(this::submitted);
    }

    public NameRegistrationMessage(ACD acd, MessageChannel channel, long serverId, List<WynnPlayer> players, List<Member> discordPlayers,
                                   String givenMinecraftPlayer,
                                   String givenDiscordPlayer) {
        this(acd, channel, serverId, (WynnPlayer) null, (Member) null, givenMinecraftPlayer, givenDiscordPlayer);
        this.players = players;
        this.discordPlayers = discordPlayers;
        this.discordPlayers.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getEffectiveName(), o2.getEffectiveName()));
        this.players.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.username, o2.username));
    }

    public NameRegistrationMessage(ACD acd, MessageChannel channel, long serverId, List<WynnPlayer> players, @Nullable Member discordPlayer,
                                   String givenMinecraftPlayer,
                                   String givenDiscordPlayer) {
        this(acd, channel, serverId, (WynnPlayer) null, discordPlayer, givenMinecraftPlayer, givenDiscordPlayer);
        this.players = players;
        this.players.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.username, o2.username));
    }

    public NameRegistrationMessage(ACD acd, MessageChannel channel, long serverId, @Nullable WynnPlayer player, List<Member> discordPlayers,
                                   String givenMinecraftPlayer,
                                   String givenDiscordPlayer) {
        this(acd, channel, serverId, player, (Member) null, givenMinecraftPlayer, givenDiscordPlayer);
        this.discordPlayers = discordPlayers;
        this.discordPlayers.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getEffectiveName(), o2.getEffectiveName()));
    }

    private Message chooseDiscordUser() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Set the discord account");
        embed.setAuthor(givenDiscordPlayer);
        messageBuilder.setEmbeds(embed.build());
        List<SelectOption> options = new ArrayList<>();
        int i = 0;
        for (Member player : discordPlayers) {
            options.add(SelectOption.of(player.getEffectiveName(), player.getId()));
            if (++i == MAX_OPTIONS_IN_SELECTION_MENU) break;
        }
        messageBuilder.setActionRows(ActionRow.of(
                new SelectionMenuImpl("choose_discord", "Choose a discord account", 1, 1, false, options)
        ));
        return messageBuilder.build();
    }

    private Message choosePlayer() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Set the minecraft account");
        messageBuilder.setEmbeds(embed.build());
        List<SelectOption> options = new ArrayList<>();
        int i = 0;
        for (WynnPlayer player : players) {
            options.add(SelectOption.of(player.username, player.username));
            if (++i == MAX_OPTIONS_IN_SELECTION_MENU) break;
        }
        messageBuilder.setActionRows(ActionRow.of(
                new SelectionMenuImpl("choose_minecraft", "Choose a minecraft account", 1, 1, false, options)
        ));
        return messageBuilder.build();
    }

    private Message submit() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(this.player.username);
        embed.setAuthor(this.discordPlayer.getEffectiveName(), null, this.discordPlayer.getUser().getAvatarUrl());
        embed.setImage(Links.MC_AVATAR + player.uuid);
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows(ActionRow.of(getSubmitButton()));
        return messageBuilder.build();
    }

    private Message submitted() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(this.player.username);
        embed.setAuthor(this.discordPlayer.getEffectiveName(), null, this.discordPlayer.getUser().getAvatarUrl());
        embed.addField("Submitted", "The discord and minecraft accounts have been linked", false);
        embed.setImage(Links.MC_AVATAR + player.uuid);
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows(ActionRow.of(getSubmitButton().asDisabled()));
        return messageBuilder.build();
    }

    @Override
    protected Collection<ActionRow> getNavigationRow() {
        return Collections.emptyList();
    }

    private ButtonImpl getSubmitButton() {
        return new ButtonImpl("submit", "Submit", ButtonStyle.SUCCESS, false, null);
    }

    @GuiMenu(id = "choose_minecraft")
    public void chooseMinecraft(SelectionMenuEvent event) {
        List<SelectOption> options = event.getSelectedOptions();
        if (options != null && !options.isEmpty()) {
            WynnPlayer choice = null;
            String optionValue = options.get(0).getValue();
            for (WynnPlayer player : players) {
                if (player.username.equals(optionValue)) {
                    choice = player;
                    break;
                }
            }
            if (choice != null) {
                this.player = choice;
                page++;
            }
        }
        editAsReply(event);
    }

    @GuiMenu(id = "choose_discord")
    public void chooseDiscord(SelectionMenuEvent event) {
        List<SelectOption> options = event.getSelectedOptions();
        if (options != null && !options.isEmpty()) {
            Member choice = null;
            String optionValue = options.get(0).getValue();
            for (Member player : discordPlayers) {
                if (player.getId().equals(optionValue)) {
                    choice = player;
                    break;
                }
            }
            if (choice != null) {
                this.discordPlayer = choice;
                page++;
            }
        }
        editAsReply(event);
    }

    @GuiButton(id = "submit")
    public void submitButton(ButtonClickEvent event) {
        page++;
        editAsReply(event);
        Servers.getOrMake(serverId).getLinkedAccounts().addAccount(new LinkedAccount(discordPlayer.getIdLong(), player.uuid, player.username));
    }


    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}
