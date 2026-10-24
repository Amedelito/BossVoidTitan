package com.tuservidor.eggboss;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EggBossPlugin extends JavaPlugin {

    private static EggBossPlugin instance;
    private BossManager bossManager;

    @Override
    public void onEnable() {
        instance = this;
        this.bossManager = new BossManager(this);

        if (getCommand("eggboss") != null) {
            getCommand("eggboss").setExecutor(new EggBossCommand());
        }
        Bukkit.getPluginManager().registerEvents(new BossListeners(this, bossManager), this);

        getLogger().info("¡EggBossPlugin activado correctamente compartiendo ResourcePack!");
    }

    @Override
    public void onDisable() {
        if (bossManager != null) {
            bossManager.cleanupAllBosses();
        }
    }

    public static EggBossPlugin getInstance() {
        return instance;
    }
}
