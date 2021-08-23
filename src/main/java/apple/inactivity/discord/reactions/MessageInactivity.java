package apple.inactivity.discord.reactions;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.gui.*;
import apple.inactivity.utils.Pretty;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import apple.inactivity.wynncraft.player.WynnPlayer;
import com.google.gson.Gson;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MessageInactivity extends ACDGuiEntryList {

    public static final Comparator<InactivityWynnPlayer> MEMBERS_COMPARATOR = (p1, p2) -> {
        long time = (p1.player.meta.lastJoin.getTime() - p2.player.meta.lastJoin.getTime());
        if (time > 0) return 1;
        else if (time == 0) {
            return p1.player.username.compareTo(p2.player.username);
        }
        return -1;
    };
    public static final Comparator<InactivityWynnPlayer> MEMBERS_COMPARATOR_REVERSED = MEMBERS_COMPARATOR.reversed();

    private static final String TOP = "top";
    private static final String DOWN = "down";
    private static final String UP = "up";
    private final Member discordMember;
    private final WynnGuildHeader guildHeader;
    private final List<WynnPlayer> members;
    private boolean isReversed = false;


    public MessageInactivity(ACD acd, Message message, Member discordMember, WynnGuildHeader guildHeader, List<WynnPlayer> members) {
        super(acd, message);
        this.discordMember = discordMember;
        this.guildHeader = guildHeader;
        this.members = members;
        addPage(new MessageInactivityListPage(this, MEMBERS_COMPARATOR, 15));
    }

    @Override
    protected Collection<ActionRow> getNavigationRow() {
        return Collections.singleton(ActionRow.of(this.getBackButton(), this.getForwardButton(), this.getTopButton(), this.getDownButton()));
    }

    @GuiButton(id = TOP)
    public void onTop(ButtonClickEvent interaction) {
        page = 0;
        editAsReply(interaction);
    }

    @GuiButton(id = UP)
    public void onReverse(ButtonClickEvent interaction) {
        page = 0;
        isReversed = !isReversed;
        sort();
        editAsReply(interaction);
    }

    private ButtonImpl getTopButton() {
        return new ButtonImpl(TOP, "To page 1", ButtonStyle.PRIMARY, false, null);
    }

    private ButtonImpl getDownButton() {
        return new ButtonImpl(UP, "Reverse sort", ButtonStyle.PRIMARY, false, null);
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    private static class InactivityWynnPlayer implements GuiEntryStringable {
        public WynnPlayer player;

        public InactivityWynnPlayer(WynnPlayer player) {
            this.player = player;
        }

        @Override
        public String asEntryString(int indexInPage, int indexInList) {
            if (player.meta == null) {
                System.out.println(new Gson().toJson(player));
                return "";
            }
            long days = (player.timeRetrieved - player.meta.lastJoin.getTime()) / MillisTimeUnits.DAY;
            String daysString;
            if (days < 0)
                daysString = "Error";
            else
                daysString = days + " day" + (days == 1 ? "" : "s");
            return String.format("|%4d. %-30s| %-25s| %-25s|",
                    indexInList + 1,
                    Pretty.limit(player.username, 30),
                    Pretty.uppercaseFirst(player.guildMember.rank),
                    daysString);
        }
    }

    public class MessageInactivityListPage extends ACDEntryPage<InactivityWynnPlayer> {

        public MessageInactivityListPage(ACDGui parent, Comparator<InactivityWynnPlayer> sorter, int entriesPerPage) {
            super(parent, sorter, entriesPerPage);
            addAllEntry(members.stream().map(InactivityWynnPlayer::new).collect(Collectors.toList()));
            sort();
        }

        @Override
        public void sort() {
            setSorter(isReversed ? MEMBERS_COMPARATOR_REVERSED : MEMBERS_COMPARATOR);
            super.sort();
        }

        @Override
        protected Message asMessage(List<GuiEntryNumbered<InactivityWynnPlayer>> entriesThisPage) {
            StringBuilder content = new StringBuilder(String.format("```ml\n|%5s %-30s| %-25s| %-25s|\n", "", guildHeader.name + " Members", "Rank", "Time Inactive"));
            for (int i = 0; i < entriesThisPage.size(); i++) {
                String toAdd;
                if ((i + getFirstDashIndex()) % getEntriesPerSection() == 0) {
                    toAdd = getDivider();
                    if (content.length() + toAdd.length() >= Message.MAX_CONTENT_LENGTH - 4) {
                        break;
                    }
                    content.append(toAdd);
                    content.append("\n");
                }
                GuiEntryNumbered<InactivityWynnPlayer> entryNumbered = entriesThisPage.get(i);
                InactivityWynnPlayer entry = entryNumbered.entry();

                toAdd = entry.asEntryString(i, entryNumbered.indexInList());
                if (content.length() + toAdd.length() >= Message.MAX_CONTENT_LENGTH - 4) {
                    break;
                }
                content.append(toAdd);
                content.append("\n");
            }
            content.append("\n```");
            return new MessageBuilder(content).build();
        }

        private int getEntriesPerSection() {
            return 5;
        }

        private int getFirstDashIndex() {
            return 0;
        }

        protected String getDivider() {
            return "+" + "-".repeat(36) + "+" + "-".repeat(26) + "+" + "-".repeat(26) + "+";
        }
    }
}
