package net.ctrlaltmilk.globaloptions;

import net.neoforged.neoforge.common.util.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GlobalOptions {
    public static final Logger LOGGER = LoggerFactory.getLogger(GlobalOptions.class);

    public static final Lazy<GlobalOptionsConfig> CONFIG = Lazy.of(() -> GlobalOptionsConfig.load(configFile()));

    private static File minecraftDir() {
        String homeDir = System.getProperty("user.home");
        String os = System.getProperty("os.name");

        String path;
        if (os.contains("win")) {
            path = "/AppData/Roaming/.minecraft/";
        } else if (os.contains("mac")) {
            path = "/Library/Application Support/minecraft/";
        } else {
            path = "/.minecraft/";
        }

        File minecraftDir = new File(homeDir, path);
        minecraftDir.mkdirs();

        return minecraftDir;
    }

    public static File configFile() {
        File configDir = new File(minecraftDir(), "config/");
        configDir.mkdirs();

        return new File(configDir, "globaloptions.txt");
    }

    public static File optionsFile() {
        return new File(minecraftDir(), "options.txt");
    }
}
