package apple.inactivity.wynncraft.guild;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class WynnGuildMember {
    public String name;
    public String uuid;
    public String rank;
    @SerializedName(value = "contributed")
    public long xpContributed;
    public Date joined;
}
