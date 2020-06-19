package vk;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;

import java.util.concurrent.Executors;

public class VKServer {
    public static VKCore vkCore;

    public static void main(String[] args) throws Exception {
        vkCore = new VKCore();
        System.out.println("Running server...");

        while (true) {
            Thread.sleep(300);
            try {
                Message msg = vkCore.getMessage();
                if (msg != null && (!msg.getText().isEmpty()))
                    Executors.newCachedThreadPool().execute(()->sendMessage(getReplyMessage(msg),/*User id*/msg.getPeerId(),msg.getRandomId()));
            } catch (ClientException e) {
                System.out.println("Повторное соединение..");
                Thread.sleep(10000);
            }
        }
    }

    public static String getReplyMessage(Message msg){
        String userMessage = msg.getText();
        int userId = msg.getPeerId();
        System.out.println("Got message. Text: "+userMessage+" user id: "+userId);
        return "Hello, "+userId;
    }
    
    public static void sendMessage(String message, int userId, int randomId) {
        try { vkCore.vk.messages().send(vkCore.actor).userId(userId).randomId(randomId).message(message).execute(); }
        catch (ApiException | ClientException e){ e.printStackTrace(); }
    }
}
