package apple.inactivity.cache;

import apple.inactivity.discord.DiscordBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static apple.inactivity.cache.SqlNames.*;

public class VerifyDiscordCache {
    private static final String DATABASE_FILENAME;
    public static final Object syncDB = new Object();
    private static final String BUILD_TABLE_MESSAGES = String.format("CREATE TABLE IF NOT EXISTS %s\n" +
                    "(\n" +
                    "    %s BIGINT NOT NULL,\n" +
                    "    %s BIGINT NOT NULL,\n" +
                    "    %s BIGINT NOT NULL,\n" +
                    "    %s BIGINT NOT NULL,\n" +
                    "    %s TEXT,\n" +
                    "    %s TIMESTAMP,\n" +
                    "    PRIMARY KEY (%s, %s, %s)\n" +
                    ")",
            TABLE_MESSAGE,
            MESSAGE_ID,
            CHANNEL_ID,
            GUILD_ID,
            AUTHOR_ID,
            CONTENT,
            TIME_STAMP,
            MESSAGE_ID,
            CHANNEL_ID,
            GUILD_ID
    );
    private static final String BUILD_TABLE_AUTHORS = String.format("CREATE TABLE IF NOT EXISTS %s\n" +
                    "(\n" +
                    "    %s   BIGINT,\n" +
                    "    %s TEXT,\n" +
                    "PRIMARY KEY (%s,%s)" +
                    ")",
            TABLE_AUTHOR,
            AUTHOR_ID,
            AUTHOR_NAME,
            AUTHOR_ID,
            AUTHOR_NAME
    );
    private static final String BUILD_TABLE_CHANNELS = String.format("CREATE TABLE IF NOT EXISTS %s\n" +
                    "(\n" +
                    "    %s   BIGINT,\n" +
                    "    %s TEXT,\n" +
                    "PRIMARY KEY (%s,%s)" +
                    ")",
            TABLE_CHANNEL,
            CHANNEL_ID,
            CHANNEL_NAME,
            CHANNEL_ID,
            CHANNEL_NAME
    );
    private static final String BUILD_TABLE_GUILDS = String.format("CREATE TABLE IF NOT EXISTS %s\n" +
                    "(\n" +
                    "    %s   BIGINT,\n" +
                    "    %s TEXT,\n" +
                    "PRIMARY KEY (%s,%s)" +
                    ")",
            TABLE_GUILD,
            GUILD_ID,
            GUILD_NAME,
            GUILD_ID,
            GUILD_NAME
    );
    public static Connection database;

    static {
        List<String> list = Arrays.asList(DiscordBot.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        DATABASE_FILENAME = String.join("/", list.subList(0, list.size() - 1)) + "/spooky.db";
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        synchronized (syncDB) {
            Class.forName("org.sqlite.JDBC");
            database = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILENAME);
            verify();
        }
    }

    private synchronized static void verify() throws SQLException {
        Statement statement = database.createStatement();
        statement.execute(BUILD_TABLE_MESSAGES);
        statement.execute(BUILD_TABLE_CHANNELS);
        statement.execute(BUILD_TABLE_GUILDS);
        statement.execute(BUILD_TABLE_AUTHORS);
        statement.close();
    }

}
