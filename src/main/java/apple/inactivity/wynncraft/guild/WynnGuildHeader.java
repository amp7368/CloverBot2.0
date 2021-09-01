package apple.inactivity.wynncraft.guild;

import com.google.gson.annotations.Expose;

import java.util.Objects;
import java.util.regex.Pattern;

public class WynnGuildHeader {
    @Expose
    public String name;
    @Expose
    public String prefix = null;
    private transient Pattern namePatternIgnoreCase = null;
    private transient Pattern namePattern = null;

    public WynnGuildHeader() {
    }

    public WynnGuildHeader(String name) {
        this.name = name;
    }

    public WynnGuildHeader(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public boolean matchesTag(String guildName) {
        return prefix != null && prefix.equals(guildName);
    }

    public boolean matchesTagIgnoreCase(String guildName) {
        return prefix != null && prefix.equalsIgnoreCase(guildName);
    }

    private void verifyPattern() {
        if (namePatternIgnoreCase == null) {
            namePatternIgnoreCase = Pattern.compile(".*" + Pattern.quote(name) + ".*", Pattern.CASE_INSENSITIVE);
            namePattern = Pattern.compile(".*" + Pattern.quote(name) + ".*");
        }
    }

    public boolean matchesGuildName(String guildName) {
        verifyPattern();
        return namePattern.asMatchPredicate().test(guildName);
    }


    public boolean matchesGuildNameIgnoreCase(String guildName) {
        verifyPattern();
        return namePatternIgnoreCase.asMatchPredicate().test(guildName);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.prefix);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WynnGuildHeader other) {
            return this.name.equals(other.name);
//            return this.name.equals(other.name) && this.prefix.equals(other.prefix);
        }
        return false;
    }

    @Override
    public String toString() {
        return "WynnGuildHeader{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
