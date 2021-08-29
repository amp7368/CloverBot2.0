package apple.inactivity.manage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class Servers {
    private static final HashMap<Long, ServerManager> servers = new HashMap<>();

    public static void loadNow() {
        File folder = ServerManager.getDBFolder();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.length() > 5) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }
                long id;
                try {
                    id = Long.parseLong(fileName);
                } catch (NumberFormatException e) {
                    continue;
                }
                ServerManager manager = new ServerManager(id).loadNow(ServerManager.class);
                if (manager != null)
                    servers.put(manager.getId(), manager);
            }
        }
    }

    @NotNull
    public static ServerManager getOrMake(long serverId) {
        return servers.computeIfAbsent(serverId, (k) -> new ServerManager(serverId));
    }
}
