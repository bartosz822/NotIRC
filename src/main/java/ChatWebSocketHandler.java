import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.net.HttpCookie;


@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;

    private Chat chat = new Chat();

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = chat.getUsername(user);
        chat.userUsernameMap.put(user, username);
        chat.addNewUser(user);
        chat.sendMessagToUser(user);
    }



    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = chat.userUsernameMap.get(user);
        chat.userUsernameMap.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        System.out.println(message);
        chat.evalMessage(user, message);
    }

}