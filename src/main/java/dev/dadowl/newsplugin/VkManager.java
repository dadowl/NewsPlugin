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
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class VkManager {

    private final NewsPlugin plugin;
    private final int VK_APP;
    private final String VK_SECRET;
    private final int VK_GROUP;

    public VkManager(NewsPlugin newsPlugin, ConfigurationSection conf) {
        this.plugin = newsPlugin;
        VK_APP = conf.getInt("app_id");
        VK_SECRET = conf.getString("secret_key");
        VK_GROUP = conf.getInt("group_id");
    }

    public void update(){
        plugin.getLogger().info(VK_APP + " " + VK_SECRET + " " + VK_GROUP);
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
                        plugin.getLogger().info(wallpost.getText());
                    }
                } else {
                    plugin.getLogger().info(item.getText());
                }
            }
        });
    }

}
