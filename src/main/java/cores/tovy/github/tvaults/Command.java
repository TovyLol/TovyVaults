package cores.tovy.github.tvaults;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {
    public Gui gui;
    public Main pl;

    public Command(Gui gui, Main pl) {
        this.gui = gui;
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command.");
            return true;
        }

        Player p = (Player) commandSender;
        if (command.getName().equals("pv")) {
            if (args.length == 0) {
                gui.openGui(p);
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (p.hasPermission("tvault.admin")) {
                    Main.getInstance().reloadPluginConfig();
                    sendReloadMessages(p);
                } else {
                    p.sendMessage(ChatColor.BLUE + "TovyVaults | " + ChatColor.WHITE + "Error whilst executing config reload, retry.");
                }
                return true;
            }
            if (p.hasPermission("tvault.admin")) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    p.sendMessage(ChatColor.BOLD.RED + "Error | " + ChatColor.WHITE + "This Player is not online or does not have a vault");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return true;
                }
                if (args.length != 2 || !isInteger(args[1])) {
                    p.sendMessage(ChatColor.BOLD.RED + "Error | " + ChatColor.WHITE + "This is not a valid vault");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return true;
                }
                int vaultNumber = Integer.parseInt(args[1]);
                gui.openVault(target, vaultNumber);
                p.openInventory(target.getOpenInventory().getTopInventory());
                return true;
            }
        }
        return false;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendReloadMessages(Player p) {
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            p.sendMessage(ChatColor.WHITE + "*Crack*");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }, 0L);
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            p.sendMessage(ChatColor.WHITE + "*Click*");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }, 20L);
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            p.sendMessage(ChatColor.RED + "*OUCH!*");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }, 40L);
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            p.sendMessage(ChatColor.WHITE + "*Tick*");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }, 60L);
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            p.sendMessage(ChatColor.WHITE + "Successfully reloaded the plugin config :3 -Love from tovy!");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
        }, 80L);
    }
}