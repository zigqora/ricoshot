package zigqora.ricoshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RicoshotConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("ricoshot.json").toFile();

    public static RicoshotConfig instance = new RicoshotConfig();

    public boolean enableActionBarText = true;
    public String ricoshotText = "§6§l+ RICOSHOT";
    public String ricoshotNoTargetsText = "§6§l+ RICOSHOT §7(NO TARGETS)";
    public String ultraRicoshotText = "§b§l+ ULTRARICOSHOT §a§l(x{chain})";
    public String ultraRicoshotPerfectText = "§d§l+ ULTRARICOSHOT §e§l(PERFECT SPLIT!)";
    public String shieldParryText = "§e§l+ SHIELD PARRY!";
    public String ricoshotBlockedText = "§c§l+ RICOSHOT BLOCKED!";

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, RicoshotConfig.class);
                if (instance == null) {
                    instance = new RicoshotConfig();
                }
            } catch (Exception e) {
                Ricoshot.LOGGER.error("Failed to load Ricoshot config, using defaults", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            Ricoshot.LOGGER.error("Failed to save Ricoshot config", e);
        }
    }
}
