import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by bartek on 1/19/17.
 */
public class MenuChannel extends Channel {
    public MenuChannel(String name) {
        super(name);
    }

    @Override
    public void broadcastMessageOnChannel(String sender, String message) {
        getSessionUsernameMap().keySet().stream().filter(Session::isOpen).forEach(Chat::sendMessagToUser);
    }
}
