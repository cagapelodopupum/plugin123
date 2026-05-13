package com.groxmc.summontuttzs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoulFireListener implements Listener {

    private final SummonTuttzs plugin;

    public SoulFireListener(SummonTuttzs plugin) {
        this.plugin = plugin;
    }

    // Detecta quando alguem usa isqueiro em cima de bloco de soul sand/soil
    // produzindo soul fire
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        if (placed.getType() == Material.SOUL_FIRE) {
            TuttzsSummoner.summon(plugin, placed.getLocation());
        }
    }

    // Detecta uso de flint and steel / fire charge em soul sand/soul soil
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        // Verifica se o bloco clicado é soul sand ou soul soil
        Material type = clicked.getType();
        if (type != Material.SOUL_SAND && type != Material.SOUL_SOIL) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // Verifica se o jogador usou isqueiro ou fire charge
        if (item.getType() != Material.FLINT_AND_STEEL && item.getType() != Material.FIRE_CHARGE) return;

        // O fogo vai aparecer no bloco acima do soul sand
        Block fireBlock = clicked.getRelative(org.bukkit.block.BlockFace.UP);

        // Aguarda 1 tick pro bloco aparecer de fato antes de verificar
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (fireBlock.getType() == Material.SOUL_FIRE) {
                TuttzsSummoner.summon(plugin, fireBlock.getLocation());
            }
        }, 1L);
    }
}
