package com.llamalad7.betterchat;

//import com.llamalad7.betterchat.command.CommandConfig;
import net.fabricmc.api.ClientModInitializer;

public class BetterChat implements ClientModInitializer {
    public static final String MODID = "betterchat";
    public static final String VERSION = "1.5";
//    private static ChatSettings settings;

//    @EventHandler
//    public void preinit(FMLPreInitializationEvent event) {
//        boolean firstRun = false;
//        if (!event.getSuggestedConfigurationFile().exists()) {
//            firstRun = true;
//        }
//        settings = new ChatSettings(new Configuration(event.getSuggestedConfigurationFile()));
//        if (firstRun) settings.resetConfig();
//        settings.loadConfig();
//    }

    @Override
    public void onInitializeClient() {
        //TODO: command
    }

//    public static ChatSettings getSettings() {
//        return settings;
//    }
}
