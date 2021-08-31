package apple.inactivity.manage;

import apple.inactivity.mojang.MojangService;
import apple.utilities.request.SimpleExceptionHandler;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;

import java.util.Objects;
import java.util.UUID;

public class LinkedAccount {
    private long discord;
    private UUID minecraft;
    private String minecraftUsername;
    private String discordUsername;

    public LinkedAccount() {
    }

    public LinkedAccount(long discord, UUID minecraft, String minecraftUsername) {
        this.discord = discord;
        this.minecraft = minecraft;
        this.minecraftUsername = minecraftUsername;
    }

    public long getDiscord() {
        return discord;
    }

    public UUID getMinecraft() {
        return minecraft;
    }

    public void verifyMinecraftName(Runnable callback) {
        RequestPrioritySettingsBuilder<MojangService.ResponseMinecraftUsername[], MojangService.MojangPriority> settings = RequestPrioritySettingsBuilder.emptyPriority();
        settings.withPriorityExceptionHandler(new SimpleExceptionHandler(new Class[]{Exception.class}, callback));
        settings.withPriority(MojangService.MojangPriority.HIGH);
        MojangService.getPlayerName(minecraft, (username) -> {
            synchronized (this) {
                this.minecraftUsername = username[username.length-1];
            }
            callback.run();
        }, settings).completeAndRun();

    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.discord, this.minecraft);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LinkedAccount other) {
            return other.discord == this.discord && other.minecraft.equals(this.minecraft);
        }
        return false;
    }
}
