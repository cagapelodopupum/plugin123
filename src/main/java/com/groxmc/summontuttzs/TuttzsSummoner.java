package com.groxmc.summontuttzs;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class TuttzsSummoner {

    private static final String TARGET_NAME = "tuttzs";

    public static void summon(SummonTuttzs plugin, Location fireLoc) {
        Player tuttzs = findTuttzs();

        if (tuttzs == null) {
            plugin.getLogger().info("[SummonTuttzs] tuttzs não está online, ignorando.");
            return;
        }

        // Centro do bloco de fogo azul (+ 0.5 pra ficar no meio)
        Location dest = fireLoc.clone().add(0.5, 0, 0.5);
        dest.setYaw(tuttzs.getLocation().getYaw());
        dest.setPitch(tuttzs.getLocation().getPitch());

        // Remove invisibilidade de poção se tiver
        removeInvisibility(tuttzs);

        // Remove vanish de plugins comuns (EssentialsX, CMI, SuperVanish, PremiumVanish)
        removeVanish(plugin, tuttzs);

        // Teleporta
        tuttzs.teleport(dest);

        // Efeitos imediatos no destino
        spawnEffectsAtDest(plugin, dest);

        // Mensagem pro tuttzs
        tuttzs.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "⚡ Você foi invocado pelo fogo azul! ⚡");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static Player findTuttzs() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(TARGET_NAME)) {
                return p;
            }
        }
        return null;
    }

    private static void removeInvisibility(Player player) {
        // Remove efeito de poção de invisibilidade
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    private static void removeVanish(SummonTuttzs plugin, Player player) {
        // ── EssentialsX ──────────────────────────────────────────────────────
        var essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essPlugin != null && essPlugin.isEnabled()) {
            try {
                var essentials = (com.earth2me.essentials.Essentials) essPlugin;
                var user = essentials.getUser(player);
                if (user != null && user.isVanished()) {
                    user.setVanished(false);
                }
            } catch (Exception | NoClassDefFoundError e) {
                plugin.getLogger().log(Level.WARNING, "[SummonTuttzs] Erro ao remover vanish do Essentials: " + e.getMessage());
            }
        }

        // ── SuperVanish / PremiumVanish ───────────────────────────────────────
        // Ambos respondem ao mesmo comando
        var svPlugin = Bukkit.getPluginManager().getPlugin("SuperVanish");
        var pvPlugin = Bukkit.getPluginManager().getPlugin("PremiumVanish");
        if ((svPlugin != null && svPlugin.isEnabled()) || (pvPlugin != null && pvPlugin.isEnabled())) {
            try {
                // API via evento customizado
                var event = new org.bukkit.event.player.PlayerCommandPreprocessEvent(player, "/vanish off");
                // Método direto: forçar aparecer via método da API
                // SuperVanish/PremiumVanish expõem API via serviço
                Object service = Bukkit.getServicesManager().load(Class.forName("de.myzelyam.api.vanish.VanishAPI"));
                if (service != null) {
                    var method = service.getClass().getMethod("showPlayer", Player.class);
                    method.invoke(service, player);
                }
            } catch (Exception | NoClassDefFoundError e) {
                // Tenta via comando como fallback
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sv show " + player.getName());
            }
        }

        // ── CMI ──────────────────────────────────────────────────────────────
        var cmiPlugin = Bukkit.getPluginManager().getPlugin("CMI");
        if (cmiPlugin != null && cmiPlugin.isEnabled()) {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi vanish " + player.getName() + " off");
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[SummonTuttzs] Erro ao remover CMI vanish: " + e.getMessage());
            }
        }

        // ── Garante que todos os players vejam o tuttzs ───────────────────────
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }

    private static void spawnEffectsAtDest(SummonTuttzs plugin, Location dest) {
        World world = dest.getWorld();
        if (world == null) return;

        // ── Raio imediato ─────────────────────────────────────────────────────
        world.strikeLightningEffect(dest); // strikeLightningEffect = raio visual SEM dano

        // ── Partículas de soul fire ao redor ──────────────────────────────────
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20) { // dura 1 segundo (20 ticks)
                    cancel();
                    return;
                }

                // Anel de partículas azuis
                for (int i = 0; i < 12; i++) {
                    double angle = (2 * Math.PI / 12) * i;
                    double radius = 1.5;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = dest.clone().add(x, 1, z);
                    world.spawnParticle(Particle.SOUL, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Partículas subindo
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, dest.clone().add(0, 0.5, 0), 5, 0.3, 0.5, 0.3, 0.02);
                world.spawnParticle(Particle.CLOUD, dest.clone().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.01);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // ── Som de raio + soul fire ───────────────────────────────────────────
        world.playSound(dest, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        world.playSound(dest, Sound.BLOCK_SOUL_SAND_HIT, 1.0f, 0.5f);
        world.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.7f);
    }
}
