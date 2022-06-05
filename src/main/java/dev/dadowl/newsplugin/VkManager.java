package dev.dadowl.newsplugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

public class VkManager {

    private final NewsPlugin plugin;
    private final String VK_SECRET;
    private final int VK_GROUP;

    private Connection connection;
    private final String connectionUrl = "jdbc:sqlite:" + NewsPlugin.getInstance().getDataFolder() + "/database.db";

    private String text = "";
    private int lastPostId = 0;

    public ItemStack newsBook = null;

    public VkManager(NewsPlugin newsPlugin, ConfigurationSection conf) {
        this.plugin = newsPlugin;
        VK_SECRET = conf.getString("secret_key");
        VK_GROUP = conf.getInt("group_id");
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(connectionUrl);
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `VKposts` (`postId` integer PRIMARY KEY, `postText` text);"
            );
            ps.executeUpdate();
            connection.commit();

            ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `PlayersSeenPost` (`uuid` varchar(36) PRIMARY KEY UNIQUE, `postId` integer);"
            );
            ps.executeUpdate();
            connection.commit();

            setLastPostIdFromDB();
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось подключиться к SQLite.");
            e.printStackTrace();
            return;
        }
    }

    public void update() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String requestUrl = "https://api.vk.com/method/wall.get?owner_id="+VK_GROUP+"&count=1" +
                    "&filter=owner&access_token="+VK_SECRET+"&v=5.131";
            try{
                URL obj = new URL(requestUrl);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestProperty("Content-Type", "charset=utf-8");
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                in.close();

                JsonObject json = new Gson().fromJson(response, JsonObject.class);
                JsonArray items = json.get("response").getAsJsonObject().get("items").getAsJsonArray();
                if (!items.isEmpty()) {
                    plugin.getLogger().info("Посты получены.");
                    JsonObject post = items.get(0).getAsJsonObject();
                    if (post.get("id").getAsInt() != lastPostId){
                        String postText = post.get("text").getAsString();
                        if (postText.isEmpty()){
                            postText = post.get("copy_history").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                        }
                        text = postText;
                        lastPostId = post.get("id").getAsInt();
                        addPost(lastPostId, text);
                        plugin.getLogger().info("В группе вышел новый пост, обновляем его в плагине.");
                        newsBook = getNewsBook();
                    } else {
                        plugin.getLogger().info("Новые посты отсутствуют.");
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }, 0, 20 * 3600);
    }

    public ItemStack getNewsBook() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();

        meta.setTitle("§6Последние новости");
        meta.setAuthor("DadOwl");

        ArrayList<Component> pages = new ArrayList<>();
        ArrayList<String> pagesText = Utils.splitText(text, 19);
        int lines = 14;

        StringBuilder page = new StringBuilder();
        int q = 0;
        for (String s : pagesText) {
            if (q == lines) {
                pages.add(Component.text(page.toString()));
                page = new StringBuilder();
                q = 0;
            }

            if (q == lines - 3 || q == lines - 2) {
                s = s.replaceFirst("\n\n", "\n");
            }
            if (q == lines - 1){
                s = s.replaceFirst("\n\n", "");
            }

            page.append(s);

            q++;
        }

        if (page.length() > 0) {
            pages.add(Component.text(page.toString()));
        }

        meta.addPages(pages.toArray(new Component[0]));

        item.setItemMeta(meta);
        return item;
    }

    private void setLastPostIdFromDB() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM VKposts ORDER BY postId desc LIMIT 1;"
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                lastPostId = result.getInt("postId");
                text = result.getString("postText");
                newsBook = getNewsBook();
                return;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при получении последнего поста.");
            e.printStackTrace();
        }

        return;
    }

    private void addPost(int id, String text) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO `VKposts` VALUES (" + id + ",'" + text + "');"
            );
            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при сохранении поста.");
            e.printStackTrace();
        }
    }

    private Boolean isPlayerSeenPost(UUID uuid, int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM PlayersSeenPost WHERE uuid = '" + uuid.toString() + "' ORDER BY postId desc LIMIT 1;"
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                if (result.getInt("postId") == id) return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при получении последнего поста для игрока.");
            e.printStackTrace();
        }

        return false;
    }

    public Boolean isPlayerSeenLastPost(UUID uuid) {
        return isPlayerSeenPost(uuid, lastPostId);
    }

    private void setPlayerSeen(UUID uuid, int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO PlayersSeenPost (uuid, postId) " +
                            "VALUES('" + uuid.toString() + "', " + id + ")"
            );
            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при сохранении поста для игрока.");
            e.printStackTrace();
        }
    }

    public void setPlayerSeenLastPost(UUID uuid) {
        setPlayerSeen(uuid, lastPostId);
    }
}
