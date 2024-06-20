// Database.java
package cores.tovy.github.tvaults;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {
    private Logger  logger = Bukkit.getLogger();
    private Connection connection;

    public Database() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/minecraft", "root", "");
            logger.info("Connected to the database.");
        } catch (SQLException e) {
            logger.severe("Could not connect to the database: " + e.getMessage());
        }
    }

    public void saveVault(String playerId, int vaultNumber, Inventory inventory) {
        try {
            String sql = "REPLACE INTO vaults (player_id, vault_number, item_index, item_stack) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    statement.setString(1, playerId);
                    statement.setInt(2, vaultNumber);
                    statement.setInt(3, i);
                    statement.setString(4, serializeItemStack(item));
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (SQLException e) {
            logger.severe("Could not save the vault: " + e.getMessage());
        }
    }

    public Inventory loadVault(String playerId, int vaultNumber, String inventoryName) {
        Inventory inventory = Bukkit.createInventory(null, 27, inventoryName);
        try {
            String sql = "SELECT item_index, item_stack FROM vaults WHERE player_id = ? AND vault_number = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, playerId);
            statement.setInt(2, vaultNumber);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int index = resultSet.getInt("item_index");
                String serializedItem = resultSet.getString("item_stack");
                inventory.setItem(index, deserializeItemStack(serializedItem));
            }
        } catch (SQLException e) {
            logger.severe("Could not load the vault: " + e.getMessage());
        }
        return inventory;
    }

    private String serializeItemStack(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }


    private ItemStack deserializeItemStack(String serializedItem) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new StringReader(serializedItem));
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return config.getItemStack("item");
    }
}
