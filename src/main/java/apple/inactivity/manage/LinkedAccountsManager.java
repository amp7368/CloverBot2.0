package apple.inactivity.manage;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LinkedAccountsManager {
    private final HashSet<LinkedAccount> linkedAccounts = new HashSet<LinkedAccount>();
    private transient HashMap<Long, LinkedAccount> discordToAccount = null;
    private transient HashMap<UUID, LinkedAccount> minecraftToAccount = null;
    private long discordServerId;
    private transient ServerManager serverManager = null;

    public LinkedAccountsManager(long discordServerId) {
        this.discordServerId = discordServerId;
    }

    private void verifyServerManager() {
        serverManager = Servers.getOrMake(discordServerId);
    }

    private void verifyMaps() {
        synchronized (linkedAccounts) {
            if (discordToAccount == null) {
                discordToAccount = new HashMap<>();
            }
            if (minecraftToAccount == null) {
                minecraftToAccount = new HashMap<>();
            }
            for (LinkedAccount linkedAccount : linkedAccounts) {
                this.discordToAccount.put(linkedAccount.getDiscord(), linkedAccount);
                this.minecraftToAccount.put(linkedAccount.getMinecraft(), linkedAccount);
            }
        }
    }

    public void addAccount(LinkedAccount account) {
        verifyMaps();
        verifyServerManager();
        synchronized (linkedAccounts) {
            this.linkedAccounts.add(account);
            if (discordToAccount != null) {
                discordToAccount.put(account.getDiscord(), account);
            }
            if (minecraftToAccount != null) {
                minecraftToAccount.put(account.getMinecraft(), account);
            }
        }
        serverManager.save();
    }

    public LinkedAccount getAccount(@NotNull UUID uuid) {
        verifyMaps();
        synchronized (linkedAccounts) {
            return this.minecraftToAccount.get(uuid);
        }
    }


    public List<LinkedAccount> listAccounts() {
        synchronized (linkedAccounts) {
            return new ArrayList<>(linkedAccounts);
        }
    }

    public void clearAllConfirm() {
        synchronized (linkedAccounts) {
            linkedAccounts.clear();
        }
    }

    public boolean hasAccount(UUID uuid) {
        verifyMaps();
        synchronized (linkedAccounts) {
            return minecraftToAccount.containsKey(uuid);
        }
    }
}
