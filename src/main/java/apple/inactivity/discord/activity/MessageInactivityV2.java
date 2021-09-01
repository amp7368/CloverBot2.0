package apple.inactivity.discord.activity;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.gui.ACDEntryPage;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.discord.acd.reaction.gui.ACDGuiEntryList;
import apple.discord.acd.reaction.gui.GuiEntryNumbered;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import apple.inactivity.wynncraft.player.WynnPlayer;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MessageInactivityV2 extends ACDGuiEntryList {
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
    private static final String UP = "up";
    public static final int ENTRIES_PER_PAGE = 15;
    private final WynnGuildHeader guildHeader;
    private final List<WynnPlayer> members;
    private boolean isReversed = false;


    public MessageInactivityV2(ACD acd, Message message, WynnGuildHeader guildHeader, List<WynnPlayer> members) {
        super(acd, message);
        this.guildHeader = guildHeader;
        this.members = members;
        addPage(new MessageInactivityListPage(this, MEMBERS_COMPARATOR, ENTRIES_PER_PAGE));
    }


    @Override
    protected Collection<ActionRow> getNavigationRow() {
        return Collections.singleton(ActionRow.of(this.getBackButton(), this.getForwardButton(), this.getTopButton(), this.getDownButton()));
    }

    @NotNull
    public static List<String> inactivityV1(List<InactivityWynnPlayer> wynnPlayers, WynnGuildHeader guildHeader) {
        wynnPlayers.sort(MEMBERS_COMPARATOR);
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < wynnPlayers.size(); ) {
            List<GuiEntryNumbered<InactivityWynnPlayer>> entriesThisPage = new ArrayList<>();
            for (int j = 0, wynnPlayersSize = wynnPlayers.size(); i < wynnPlayersSize && j < ENTRIES_PER_PAGE; j++) {
                InactivityWynnPlayer player = wynnPlayers.get(j);
                entriesThisPage.add(new GuiEntryNumbered<>(i++, player));
            }
            if (!entriesThisPage.isEmpty())
                messages.add(asStringMessage(entriesThisPage, guildHeader).toString());
        }
        return messages;
    }

    @NotNull
    private static StringBuilder asStringMessage(List<GuiEntryNumbered<InactivityWynnPlayer>> entriesThisPage, WynnGuildHeader guildHeader) {
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
        return content;
    }

    private static int getEntriesPerSection() {
        return 5;
    }

    private static int getFirstDashIndex() {
        return 0;
    }

    protected static String getDivider() {
        return "+" + "-".repeat(36) + "+" + "-".repeat(26) + "+" + "-".repeat(26) + "+";
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

    @GuiButton(id = TOP)
    public void onTop(ButtonClickEvent interaction) {
        resetPage();
        editAsReply(interaction);
    }

    private void resetPage() {
        if (getPage() instanceof MessageInactivityListPage p) {
            p.resetIndexInList();
        }
    }

    @GuiButton(id = UP)
    public void onReverse(ButtonClickEvent interaction) {
        resetPage();
        isReversed = !isReversed;
        sort();
        editAsReply(interaction);
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
            StringBuilder content = asStringMessage(entriesThisPage, guildHeader);
            return new MessageBuilder(content).build();
        }
    }
}
