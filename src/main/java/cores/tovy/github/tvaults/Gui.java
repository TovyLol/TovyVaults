package cores.tovy.github.tvaults;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Gui implements Listener {
    private Database database = new Database();
    private Map<UUID, Integer> openVaults = new HashMap<>();
    private List<String> blockExceptions;
    private List<String> itemExceptions;
    private Logger logger = Bukkit.getLogger();

    private static final int INVENTORY_SIZE = 27;
    private static final String INVENTORY_TITLE = "Playervaults";

    public Gui(List<String> blockExceptions, List<String> itemExceptions) {
        this.blockExceptions = blockExceptions;
        this.itemExceptions = itemExceptions;
    }

    public void openGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        ItemStack redPane = createItem(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, redPane);
            }
        }

        for (int i = 1; i <= 7; i++) {
            String permission = i + ".tvault.use";
            if (p.hasPermission(permission)) {
                inv.setItem(9 + i, createVaultItem(ChatColor.BLUE + "Vault " + i, ChatColor.GRAY + "Right click to open"));
            } else {
                inv.setItem(9 + i, createVaultItem(ChatColor.RED + "Vault " + i, ChatColor.BOLD.toString() + ChatColor.RED + "LOCKED"));
            }
        }

        p.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createVaultItem(String name, String loreText) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(loreText));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(INVENTORY_TITLE)) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
            int slot = e.getSlot();
            if (slot >= 10 && slot <= 16) {
                int vaultNumber = slot - 9;
                String permission = vaultNumber + ".tvault.use";
                ItemStack clickedItem = e.getCurrentItem();

                if (clickedItem != null) {
                    Material type = clickedItem.getType();
                    String name = clickedItem.getItemMeta().getDisplayName();

                    if (blockExceptions.contains(type.toString()) || itemExceptions.contains(name)) {
                        p.sendMessage(ChatColor.BLUE + "TovyVaults | " + ChatColor.WHITE + "This item is sadly blocked :c");
                        return;
                    }
                }

                if (p.hasPermission(permission) && !openVaults.containsKey(p.getUniqueId())) {
                    openVault(p, vaultNumber);
                    p.sendMessage(ChatColor.BLUE + "TovyVaults | " + ChatColor.WHITE + "Opened Vault " + vaultNumber);
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                } else if (!p.hasPermission(permission)) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to open Vault " + vaultNumber);
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (openVaults.containsKey(p.getUniqueId())) {
            int vaultNumber = openVaults.remove(p.getUniqueId());
            logger.info("Saving vault " + vaultNumber + " for player " + p.getUniqueId());
            database.saveVault(p.getUniqueId().toString(), vaultNumber, e.getInventory());
        }
    }

    public void openVault(Player p, int vaultNumber) {
        String title = p.getName() + getOrdinalSuffix(vaultNumber) + " Vault";
        logger.info("Opening vault " + vaultNumber + " for player " + p.getUniqueId());
        Inventory vault = database.loadVault(p.getUniqueId().toString(), vaultNumber, title);
        openVaults.put(p.getUniqueId(), vaultNumber);
        p.openInventory(vault);
    }

    private String getOrdinalSuffix(int value) {
        int mod100 = value % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return "th";
        }
        switch (value % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
}