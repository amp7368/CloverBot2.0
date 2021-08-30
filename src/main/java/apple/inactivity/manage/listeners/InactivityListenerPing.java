package apple.inactivity.manage.listeners;

import apple.inactivity.discord.DiscordBot;
import apple.inactivity.manage.LinkedAccount;
import apple.inactivity.manage.ServerManager;
import apple.utilities.util.ObjectUtilsFormatting;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class InactivityListenerPing extends InactivityListener {
    public String message = "%s has been inactive for ## days";
    private long channelId;
    private transient TextChannel channel = null;

    public InactivityListenerPing() {
        super(InactivityListenerType.PING.getTypeId());
    }

    private void verifyChannel() {
        if (channel == null) {
            channel = DiscordBot.ACD.getJDA().getTextChannelById(channelId);
        }
    }

    @Override
    public String prettyString() {
        verifyChannel();
        // add a custom message later
        return "I will log the inactivity message with the following message in " + channel.getAsMention() + ":\n" + message;
    }


    @Override
    public void trigger(ServerManager serverManager, int daysInactiveToTrigger, String player, @Nullable UUID uuid) {
        verifyChannel();
        @Nullable LinkedAccount linkedAccount = ObjectUtilsFormatting.defaultIfNull(null, uuid, (u) -> serverManager.getLinkedAccounts().getAccount(u));
        String mention = "@" + player;
        if (linkedAccount == null) {
            doTriggerMessage(daysInactiveToTrigger, player, mention);
        } else {
            linkedAccount.verifyMinecraftName(() ->
                    channel.getGuild().retrieveMemberById(linkedAccount.getDiscord()).queue(member ->
                            doTriggerMessage(daysInactiveToTrigger, player, ObjectUtilsFormatting.defaultIfNull(mention, member, IMentionable::getAsMention))));
        }
    }

    public void doTriggerMessage(int daysInactiveToTrigger, String player, String mention) {
        channel.sendMessage(message
                .replace("%s", player)
                .replace("##", String.valueOf(daysInactiveToTrigger))
                .replace("@@", mention)
        ).queue();
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
        this.channelId = channel.getIdLong();
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}
