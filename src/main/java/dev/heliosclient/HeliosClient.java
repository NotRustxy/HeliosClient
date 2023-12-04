package dev.heliosclient;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.gui.Quadtree;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.DamageUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.util.cape.CapeSynchronizer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HeliosClient implements ModInitializer {
    public static final HeliosClient INSTANCE = new HeliosClient();
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger("Helios Client");
    public static final String clientTag = ColorUtils.red + "Helios Client";
    public static final String versionTag = ColorUtils.gray + "v0.dev";
    public static final String MODID = "heliosclient";
    public static Quadtree quadTree;
    public static Config CONFIG = new Config();
    public static int uiColorA = 0xFF55FFFF;
    public static int uiColor = 0x55FFFF;
    public static FontManager fontManager = new FontManager();
    public static NotificationManager notificationManager = new NotificationManager();
    public static AddonManager addonManager = new AddonManager();
    public static ClickGUI clickGUI = new ClickGUI();


    @Override
    public void onInitialize() {
        LOGGER.info("Helios Client loading...");
        addonManager.loadAddons();
        AddonManager.initializeAddons();

        Categories.registerCategories();
        SoundUtils.registerSounds();


        loadConfig();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if(MC.player !=null) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture, CapeManager.getElytraTexture(MC.player));
                }
            }
        });


        EventManager.register(fontManager);
        EventManager.register(notificationManager);
        EventManager.register(dev.heliosclient.util.Renderer2D.INSTANCE);
        EventManager.register(KeybindManager.INSTANCE);
        EventManager.register(ColorManager.INSTANCE);
        EventManager.register(new DamageUtils());

        if (MC.getWindow() != null) {
            quadTree = new Quadtree(0);
        }
        ClickGUIScreen.INSTANCE.onLoad();

        CapeManager.capes = CapeManager.loadCapes();
        CapeSynchronizer.registerCapeSyncPacket();
    }

    public void loadConfig() {
        LOGGER.info("Loading config...");
        if (!CONFIG.doesConfigExist()) {
            CONFIG.loadDefaultConfig();
            CONFIG.save();
        }
        CONFIG.load();

        for (Module_ m : ModuleManager.INSTANCE.modules) {
            for (SettingGroup settingBuilder : m.settingGroups) {
                for (Setting s : settingBuilder.getSettings()) {
                    s.value = ((Map<String, Object>) ((Map<String, Object>) CONFIG.config.get("modules")).get(m.name))
                            .get(s.name);
                }
            }
        }
        CommandManager.prefix = (String) CONFIG.config.get("prefix");
    }

    public void saveConfig() {
        LOGGER.info("Saving config...");
        Map<String, Object> mi = new HashMap<>();
        for (Module_ m : ModuleManager.INSTANCE.modules) {
            Map<String, Object> mo = new HashMap<>();
            for (SettingGroup settingBuilder : m.settingGroups) {
                for (Setting s : settingBuilder.getSettings()) {
                    mo.put(s.name, s.value);
                }
                mi.put(m.name, mo);
            }
        }
        CONFIG.config.put("modules", mi);
        Map<String, Object> pi = new HashMap<>();
        for (CategoryPane c : ClickGUIScreen.INSTANCE.categoryPanes) {
            Map<String, Object> po = new HashMap<>();
            po.put("x", c.x);
            po.put("y", c.y);
            po.put("collapsed", c.collapsed);
            pi.put(c.category.name, po);
        }
        CONFIG.config.put("panes", pi);
        CONFIG.config.put("prefix", CommandManager.get().getPrefix());
        CONFIG.save();
    }
}
