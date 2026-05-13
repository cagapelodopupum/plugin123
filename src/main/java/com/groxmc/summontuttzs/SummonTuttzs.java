package com.groxmc.summontuttzs;

import org.bukkit.plugin.java.JavaPlugin;

public class SummonTuttzs extends JavaPlugin {

    private static SummonTuttzs instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new SoulFireListener(this), this);

        var cmd = getCommand("summontuttzs");
        if (cmd != null) {
            cmd.setExecutor(new SummonCommand(this));
        }

        getLogger().info("SummonTuttzs ativado! Fogo azul = tuttzs chegando.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SummonTuttzs desativado.");
    }

    public static SummonTuttzs getInstance() {
        return instance;
    }
}
