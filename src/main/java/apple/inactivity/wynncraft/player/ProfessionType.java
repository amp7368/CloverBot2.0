package apple.inactivity.wynncraft.player;

import apple.inactivity.utils.Pretty;

import java.util.function.Function;

public enum ProfessionType {
    ALCHEMISM(o -> o.alchemism),
    ARMORING(o -> o.armouring),
    COOKING(o -> o.cooking),
    COMBAT(o -> o.combat),
    FARMING(o -> o.farming),
    FISHING(o -> o.fishing),
    JEWELING(o -> o.jeweling),
    MINING(o -> o.mining),
    SCRIBING(o -> o.scribing),
    TAILORING(o -> o.tailoring),
    WEAPONSMITHING(o -> o.weaponsmithing),
    WOODCUTTING(o -> o.woodcutting),
    WOODWORKING(o -> o.woodworking);

    private static ProfessionType[] profs = null;
    private final Function<WynnPlayerClassProfessions, ProfessionLevel> get;
    private final String prettyName;

    ProfessionType(Function<WynnPlayerClassProfessions, ProfessionLevel> get) {
        this.get = get;
        this.prettyName = Pretty.uppercaseFirst(name());
    }

    public static ProfessionType[] profsNoCombat() {
        if (profs == null) {
            profs = new ProfessionType[values().length - 1];
            int i = 0;
            for (ProfessionType prof : values()) {
                if (prof != COMBAT)
                    profs[i++] = prof;
            }
        }
        return profs;
    }

    public ProfessionLevel get(WynnPlayerClassProfessions wynnClass) {
        return get.apply(wynnClass);
    }

    public String prettyName() {
        return prettyName;
    }
}
