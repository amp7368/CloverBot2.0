package apple.inactivity.discord.reactions;

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

public class MessageInactivityProgress extends ACDGui {
    private final WynnGuildHeader guildHeader;
    private Member member;
    private WynnGuild guild;
    private List<WynnPlayer> members = new ArrayList<>();

    public MessageInactivityProgress(ACD acd, MessageChannel channel, WynnGuildHeader guildHeader, Member member) {
        super(acd, channel);
        this.guildHeader = guildHeader;
        this.member = member;
        WynncraftService.queue(WynncraftService.WynnRequestPriority.PRIMARY, guildHeader.name, wynnGuild -> {
            synchronized (this) {
                setGuild(wynnGuild);
                editMessageOnTimer();
            }
            for (WynnGuildMember guildMember : List.of(wynnGuild.members)) {
                if (guildMember != null) {
                    @Nullable WynnPlayer player = WynnGuildDatabase.getPlayer(guildMember.uuid);
                    if (player == null)
                        WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.NOW, guildMember, this::addPlayer);
                    else
                        this.addPlayer(player);
                }
            }
        });
    }

    private void addPlayer(WynnPlayer member) {
        synchronized (this) {
            this.members.add(member);
            editMessageOnTimer();
        }
        WynnGuildDatabase.addMember(member);
    }

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
        double progress = this.members.size() / (double) (this.guild == null ? 1 : this.guild.members.length);
        return new MessageBuilder(Pretty.getProgress(progress)).build();
    }
}
