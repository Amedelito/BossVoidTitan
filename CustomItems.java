package com.tuservidor.eggboss;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class CustomItems {

    // IDs de CustomModelData (Compartidos en el mismo Texture Pack)
    public static final int SLOT_COIN_CMD = 1001;        // ID de la moneda del plugin de Tragaperras
    public static final int EGG_SPAWNER_CMD = 2001;      // Huevo Invocador y Fase 1
    public static final int EGG_PHASE2_CMD = 2002;       // Modelo 3D Huevo Agrietado (Fase 2)
    public static final int EGG_PHASE3_CMD = 2003;       // Modelo 3D Huevo Dorado/Abierto (Fase 3)
    public static final int EGG_ATTACK_CMD = 2004;       // Modelo 3D Animación de Ataque
    public static final int MOBILITY_ITEM_CMD = 2010;    // Modelo 3D Ítem de Movilidad

    public static NamespacedKey KEY_EGG_SPAWNER() {
        return new NamespacedKey(EggBossPlugin.getInstance(), "egg_spawner");
    }

    public static NamespacedKey KEY_MOBILITY_ITEM() {
        return new NamespacedKey(EggBossPlugin.getInstance(), "mobility_relic");
    }

    public static NamespacedKey KEY_SLOT_COIN() {
        return new NamespacedKey(EggBossPlugin.getInstance(), "slot_coin");
    }

    // 1. Huevo Invocador 3D
    public static ItemStack createSpawnerEgg() {
        ItemStack item = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "🥚 Huevo Ancestral del Coloso");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Colócalo en el suelo para despertar",
                    ChatColor.GRAY + "al " + ChatColor.RED + "Rey Huevo Emperador" + ChatColor.GRAY + ".",
                    "",
                    ChatColor.YELLOW + "¡Prepara tu equipamiento antes de ponerlo!"
            ));
            meta.setCustomModelData(EGG_SPAWNER_CMD);
            meta.getPersistentDataContainer().set(KEY_EGG_SPAWNER(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // 2. Ítem de Movilidad 3D (Balanceado e Infinito)
    public static ItemStack createMobilityRelic() {
        ItemStack item = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "🪽 Alas Cascarón de Céfiro");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Reliquia mística obtenida del Rey Huevo.",
                    "",
                    ChatColor.GOLD + "Habilidad: Impulso Aéreo " + ChatColor.YELLOW + "(Clic Derecho)",
                    ChatColor.WHITE + "Te propulsa con viento hacia donde miras",
                    ChatColor.WHITE + "y anula el daño por caída del salto.",
                    "",
                    ChatColor.GREEN + "✔ Uso Infinito",
                    ChatColor.BLUE + "⏱ Enfriamiento: 4 segundos"
            ));
            meta.setCustomModelData(MOBILITY_ITEM_CMD);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(KEY_MOBILITY_ITEM(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // 3. Moneda de Tragaperras (Compatible con el Plugin 1 de Casino)
    public static ItemStack createSlotCoins(int amount) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "🪙 Ficha de Tragaperras");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Úsala en las máquinas tragaperras",
                    ChatColor.GRAY + "del Casino para ganar premios."
            ));
            meta.setCustomModelData(SLOT_COIN_CMD);
            meta.getPersistentDataContainer().set(KEY_SLOT_COIN(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Helper para cambiar el modelo 3D de la cabeza del Boss
    public static ItemStack createBossModelItem(int customModelData) {
        ItemStack item = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        return item;
    }
}
