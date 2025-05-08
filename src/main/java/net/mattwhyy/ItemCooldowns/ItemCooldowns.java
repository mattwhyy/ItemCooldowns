package net.mattwhyy.ItemCooldowns;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ItemCooldowns extends JavaPlugin implements Listener {
    private Map<Material, Integer> itemCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveResourceIfNotExists();
        reloadConfig();
        loadCooldowns();
        getServer().getPluginManager().registerEvents(this, this);

        for (Player player : getServer().getOnlinePlayers()) {
            for (Map.Entry<Material, Integer> entry : itemCooldowns.entrySet()) {
                if (player.hasCooldown(entry.getKey())) {
                    player.setCooldown(entry.getKey(), entry.getValue() * 20);
                }
            }
        }
    }

    private void saveResourceIfNotExists() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    @Override
    public void onDisable() {
        saveCooldowns();
    }

    private void loadCooldowns() {
        FileConfiguration config = getConfig();

        if (!config.contains("cooldowns")) {
            return;
        }

        itemCooldowns.clear();

        for (String key : config.getConfigurationSection("cooldowns").getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                int cooldown = config.getInt("cooldowns." + key);
                itemCooldowns.put(material, cooldown);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material in config: " + key);
            }
        }
    }

    private void saveCooldowns() {
        FileConfiguration config = getConfig();

        if (config.getConfigurationSection("cooldowns") == null) {
            config.createSection("cooldowns");
        }

        for (Map.Entry<Material, Integer> entry : itemCooldowns.entrySet()) {
            config.set("cooldowns." + entry.getKey().name().toLowerCase(), entry.getValue());
        }

        saveConfig();
        reloadConfig();
    }

    private void applyCooldown(Player player, Material item) {
        if (itemCooldowns.containsKey(item)) {
            int cooldownTicks = itemCooldowns.get(item) * 20;
            player.setCooldown(item, cooldownTicks);
        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Material item = player.getInventory().getItemInMainHand().getType();

            if (isEdibleOrDrinkable(item) || item == Material.BOW || item == Material.TRIDENT || item == Material.CROSSBOW || item == Material.SHIELD) {
                return;
            }

            if (itemCooldowns.containsKey(item)) {
                if (player.hasCooldown(item)) {
                    event.setCancelled(true);
                    return;
                }

                getServer().getScheduler().runTaskLater(this, () -> applyCooldown(player, item), 1L);
            }
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material item = event.getItem().getType();
        applyCooldown(player, item);
    }

    private boolean isEdibleOrDrinkable(Material item) {
        return item.isEdible() ||
                item == Material.POTION ||
                item == Material.MILK_BUCKET ||
                item == Material.HONEY_BOTTLE;
    }


    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Material shotItem = event.getBow().getType();

            if (shotItem == Material.BOW || shotItem == Material.CROSSBOW) {
                applyCooldown(player, shotItem);
            }
        }
    }

    @EventHandler
    public void onTridentThrow(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            if (event.getEntity() instanceof org.bukkit.entity.Trident) {
                applyCooldown(player, Material.TRIDENT);
            }
        }
    }

    @EventHandler
    public void onRiptideUse(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        applyCooldown(player, Material.TRIDENT);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Material item = player.getInventory().getItemInMainHand().getType();

            if (isWeaponOrTool(item) && itemCooldowns.containsKey(item)) {
                if (player.hasCooldown(item)) {
                    event.setCancelled(true);
                } else {
                    applyCooldown(player, item);
                }
            }
        }
    }

    private boolean isWeaponOrTool(Material item) {
        return item.name().endsWith("_SWORD") ||
                item.name().endsWith("_AXE") ||
                item.name().endsWith("_PICKAXE") ||
                item.name().endsWith("_SHOVEL") ||
                item.name().endsWith("_HOE") ||
                item.name().contains("MACE") ||
                item == Material.TRIDENT;
    }

    @EventHandler
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (player.isBlocking() && player.getInventory().getItemInOffHand().getType() == Material.SHIELD || player.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
                applyCooldown(player, Material.SHIELD);
            }
        }
    }

    @EventHandler
    public void onTotemUse(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (player.hasCooldown(Material.TOTEM_OF_UNDYING)) {
                event.setCancelled(true);
                return;
            }

            applyCooldown(player, Material.TOTEM_OF_UNDYING);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setcooldown")) {
            if (!(sender instanceof Player) || !sender.hasPermission("itemcooldowns.set")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length != 2) {
                sender.sendMessage("§eUsage: /setcooldown <item> <seconds>");
                return true;
            }
            try {
                Material material = Material.valueOf(args[0].toUpperCase());
                int cooldown = Integer.parseInt(args[1]);
                itemCooldowns.put(material, cooldown);
                getConfig().set("cooldowns." + material.name().toLowerCase(), cooldown);
                saveConfig();
                sender.sendMessage("§aSet cooldown for " + material.name() + " to " + cooldown + " seconds.");
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid item name or cooldown value.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("resetcooldown")) {
            if (!sender.hasPermission("itemcooldowns.reset")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    itemCooldowns.clear();
                    getConfig().set("cooldowns", null);
                    saveConfig();
                    reloadConfig();
                    sender.sendMessage("§aAll cooldown settings have been wiped!");
                    return true;
                }

                try {
                    Material material = Material.valueOf(args[0].toUpperCase());

                    if (itemCooldowns.containsKey(material)) {
                        itemCooldowns.remove(material);
                        getConfig().set("cooldowns." + material.name().toLowerCase(), null);
                        saveConfig();
                        reloadConfig();
                        sender.sendMessage("§aCooldown for " + material.name() + " has been reset.");
                    } else {
                        sender.sendMessage("§eNo cooldown found for " + material.name() + ".");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid item name.");
                }
                return true;
            }

            sender.sendMessage("§eUsage: /resetcooldown <item> OR /resetcooldown all");
            return true;
        }


        if (command.getName().equalsIgnoreCase("getcooldown")) {
            if (!(sender instanceof Player) || !sender.hasPermission("itemcooldowns.get")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("§eUsage: /getcooldown <item>");
                return true;
            }

            try {
                Material material = Material.valueOf(args[0].toUpperCase());
                if (itemCooldowns.containsKey(material)) {
                    sender.sendMessage("§aCooldown for " + material.name() + " is " + itemCooldowns.get(material) + " seconds.");
                } else {
                    sender.sendMessage("§eNo cooldown set for " + material.name() + ".");
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid item name.");
            }
            return true;
        }
        return false;
    }
}

