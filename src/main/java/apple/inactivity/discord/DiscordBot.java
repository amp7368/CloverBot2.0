package apple.inactivity.discord;

import apple.discord.acd.ACD;
import apple.inactivity.CloverMain;
import apple.inactivity.cache.SqlDiscordCache;
import apple.inactivity.discord.activity.CommandInactivity;
import apple.inactivity.discord.changelog.ChangelogDatabase;
import apple.inactivity.discord.changelog.MessageChangelog;
import apple.inactivity.discord.commands.CommandStats;
import apple.inactivity.discord.commands.CommandSuggest;
import apple.inactivity.discord.help.CommandHelp;
import apple.inactivity.utils.Pretty;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public static final String PREFIX = "t!";
    public static final long APPLEPTR16 = 253646208084475904L;
    public static final long LOGGING_CHANNEL = 769737908293992509L;
    private static ACD ACD;

    public static String discordToken; // my bot
    public static JDA client;

    static {
        List<String> list = Arrays.asList(CloverMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String BOT_TOKEN_FILE_PATH = String.join("/", list.subList(0, list.size() - 1)) + "/config/discordToken.data";

        File file = new File(BOT_TOKEN_FILE_PATH);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {
            }
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            discordToken = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
        }

    }

    public DiscordBot() throws LoginException {
        Collection<GatewayIntent> intents = new ArrayList<>(Arrays.asList(GatewayIntent.values()));
        intents.remove(GatewayIntent.GUILD_MEMBERS);
        intents.remove(GatewayIntent.GUILD_PRESENCES);
        JDABuilder builder = JDABuilder.create(intents);
        builder.addEventListeners(this);
        builder.setToken(discordToken);
        client = builder.build();
        client.getPresence().setPresence(Activity.playing(PREFIX + "help"), false);
        ACD = new ACD(PREFIX, client);
        ACD.getCommandLogger().addLogger((event, response) -> {
            String userTag = event.getAuthor().getAsTag();
            String content = event.getMessage().getContentDisplay();
            String guildName = event.getGuild().getName();
            if (!ChangelogDatabase.hasHeardChangelog(event.getAuthor().getIdLong())) {
                new MessageChangelog(ACD, event.getChannel()).makeFirstMessage();
                ChangelogDatabase.addMember(event.getAuthor());
            }
            SendLogs.log(Pretty.uppercaseFirst(response.getCommandAlias()), String.format("*%s* has requested '%s' in the %s server", userTag, content, guildName));
        });
        new CommandInactivity(ACD);
        new CommandStats(ACD);
        new CommandSuggest(ACD);
        new CommandHelp(ACD);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
    }

    @Override
    public void onUserTyping(@NotNull UserTypingEvent event) {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getChannelType() != ChannelType.TEXT) {
            return;
        }
        // the author is not a bot
        legacy(event);

        try {
            SqlDiscordCache.cache(event);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void legacy(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentStripped().toLowerCase();
        if (messageContent.startsWith("$inactivity")) {
            event.getChannel().sendMessage("This is no longer a supported command. Try doing c!inactivity or c!activity").queue();
        }
    }
}
