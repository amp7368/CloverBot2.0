package apple.inactivity.cache;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static apple.inactivity.cache.SqlNames.*;

public class SqlDiscordCache {
    public static void cache(MessageReceivedEvent event) throws SQLException {
        // get the uid
        long messageId = event.getMessageIdLong();
        long channelId = event.getChannel().getIdLong();

        // get data about the guild
        long guildId = event.getGuild().getIdLong();


        long authorId = event.getAuthor().getIdLong();
        synchronized (VerifyDiscordCache.syncDB) {
            PreparedStatement statement = VerifyDiscordCache.database.prepareStatement(String.format(
                    "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (%d,%d,%d,%d,?,%d)",
                    TABLE_MESSAGE,
                    MESSAGE_ID,
                    CHANNEL_ID,
                    GUILD_ID,
                    AUTHOR_ID,
                    CONTENT,
                    TIME_STAMP,
                    messageId,
                    channelId,
                    guildId,
                    authorId,
                    event.getMessage().getTimeCreated().toInstant().toEpochMilli()
            ));
            statement.setString(1, event.getMessage().getContentRaw());
            statement.execute();
            statement.close();

            statement = VerifyDiscordCache.database.prepareStatement(String.format("INSERT INTO %s (%s, %s)\n" +
                            "VALUES (%d,?)\n" +
                            "ON CONFLICT DO NOTHING",
                    TABLE_CHANNEL, CHANNEL_ID, CHANNEL_NAME, channelId
            ));
            statement.setString(1,  event.getChannel().getName());
            statement.execute();
            statement.close();

            statement = VerifyDiscordCache.database.prepareStatement(String.format("INSERT INTO %s (%s, %s)\n" +
                            "VALUES (%d,?)\n" +
                            "ON CONFLICT DO NOTHING",
                    TABLE_GUILD, GUILD_ID, GUILD_NAME,guildId
            ));
            statement.setString(1, event.getGuild().getName());
            statement.execute();
            statement.close();

             statement = VerifyDiscordCache.database.prepareStatement(String.format("INSERT INTO %s (%s, %s)\n" +
                            "VALUES (%d,?)\n" +
                            "ON CONFLICT DO NOTHING",
                    TABLE_AUTHOR, AUTHOR_ID, AUTHOR_NAME, authorId
            ));
            statement.setString(1, event.getAuthor().getName());
            statement.execute();
            statement.close();
        }
    }
}
