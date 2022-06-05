package dev.dadowl.newsplugin;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.GetFilter;
import com.vk.api.sdk.objects.wall.Wallpost;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class VkManager {

    private final NewsPlugin plugin;
    private final int VK_APP;
    private final String VK_SECRET;
    private final int VK_GROUP;

    private String text = "";

    public VkManager(NewsPlugin newsPlugin, ConfigurationSection conf) {
        this.plugin = newsPlugin;
        VK_APP = conf.getInt("app_id");
        VK_SECRET = conf.getString("secret_key");
        VK_GROUP = conf.getInt("group_id");
    }

    public void update(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TransportClient transportClient = new HttpTransportClient();
            VkApiClient vk = new VkApiClient(transportClient);

            ServiceActor actor = new ServiceActor(VK_APP, VK_SECRET);

            GetResponse getResponse;
            try {
                getResponse = vk.wall().get(actor)
                    .ownerId(VK_GROUP)
                    .count(1)
                    .filter(GetFilter.OWNER)
                .execute();
            } catch (ApiException | ClientException e) {
                e.printStackTrace();
                return;
            }

            for (WallpostFull item : getResponse.getItems()) {
                if (item.getText().isEmpty()){
                    for (Wallpost wallpost : item.getCopyHistory()) {
                        text = wallpost.getText();
                    }
                } else {
                    text = item.getText();
                }
            }
        });
    }

    public ItemStack getNewsBook(){
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

            if (q == lines-3 || q == lines-2 || q == lines-1){
                s = s.replaceFirst("\n\n", "\n");
            }

            page.append(s);

            q++;
        }

        if (page.length() > 0){
            pages.add(Component.text(page.toString()));
        }

        meta.addPages(pages.toArray(new Component[0]));

        item.setItemMeta(meta);
        return item;
    }

}
