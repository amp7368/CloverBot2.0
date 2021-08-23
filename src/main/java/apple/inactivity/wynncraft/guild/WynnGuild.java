package apple.inactivity.wynncraft.guild;


import java.util.Date;

public class WynnGuild {
    public String name;
    public String prefix;
    public WynnGuildMember[] members;
    public long level;
    public Date date;

    public WynnGuildHeader toHeader() {
        return new WynnGuildHeader(name, prefix);
    }
}
