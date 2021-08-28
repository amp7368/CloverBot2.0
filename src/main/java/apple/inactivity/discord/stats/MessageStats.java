package apple.inactivity.discord.stats;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.inactivity.utils.Pretty;
import apple.inactivity.wynncraft.player.ProfessionLevel;
import apple.inactivity.wynncraft.player.ProfessionType;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.inactivity.wynncraft.player.WynnPlayerClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.*;
import java.util.stream.Collectors;


public class MessageStats extends ACDGuiPageable {
    private static final Map<String, Integer> rankColors = new HashMap<>() {
        {
            put("DEFAULT", 0x656665);
            put("HERO", 0xba36d1);
            put("VIP+", 0x21ccd9);
            put("VIP", 0x36d158);
            put("CHAMPION", 0xf6a831);
        }
    };
    private final WynnPlayer player;

    public MessageStats(ACD acd, MessageChannel channel, WynnPlayer player) {
        super(acd, channel);
        this.player = player;
        addPage(this::makeInfoMessage);
        addPage(this::makeGlobalMessage);
        for (WynnPlayerClass playerClass : Arrays.stream(player.classes).
                sorted(Comparator.comparingInt((WynnPlayerClass o) -> ProfessionType.COMBAT.get(o.professions).level).
                        thenComparing(o -> ProfessionType.COMBAT.get(o.professions).level)).collect(Collectors.toList())) {
            addPage(() -> makeClasses(playerClass));
        }
    }


    private Message makeInfoMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(String.format("General Page (%d)", page + 1));
        colorAndTitle(embed);
        embed.addField("Activity", String.format("**%s days** since last active", this.player.inactivity()), false);
        embed.addField("Playtime", String.format("%d hours", this.player.hoursPlayed()), false);
        return new MessageBuilder().setEmbeds(embed.build()).build();
    }

    private Message makeGlobalMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(String.format("Total Level Page (%d)", page + 1));
        colorAndTitle(embed);
        for (ProfessionType professionType : ProfessionType.profsNoCombat()) {
            ProfessionLevel level = player.getProf(professionType);
            embed.addField(professionType.prettyName(), String.format("Level %d.%d", level.level, (int) (level.xp)), true);
        }

        return new MessageBuilder().setEmbeds(embed.build()).build();
    }

    private Message makeClasses(WynnPlayerClass playerClass) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(String.format("Class Page (%d)", page + 1));
        colorAndTitle(embed);
        embed.setDescription(String.format("***%s [Combat %d]***", playerClass.getName(), ProfessionType.COMBAT.get(playerClass.professions).level));
        for (ProfessionType professionType : ProfessionType.profsNoCombat()) {
            ProfessionLevel level = professionType.get(playerClass.professions);
            embed.addField(professionType.prettyName(), String.format("Level %d.%d", level.level, (int) (level.xp)), true);
        }

        return new MessageBuilder().setEmbeds(embed.build()).build();
    }

    private void colorAndTitle(EmbedBuilder embed) {
        embed.setTitle(String.format("%s [%s]", this.player.username, Pretty.uppercaseFirst(this.player.meta.tag.value)));
        embed.setColor(getRankColor(this.player.meta.tag.value.toUpperCase(Locale.ROOT)));
    }

    private int getRankColor(String rank) {
        Integer color = rankColors.get(rank);
        if (color == null) color = rankColors.get("DEFAULT");
        return color;
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}
