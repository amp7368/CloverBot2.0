package apple.inactivity;

import apple.utilities.util.FileFormatting;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

public class CloverConfig {
    public boolean versionIsDate = true;
    public boolean shouldAppend = true;
    public long cloverStats = 882396439420993546L;

    public static CloverConfig load() {
        File file = FileFormatting.fileWithChildren(FileFormatting.getDBFolder(CloverMain.class), "config", "cloverConfig.yaml");
        file.getParentFile().mkdirs();
        Yaml yaml = new Yaml();
        CloverConfig config;
        try {
            config = yaml.load(new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException e) {
            config = new CloverConfig();
        }
        try {
            file.createNewFile();
            yaml.dump(config, new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return config;
    }

    public boolean getVersionIsDate() {
        return versionIsDate;
    }

    public boolean isShouldAppend() {
        return shouldAppend;
    }

    public long getCloverStatsChannel() {
        return cloverStats;
    }
}
