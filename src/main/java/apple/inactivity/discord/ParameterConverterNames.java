package apple.inactivity.discord;

import apple.discord.acd.ACD;
import apple.discord.acd.parameters.ACDParameterConverterChannelTags;
import apple.discord.acd.parameters.ACDParameterConverterTags;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class ParameterConverterNames {
    public static final String PINGS = "pings";
    public static final String CHANNEL_PINGS = "channels";

    public static void addAllParameters(ACD acd) {
        acd.getParameterConverters().add(PINGS, Member[].class, new ACDParameterConverterTags(true, true));
        acd.getParameterConverters().add(CHANNEL_PINGS, TextChannel[].class, new ACDParameterConverterChannelTags(true, true, ChannelType.TEXT));
    }
}
