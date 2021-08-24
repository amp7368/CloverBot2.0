package apple.inactivity.discord.activity;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.inactivity.utils.Pretty;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.guild.WynnGuild;
import apple.inactivity.wynncraft.guild.WynnGuildDatabase;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import apple.inactivity.wynncraft.guild.WynnGuildMember;
import apple.inactivity.wynncraft.player.WynnPlayer;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MessageInactivityProgress extends ACDGui {
    protected final WynnGuildHeader guildHeader;
    protected final Member discordMember;
    private WynnGuild guild;
    protected final List<WynnPlayer> members = new ArrayList<>();

    public MessageInactivityProgress(ACD acd, MessageChannel channel, WynnGuildHeader guildHeader, Member discordMember) {
        super(acd, channel);
        this.guildHeader = guildHeader;
        this.discordMember = discordMember;
        WynncraftService.queue(WynncraftService.WynnRequestPriority.PRIMARY, guildHeader.name, wynnGuild -> {
            synchronized (this) {
                setGuild(wynnGuild);
                editMessageOnTimer();
            }
            for (WynnGuildMember guildMember : List.of(wynnGuild.members)) {
                if (guildMember != null) {
                    @Nullable WynnPlayer player = WynnGuildDatabase.getPlayer(guildMember.uuid);
                    if (player == null)
                        WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.NOW, guildMember.uuid, member -> addPlayer(guildMember, member));
                    else
                        this.addPlayer(guildMember, player);
                }
            }
        });
    }

    private void addPlayer(WynnGuildMember guildMember, WynnPlayer player) {
        synchronized (this) {
            this.members.add(player);
            player.addGuildMemberInfo(guildMember);
            int membersThere = this.members.size();
            int membersRequired = this.guild == null ? 1 : this.guild.members.length;
            if (membersThere >= membersRequired) {
                remove();
                onFinishedProgress();
            } else {
                editMessageOnTimer();
            }
        }
        WynnGuildDatabase.addMember(player);
    }

    public abstract void onFinishedProgress();

    private void setGuild(WynnGuild wynnGuild) {
        synchronized (this) {
            this.guild = wynnGuild;
        }
    }

    @Override
    protected void initButtons() {

    }

    @Override
    protected long getMillisEditTimer() {
        return MillisTimeUnits.SECOND * 3;
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }


    @Override
    protected Message makeMessage() {
        int membersThere = this.members.size();
        int membersRequired = this.guild == null ? 1 : this.guild.members.length;
        double progress = membersThere / (double) membersRequired;

        return new MessageBuilder(Pretty.getProgress(progress)).build();
    }
}
