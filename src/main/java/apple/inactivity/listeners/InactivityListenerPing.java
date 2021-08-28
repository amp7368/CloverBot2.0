package apple.inactivity.listeners;

import net.dv8tion.jda.api.entities.TextChannel;

public class InactivityListenerPing extends InactivityListener {
    public String message = "%s has been inactive for ## days";
    private TextChannel channel;

    public InactivityListenerPing() {
        super(InactivityListenerType.PING.getTypeId());
    }

    @Override
    public String prettyString() {
        // add a custom message later
        return "I will log the inactivity message with the following message in " + channel.getAsMention() + ":\n" + message;
    }

    @Override
    public void trigger(int daysInactiveToTrigger, String player) {
        channel.sendMessage(message.replaceAll("%s", player).replaceAll("##", String.valueOf(daysInactiveToTrigger))).queue();
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    private static record InactivityPingData(String memberName, long memberId) {
    }
}
