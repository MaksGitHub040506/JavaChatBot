package vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;

import java.util.List;

public class VKCore {
    public VkApiClient vk;
    public GroupActor actor;
    private static int ts; // Временная метка
    private static int maxMsgId = -1;

    private static final String ACCESS_TOKEN = "" /*КЛЮЧ ДОСТУПА*/;
    private static final int GROUP_ID = 0 /*ID ГРУППЫ*/;

    public VKCore() throws ClientException, ApiException {
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        actor = new GroupActor(GROUP_ID, ACCESS_TOKEN);
        ts = vk.messages().getLongPollServer(actor).execute().getTs();
    }

    public Message getMessage() throws ClientException, ApiException {
        MessagesGetLongPollHistoryQuery eventsQuery = vk.messages()
                .getLongPollHistory(actor)
                .ts(ts);

        if (maxMsgId > 0){
            eventsQuery.maxMsgId(maxMsgId);
        }
        List<Message> messages = eventsQuery
                .execute()
                .getMessages()
                .getItems();

        if (!messages.isEmpty()){
            try {
                ts =  vk.messages()
                        .getLongPollServer(actor)
                        .execute()
                        .getTs();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
        if (!messages.isEmpty() && !messages.get(0).isOut()) {

            /*
            messageId - максимально полученный ID, нужен, чтобы не было ошибки 10 internal server error,
            который является ограничением в API VK. В случае, если ts слишком старый (больше суток),
            а max_msg_id не передан, метод может вернуть ошибку 10 (Internal server error).
            */
            int messageId = messages.get(0).getId();
            if (messageId > maxMsgId){
                maxMsgId = messageId;
            }

            return messages.get(0);
        }
        return null;
    }

}
