package apple.inactivity.discord;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommandResponse;
import apple.discord.acd.command.CommandLogger;
import apple.discord.acd.command.CommandLoggerLevel;
import apple.discord.acd.command.DefaultCommandLoggerLevel;
import apple.inactivity.CloverMain;
import apple.inactivity.cache.SqlDiscordCache;
import apple.inactivity.discord.activity.CommandInactivity;
import apple.inactivity.discord.changelog.ChangelogDatabase;
import apple.inactivity.discord.changelog.MessageChangelog;
import apple.inactivity.discord.clover.ManageServerCommand;
import apple.inactivity.discord.help.CommandHelp;
import apple.inactivity.discord.misc.CommandSuggest;
import apple.inactivity.discord.stats.CommandStats;
import apple.inactivity.discord.watcher.WatchGuildCommand;
import apple.inactivity.utils.Pretty;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

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
    public static ACD ACD;

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
        CloverPermissions.addAllPermissions(ACD);
        ParameterConverterNames.addAllParameters(ACD);
        ACD.getCommandLogger().addLogger(new CloverLogger());
        new CommandInactivity(ACD);
        new CommandStats(ACD);
        new CommandSuggest(ACD);
        new CommandHelp(ACD);
        new WatchGuildCommand(ACD);
        new ManageServerCommand(ACD);
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

        try {
            SqlDiscordCache.cache(event);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static class CloverLogger implements CommandLogger {
        @Override
        public void log(@NotNull MessageReceivedEvent event, ACDCommandResponse response) {
            String userTag = event.getAuthor().getAsTag();
            String content = event.getMessage().getContentDisplay();
            String guildName = event.getGuild().getName();
            if (!ChangelogDatabase.hasHeardChangelog(event.getAuthor().getIdLong())) {
                new MessageChangelog(ACD, event.getChannel()).makeFirstMessage();
                ChangelogDatabase.addMember(event.getAuthor());
            }
            SendLogs.log(Pretty.uppercaseFirst(response.getCommandAlias()), String.format("*%s* has requested '%s' in the %s server", userTag, content, guildName));
        }

        @Override
        public boolean shouldLog(CommandLoggerLevel level) {
            return level.getLevel() != DefaultCommandLoggerLevel.IGNORE.getLevel();
        }
    }
}
