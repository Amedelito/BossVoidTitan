package com.tuservidor.eggboss;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class BossManager {

    private final EggBossPlugin plugin;
    private final Map<UUID, ActiveBoss> activeBosses = new HashMap<>();
    private final Random random = new Random();

    public BossManager(EggBossPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isBoss(Entity entity) {
        return activeBosses.containsKey(entity.getUniqueId());
    }

    public void startSummonRitual(Location blockLoc, Player summoner) {
        Location center = blockLoc.clone().add(0.5, 0, 0.5);
        blockLoc.getBlock().setType(Material.AIR);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "  ⚠ ¡EL REY HUEVO EMPERADOR ESTÁ DESPERTANDO! ⚠");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "  Invocado por: " + ChatColor.WHITE + summoner.getName());
        Bukkit.broadcastMessage(ChatColor.GRAY + "  Ubicación: " + center.getBlockX() + ", " + center.getBlockY() + ", " + center.getBlockZ());
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage("");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(
                    ChatColor.GOLD + "" + ChatColor.BOLD + "🥚 REY HUEVO",
                    ChatColor.RED + "¡Se está incubando la destrucción!",
                    10, 60, 20
            );
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.7f);
        }

        ArmorStand ritualStand = center.getWorld().spawn(center, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.getEquipment().setHelmet(CustomItems.createBossModelItem(CustomItems.EGG_SPAWNER_CMD));
        });

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 60) {
                    ritualStand.remove();
                    center.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, center, 2);
                    center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                    center.getWorld().strikeLightningEffect(center);
                    spawnActualBoss(center);
                    cancel();
                    return;
                }

                Location nextLoc = ritualStand.getLocation().add(0, 0.03, 0);
                nextLoc.setYaw(nextLoc.getYaw() + 25f);
                ritualStand.teleport(nextLoc);

                center.getWorld().spawnParticle(Particle.FLAME, ritualStand.getLocation().add(0, 1.2, 0), 10, 0.4, 0.4, 0.4, 0.05);
                center.getWorld().spawnParticle(Particle.SPELL_WITCH, ritualStand.getLocation().add(0, 1.0, 0), 8, 0.5, 0.5, 0.5, 0.1);

                if (ticks % 10 == 0) {
                    center.getWorld().playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f, 0.5f + (ticks / 40f));
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void spawnActualBoss(Location loc) {
        World world = loc.getWorld();

        Zombie hitbox = world.spawn(loc, Zombie.class, z -> {
            z.setAdult();
            z.setInvisible(true);
            z.setSilent(true);
            z.setRemoveWhenFarAway(false);
            z.setShouldBurnInDay(false);
            z.getEquipment().clear();
            z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(350.0);
            z.setHealth(350.0);
            z.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.28);
            z.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(9.0);
            z.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.85);
        });

        ArmorStand modelStand = world.spawn(loc, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setBasePlate(false);
            stand.getEquipment().setHelmet(CustomItems.createBossModelItem(CustomItems.EGG_SPAWNER_CMD));
            stand.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "👑 Rey Huevo Emperador 👑");
            stand.setCustomNameVisible(true);
        });

        BossBar bossBar = Bukkit.createBossBar(
                ChatColor.GOLD + "" + ChatColor.BOLD + "🥚 REY HUEVO EMPERADOR " + ChatColor.YELLOW + "[FASE 1]",
                BarColor.YELLOW,
                BarStyle.SEGMENTED_10,
                BarFlag.PLAY_BOSS_MUSIC
        );

        ActiveBoss activeBoss = new ActiveBoss(hitbox, modelStand, bossBar);
        activeBosses.put(hitbox.getUniqueId(), activeBoss);

        startBossLoop(activeBoss);
    }

    private void startBossLoop(ActiveBoss boss) {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!boss.hitbox.isValid() || boss.hitbox.isDead()) {
                    cleanupBoss(boss.hitbox.getUniqueId(), false);
                    cancel();
                    return;
                }

                Location bossLoc = boss.hitbox.getLocation();
                double maxHealth = boss.hitbox.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHealth = boss.hitbox.getHealth();
                double healthRatio = Math.max(0.0, Math.min(1.0, currentHealth / maxHealth));

                boss.bossBar.setProgress(healthRatio);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(bossLoc.getWorld()) && p.getLocation().distanceSquared(bossLoc) <= 2500) {
                        boss.bossBar.addPlayer(p);
                    } else {
                        boss.bossBar.removePlayer(p);
                    }
                }

                if (healthRatio <= 0.65 && boss.phase == 1) {
                    enterPhase2(boss);
                } else if (healthRatio <= 0.30 && boss.phase == 2) {
                    enterPhase3(boss);
                }

                double bobbing = Math.sin(tick * 0.25) * 0.15;
                Location standLoc = bossLoc.clone().add(0, bobbing, 0);
                standLoc.setYaw(bossLoc.getYaw());
                boss.modelStand.teleport(standLoc);

                double tiltX = Math.sin(tick * 0.2) * 0.12;
                double tiltZ = Math.cos(tick * 0.2) * 0.12;
                boss.modelStand.setHeadPose(new EulerAngle(tiltX, 0, tiltZ));

                if (boss.phase == 2) {
                    bossLoc.getWorld().spawnParticle(Particle.LAVA, bossLoc.clone().add(0, 1.2, 0), 2, 0.4, 0.5, 0.4, 0);
                } else if (boss.phase == 3) {
                    bossLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, bossLoc.clone().add(0, 1.2, 0), 5, 0.5, 0.5, 0.5, 0.02);
                    bossLoc.getWorld().spawnParticle(Particle.TOTEM, bossLoc.clone().add(0, 1.5, 0), 3, 0.4, 0.4, 0.4, 0.05);
                }

                int attackInterval = (boss.phase == 3) ? 90 : (boss.phase == 2 ? 120 : 150);
                if (tick > 0 && tick % attackInterval == 0) {
                    performSpecialAttack(boss);
                }

                tick += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void enterPhase2(ActiveBoss boss) {
        boss.phase = 2;
        boss.bossBar.setColor(BarColor.RED);
        boss.bossBar.setTitle(ChatColor.RED + "" + ChatColor.BOLD + "🥚 REY HUEVO AGRIETADO " + ChatColor.GOLD + "[FASE 2]");
        boss.baseModelCmd = CustomItems.EGG_PHASE2_CMD;
        boss.modelStand.getEquipment().setHelmet(CustomItems.createBossModelItem(boss.baseModelCmd));

        boss.hitbox.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.33);
        Location loc = boss.hitbox.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 2.0f, 1.2f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, 1, 0), 3);

        announceNearby(loc, ChatColor.RED + "" + ChatColor.BOLD + "¡FASE 2! " + ChatColor.YELLOW + "¡El cascarón del Rey Huevo se agrieta y libera calor infernal!", "¡CASCARÓN AGRIETADO!", ChatColor.GOLD + "Fase 2 alcanzada");
    }

    private void enterPhase3(ActiveBoss boss) {
        boss.phase = 3;
        boss.bossBar.setColor(BarColor.PURPLE);
        boss.bossBar.setTitle(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "👑 NÚCLEO DEL REY HUEVO " + ChatColor.RED + "[FASE FINAL]");
        boss.baseModelCmd = CustomItems.EGG_PHASE3_CMD;
        boss.modelStand.getEquipment().setHelmet(CustomItems.createBossModelItem(boss.baseModelCmd));

        boss.hitbox.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.37);
        boss.hitbox.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(13.0);

        Location loc = boss.hitbox.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 1.4f);
        loc.getWorld().strikeLightningEffect(loc);

        announceNearby(loc, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "¡FASE FINAL! " + ChatColor.RED + "¡El Rey Huevo ha entrado en FRENESÍ TOTAL!", "¡FRENESÍ ANCESTRAL!", ChatColor.RED + "Fase 3 - ¡Máximo Peligro!");
    }

    private void performSpecialAttack(ActiveBoss boss) {
        Location loc = boss.hitbox.getLocation();
        List<Player> nearbyPlayers = getNearbyPlayers(loc, 20);
        if (nearbyPlayers.isEmpty()) return;

        boss.modelStand.getEquipment().setHelmet(CustomItems.createBossModelItem(CustomItems.EGG_ATTACK_CMD));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (boss.modelStand.isValid()) {
                boss.modelStand.getEquipment().setHelmet(CustomItems.createBossModelItem(boss.baseModelCmd));
            }
        }, 25L);

        int attackChoice = random.nextInt(3);

        switch (attackChoice) {
            case 0 -> attackShockwaveSlam(boss, nearbyPlayers);
            case 1 -> attackExplosiveYolkBarrage(boss, nearbyPlayers);
            case 2 -> attackSummonMinions(boss, loc);
        }
    }

    private void attackShockwaveSlam(ActiveBoss boss, List<Player> targets) {
        Location loc = boss.hitbox.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
        boss.hitbox.setVelocity(new Vector(0, 1.15, 0));

        for (Player p : targets) {
            p.sendMessage(ChatColor.GOLD + "[Rey Huevo] " + ChatColor.RED + "¡Aplastamiento Sísmico en camino, salta!");
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!boss.hitbox.isValid()) return;
            Location slamLoc = boss.hitbox.getLocation();
            slamLoc.getWorld().playSound(slamLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
            slamLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, slamLoc, 1);
            slamLoc.getWorld().spawnParticle(Particle.CLOUD, slamLoc, 80, 3.0, 0.2, 3.0, 0.1);

            for (Player p : getNearbyPlayers(slamLoc, 12)) {
                Vector knockback = p.getLocation().toVector().subtract(slamLoc.toVector()).normalize().multiply(1.4).setY(0.65);
                p.setVelocity(knockback);
                p.damage(8.0 + (boss.phase * 2), boss.hitbox);
            }
        }, 25L);
    }

    private void attackExplosiveYolkBarrage(ActiveBoss boss, List<Player> targets) {
        Location loc = boss.hitbox.getLocation().add(0, 1.5, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);

        for (Player target : targets) {
            target.sendMessage(ChatColor.GOLD + "[Rey Huevo] " + ChatColor.YELLOW + "¡Lluvia de huevos explosivos!");
            for (int i = 0; i < boss.phase + 1; i++) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!boss.hitbox.isValid() || !target.isOnline()) return;
                    Egg projectile = boss.hitbox.launchProjectile(Egg.class);
                    Vector dir = target.getEyeLocation().toVector().subtract(boss.hitbox.getLocation().add(0, 1.5, 0).toVector()).normalize().multiply(1.3);
                    projectile.setVelocity(dir);
                    projectile.setCustomName("ExplosiveBossEgg");
                }, i * 6L);
            }
        }
    }

    private void attackSummonMinions(ActiveBoss boss, Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_HURT, 2.0f, 0.5f);
        int amount = boss.phase + 1;

        for (Player p : getNearbyPlayers(loc, 20)) {
            p.sendMessage(ChatColor.GOLD + "[Rey Huevo] " + ChatColor.RED + "¡Mis guardianes del cascarón os picotearán!");
        }

        for (int i = 0; i < amount; i++) {
            Location spawnLoc = loc.clone().add((random.nextDouble() - 0.5) * 4, 0.5, (random.nextDouble() - 0.5) * 4);
            loc.getWorld().spawn(spawnLoc, Zombie.class, z -> {
                z.setBaby();
                z.setCustomName(ChatColor.YELLOW + "🐣 Polluelo Real Furioso");
                z.setCustomNameVisible(true);
                z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(16.0);
                z.setHealth(16.0);
                z.getEquipment().setHelmet(new ItemStack(Material.GOLD_BLOCK));
                z.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 1));
            });
            loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, spawnLoc, 15, 0.3, 0.3, 0.3, 0.1);
        }
    }

    public void handleBossDeath(LivingEntity entity, Player killer) {
        UUID id = entity.getUniqueId();
        ActiveBoss boss = activeBosses.get(id);
        if (boss == null) return;

        Location deathLoc = entity.getLocation().add(0, 1, 0);
        World world = deathLoc.getWorld();

        world.playSound(deathLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
        world.playSound(deathLoc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.2f, 1.4f);
        world.spawnParticle(Particle.TOTEM, deathLoc, 150, 1.5, 1.5, 1.5, 0.3);
        world.spawnParticle(Particle.FIREWORKS_SPARK, deathLoc, 100, 1.2, 1.2, 1.2, 0.2);

        String killerName = (killer != null) ? killer.getName() : "los Guerreros";
        int coinsDropped = 15 + random.nextInt(21);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GREEN + "=============================================");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "  🏆 ¡EL REY HUEVO HA SIDO DERROTADO! 🏆");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "  Golpe de gracia por: " + ChatColor.WHITE + killerName);
        Bukkit.broadcastMessage(ChatColor.AQUA + "  Recompensas: " + ChatColor.WHITE + "Alas Cascarón 3D + " + coinsDropped + " Fichas de Casino");
        Bukkit.broadcastMessage(ChatColor.GREEN + "=============================================");
        Bukkit.broadcastMessage("");

        for (Player p : getNearbyPlayers(deathLoc, 40)) {
            p.sendTitle(
                    ChatColor.GOLD + "" + ChatColor.BOLD + "¡VICTORIA ÉPICA!",
                    ChatColor.GREEN + "El Rey Huevo Emperador ha caído",
                    10, 70, 20
            );
        }

        Item relicDrop = world.dropItemNaturally(deathLoc, CustomItems.createMobilityRelic());
        relicDrop.setGlowing(true);
        relicDrop.setCustomName(ChatColor.AQUA + "" + ChatColor.BOLD + "🪽 Alas Cascarón de Céfiro");
        relicDrop.setCustomNameVisible(true);

        Item coinsDrop = world.dropItemNaturally(deathLoc, CustomItems.createSlotCoins(coinsDropped));
        coinsDrop.setGlowing(true);
        coinsDrop.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "🪙 Fichas de Tragaperras x" + coinsDropped);
        coinsDrop.setCustomNameVisible(true);

        cleanupBoss(id, true);
    }

    public void cleanupBoss(UUID id, boolean alreadyHandled) {
        ActiveBoss boss = activeBosses.remove(id);
        if (boss != null) {
            boss.bossBar.removeAll();
            if (boss.modelStand != null && boss.modelStand.isValid()) {
                boss.modelStand.remove();
            }
            if (!alreadyHandled && boss.hitbox != null && boss.hitbox.isValid()) {
                boss.hitbox.remove();
            }
        }
    }

    public void cleanupAllBosses() {
        for (UUID id : new HashSet<>(activeBosses.keySet())) {
            cleanupBoss(id, false);
        }
    }

    private void announceNearby(Location loc, String chatMsg, String title, String subtitle) {
        for (Player p : getNearbyPlayers(loc, 35)) {
            p.sendMessage(chatMsg);
            p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + title, subtitle, 5, 40, 10);
        }
    }

    private List<Player> getNearbyPlayers(Location loc, double radius) {
        List<Player> list = new ArrayList<>();
        double sq = radius * radius;
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getGameMode() != GameMode.SPECTATOR && p.getLocation().distanceSquared(loc) <= sq) {
                list.add(p);
            }
        }
        return list;
    }

    private static class ActiveBoss {
        final Zombie hitbox;
        final ArmorStand modelStand;
        final BossBar bossBar;
        int phase = 1;
        int baseModelCmd = CustomItems.EGG_SPAWNER_CMD;

        ActiveBoss(Zombie hitbox, ArmorStand modelStand, BossBar bossBar) {
            this.hitbox = hitbox;
            this.modelStand = modelStand;
            this.bossBar = bossBar;
        }
    }
}
