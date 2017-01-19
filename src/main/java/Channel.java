import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bartek on 1/14/17.
 */
public class Channel {

    private Map<Session, String> sessionUsernameMap = new ConcurrentHashMap<>();
    private String name;


    public Channel(String name) {
        this.name = name;
    }

    public Map<Session, String> getSessionUsernameMap() {
        return sessionUsernameMap;
    }

    public void addUser(Session user){
        sessionUsernameMap.put(user, Chat.getUsername(user));
    }

    public void removeUser(Session user){
        sessionUsernameMap.remove(user);
    }

    public String getName() {
        return name;
    }

    public void broadcastMessageOnChannel(String sender, String message) {
        sessionUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("channel", "true")
                        .put("userMessage", Chat.createHtmlMessageFromSender(sender, message))
                        .put("userlist", sessionUsernameMap.values())
                ));
            } catch (JSONException | IOException e) {
                System.out.println(e.getMessage());
            }
     });
    }

    public boolean hasUsers(){
        return sessionUsernameMap.keySet().stream().map(Session::isOpen).reduce(Boolean::logicalAnd).orElse(Boolean.FALSE);
    }


}
