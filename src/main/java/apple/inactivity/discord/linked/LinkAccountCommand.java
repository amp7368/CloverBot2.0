package apple.inactivity.discord.linked;

import apple.discord.acd.ACD;
import apple.discord.acd.command.*;
import apple.discord.acd.parameters.ParameterDefined;
import apple.discord.acd.parameters.ParameterSingle;
import apple.discord.acd.parameters.ParameterVargs;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.inactivity.discord.CloverPermissions;
import apple.inactivity.discord.ParameterConverterNames;
import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.Servers;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.utilities.util.FuzzyStringMatcher;
import apple.utilities.util.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LinkAccountCommand extends ACDCommand {
    public LinkAccountCommand(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(usageFormat = "Usage: %s", alias = {"link account", "link accounts"}, overlappingCommands = "link account", order = 3, permission = CloverPermissions.ADMIN, channelType = ChannelType.TEXT)
    public ACDCommandResponse register(@NotNull MessageReceivedEvent event,
                                       @ParameterSingle(usage = "[player_name]") String playerName,
                                       @ParameterDefined(splitter = " ", usage = "(ping)", id = ParameterConverterNames.PINGS_OPTIONAL) @NotNull Member[] pings,
                                       @ParameterVargs(usage = "(discord_name)") String discordName) {
        event.getMessage().addReaction(DiscordEmoji.WORKING.getEmoji()).queue();
        if (pings.length == 0) {
            if (discordName.isBlank()) {
                ACDCommandResponse acdCommandResponse = new ACDCommandResponse();
                acdCommandResponse.setCallingState(ACDCommandCalled.CallingState.COULD_SEND_USAGE);
                return acdCommandResponse;
            }
        } else {
            if (discordName.isBlank()) {
                doLinkAccount(event, playerName, discordName, Collections.emptyList(), pings[0]);
                return null;
            } else {
                ACDCommandResponse acdCommandResponse = new ACDCommandResponse();
                acdCommandResponse.setCallingState(ACDCommandCalled.CallingState.COULD_SEND_USAGE);
                return acdCommandResponse;
            }
        }
        event.getGuild().findMembers(member -> new FuzzyStringMatcher(member.getEffectiveName(),
                FuzzyStringMatcher.Flag.CONTAINS,
                FuzzyStringMatcher.Flag.CASE_INSENSITIVE).operationsToMatch(discordName, 1) >= 0).onSuccess(discordPlayers -> {
            Member discordPlayer = null;
            List<Member> matches = new ArrayList<>();
            for (Member member : discordPlayers) {
                if (member.getEffectiveName().equalsIgnoreCase(discordName)) {
                    discordPlayer = member;
                    break;
                } else if (member.getEffectiveName().contains(discordName)) {
                    matches.add(member);
                }
            }
            if (discordPlayer == null) {
                if (matches.isEmpty()) matches = discordPlayers;
                if (matches.isEmpty()) {
                    event.getChannel().sendMessage(String.format("I couldn't find any discord user named '%s'", discordName)).queue();
                    return;
                }
            }
            doLinkAccount(event, playerName, discordName, matches, discordPlayer);
        });
        return null;
    }

    public void doLinkAccount(MessageReceivedEvent event, String playerName, String discordName, List<Member> matches, Member discordPlayer) {
        @Nullable WynnPlayer playerFromDB = WynnDatabase.getPlayer(playerName);
        if (playerFromDB == null) {
            WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.IMMEDIATE, playerName, (player) -> {
                event.getMessage().removeReaction(DiscordEmoji.WORKING.getEmoji(), acd.getSelfUser()).queue();
                if (player == null) {
                    List<WynnPlayer> players = WynnDatabase.get().getPlayerMatches(playerName);
                    if (players.isEmpty()) {
                        event.getChannel().sendMessage(String.format("I couldn't find any minecraft user named '%s'", playerName)).queue();
                        return;
                    }
                    if (players.size() != 1) {
                        event.getChannel().sendMessage("Did you mean any of the following people?" +
                                Pretty.truncate(players.stream().map(p -> p.username).collect(Collectors.joining(", ")), 1500)).queue();
                        if (discordPlayer == null) {
                            new NameRegistrationMessage(acd, event.getChannel(), event.getGuild().getIdLong(), players, matches, playerName, discordName).makeFirstMessage();
                        } else {
                            new NameRegistrationMessage(acd, event.getChannel(), event.getGuild().getIdLong(), players, discordPlayer, playerName, discordName).makeFirstMessage();
                        }
                        return;
                    }
                    player = players.get(0);
                }
                if (discordPlayer == null) {
                    new NameRegistrationMessage(acd, event.getChannel(), event.getGuild().getIdLong(), player, matches, playerName, discordName).makeFirstMessage();
                } else {
                    new NameRegistrationMessage(acd, event.getChannel(), event.getGuild().getIdLong(), player, discordPlayer, playerName, discordName).makeFirstMessage();
                }
            });
        }
    }

    @DiscordCommandAlias(alias = {"link account", "link accounts"}, overlappingCommands = "link account", permission = CloverPermissions.NOT_ADMIN, order = 100, channelType = ChannelType.TEXT)
    public ACDCommandResponse registerFail(MessageReceivedEvent event) {
        event.getChannel().sendMessage("You need to have the permission 'Manage Server' to use this command here").queue();
        ACDCommandResponse acdCommandResponse = new ACDCommandResponse();
        acdCommandResponse.setLevel(DefaultCommandLoggerLevel.IGNORE);
        return acdCommandResponse;
    }

    @DiscordCommandAlias(usageFormat = "Usage: %s", alias = {"link account missing"}, overlappingCommands = "link account", order = 1, permission = CloverPermissions.ADMIN, channelType = ChannelType.TEXT)
    public void missingRegistered(MessageReceivedEvent event) {
        new MissingLinkedAccountsMessage(acd, (TextChannel) event.getChannel(), Servers.getOrMake(event.getGuild().getIdLong())).makeFirstMessage();
    }

    @DiscordCommandAlias(usageFormat = "Usage: %s", alias = {"link account missing"}, overlappingCommands = "link account", order = 2, permission = CloverPermissions.NOT_ADMIN, channelType = ChannelType.TEXT)
    public ACDCommandResponse missingRegisteredFail(MessageReceivedEvent event) {
        event.getChannel().sendMessage("You need to have the permission 'Manage Server' to use this command here").queue();
        ACDCommandResponse acdCommandResponse = new ACDCommandResponse();
        acdCommandResponse.setLevel(DefaultCommandLoggerLevel.IGNORE);
        return acdCommandResponse;
    }

    @DiscordCommandAlias(usageFormat = "Usage: %s", alias = {"clear all link account confirm"}, permission = CloverPermissions.ADMIN, channelType = ChannelType.TEXT)
    public void clearData(MessageReceivedEvent event) {
        ServerManager serverManager = Servers.getOrMake(event.getGuild().getIdLong());
        serverManager.getLinkedAccounts().clearAllConfirm();
        serverManager.save();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Success");
        embed.setDescription("All linked account data has been cleared from this server");
        event.getChannel().sendMessage(new MessageBuilder(embed.build()).build()).queue();
    }
}
