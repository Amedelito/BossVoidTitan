package com.tuservidor.eggboss;

import org.bukkit.*;
import org.bukkit.entity.Egg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class BossListeners implements Listener {

    private final EggBossPlugin plugin;
    private final BossManager bossManager;

    public BossListeners(EggBossPlugin plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    @EventHandler
    public void onPlaceBossEgg(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        if (item.getItemMeta().getPersistentDataContainer().has(CustomItems.KEY_EGG_SPAWNER(), PersistentDataType.BYTE)) {
            Player player = event.getPlayer();
            Location placedLoc = event.getBlockPlaced().getLocation();

            if (player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }

            Bukkit.getScheduler().runTask(plugin, () -> bossManager.startSummonRitual(placedLoc, player));
        }
    }

    @EventHandler
    public void onUseMobilityRelic(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        if (item.getItemMeta().getPersistentDataContainer().has(CustomItems.KEY_MOBILITY_ITEM(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            if (player.hasCooldown(Material.CARROT_ON_A_STICK)) {
                int remainingTicks = player.getCooldown(Material.CARROT_ON_A_STICK);
                double seconds = Math.round((remainingTicks / 20.0) * 10.0) / 10.0;
                player.sendMessage(ChatColor.RED + "⏱ Las Alas Cascarón se están recargando (" + seconds + "s).");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                return;
            }

            player.setCooldown(Material.CARROT_ON_A_STICK, 80);

            Vector direction = player.getLocation().getDirection().normalize();
            Vector boost = direction.multiply(1.35).setY(Math.max(0.55, direction.getY() * 0.85 + 0.35));
            player.setVelocity(boost);
            player.setFallDistance(0f);

            Location loc = player.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 1.4f);
            loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.2f);
            loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0, 0.5, 0), 25, 0.4, 0.2, 0.4, 0.08);
            loc.getWorld().spawnParticle(Particle.WAX_OFF, loc, 15, 0.4, 0.4, 0.4, 0.1);
        }
    }

    @EventHandler
    public void onBossEggHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Egg egg && "ExplosiveBossEgg".equals(egg.getCustomName())) {
            Location hitLoc = egg.getLocation();
            hitLoc.getWorld().createExplosion(hitLoc, 2.0f, false, false, egg);
            hitLoc.getWorld().spawnParticle(Particle.SLIME, hitLoc, 30, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (bossManager.isBoss(event.getEntity())) {
            Location loc = event.getEntity().getLocation();
            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.8f);
            loc.getWorld().spawnParticle(Particle.CRIT, loc.add(0, 1.2, 0), 15, 0.4, 0.5, 0.4, 0.1);
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (bossManager.isBoss(entity)) {
            event.getDrops().clear();
            event.setDroppedExp(500);
            bossManager.handleBossDeath(entity, entity.getKiller());
        }
    }
}
