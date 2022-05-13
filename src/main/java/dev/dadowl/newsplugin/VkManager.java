package dev.dadowl.newsplugin;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.enums.WallFilter;
import com.vk.api.sdk.objects.wall.Wallpost;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import org.bukkit.Bukkit;

public class VkManager {

    public static void update(){
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);

        ServiceActor actor = new ServiceActor(NewsPlugin.VK_APP, NewsPlugin.VK_SECRET);

        GetResponse getResponse = null;
        try {
            getResponse = vk.wall().get(actor)
                    .ownerId(NewsPlugin.VK_GROUP)
                    .count(1)
                    .filter(WallFilter.OWNER)
                    .execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
            return;
        }

        for (WallpostFull item : getResponse.getItems()) {
            if (item.getText().isEmpty()){
                for (Wallpost wallpost : item.getCopyHistory()) {
                    NewsPlugin.getInstance().getLogger().info(wallpost.getText());
                }
            }
        }

//        Bukkit.getScheduler().runTaskAsynchronously(NewsPlugin.getInstance(), () -> {
//
//        });
    }

}
