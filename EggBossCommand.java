package com.tuservidor.eggboss;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EggBossCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== Comandos EggBoss ===");
            player.sendMessage(ChatColor.YELLOW + "/eggboss egg " + ChatColor.WHITE + "- Recibe el Huevo Invocador 3D");
            player.sendMessage(ChatColor.YELLOW + "/eggboss relic " + ChatColor.WHITE + "- Recibe las Alas Cascarón 3D (Movilidad)");
            player.sendMessage(ChatColor.YELLOW + "/eggboss coins " + ChatColor.WHITE + "- Recibe 10 Monedas de Tragaperras");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "egg" -> {
                player.getInventory().addItem(CustomItems.createSpawnerEgg());
                player.sendMessage(ChatColor.GREEN + "¡Has recibido el Huevo Ancestral del Coloso! Ponlo en el suelo.");
            }
            case "relic" -> {
                player.getInventory().addItem(CustomItems.createMobilityRelic());
                player.sendMessage(ChatColor.AQUA + "¡Has recibido las Alas Cascarón de Céfiro!");
            }
            case "coins" -> {
                player.getInventory().addItem(CustomItems.createSlotCoins(10));
                player.sendMessage(ChatColor.GOLD + "¡Has recibido 10 Fichas de Tragaperras!");
            }
        }
        return true;
    }
}
