package apple.inactivity.listeners;

import apple.inactivity.discord.DiscordBot;
import net.dv8tion.jda.api.entities.TextChannel;

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
    public void trigger(int daysInactiveToTrigger, String player) {
        verifyChannel();
        channel.sendMessage(message.replaceAll("%s", player).replaceAll("##", String.valueOf(daysInactiveToTrigger))).queue();
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
        this.channelId = channel.getIdLong();
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    private static record InactivityPingData(String memberName, long memberId) {
    }
}
