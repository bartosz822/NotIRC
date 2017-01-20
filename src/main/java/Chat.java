import groovy.lang.Singleton;
import groovy.util.MapEntry;
import org.eclipse.jetty.websocket.api.Session;
import org.json.*;
import spark.staticfiles.StaticFilesConfiguration;

import java.net.HttpCookie;
import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static j2html.TagCreator.*;
import static spark.Spark.*;

/**
 * Created by bartek on 1/14/17.
 */
@Singleton
public class Chat {

    Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    private  Map<String, Channel> channelNameChannelMap = new ConcurrentHashMap<>();
    private  Map<Session, Channel> sessionChannelMap = new ConcurrentHashMap<>();
    private  MenuChannel menuChannel = new MenuChannel("Menu", this);





    public void sendMessagToUser(Session user) {
        try {
            user.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("channel", "false")
                    .put("channellist", channelNameChannelMap.keySet())
                    .put("userlist", userUsernameMap.values())
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Chat() {
        channelNameChannelMap.put("chatbot", new ChatBot("chatbot", this));
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    public  String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();

    }


    //    Evaluates message send through websocket
    public void evalMessage(Session user, String message) {
        if (message.startsWith("/msg")) {
            chanMsg(user, message);
        } else if (message.startsWith("/join")) {
            joinChannel(user, message);
        } else if (message.startsWith("/create")) {
            createChannel(user, message);
        } else if (message.startsWith("/leave")) {
            leaveChannel(user);
        } else if (message.startsWith("/clear")) {
            clearChannels();
        }
    }

    private void clearChannels() {
        channelNameChannelMap
                .entrySet()
                .stream()
                .filter(pair -> ! pair.getValue().hasUsers())
                .forEach(this::deleteChannel);
        menuChannel.broadcastMessageOnChannel("Server", "");
    }

    private void chanMsg(Session user, String message) {
        sessionChannelMap
                .get(user)
                .broadcastMessageOnChannel(userUsernameMap.get(user), message.substring(4, message.length()));
    }

    private void leaveChannel(Session user) {
        Channel chan= sessionChannelMap.get(user);
        chan.removeUser(user);
        chan.broadcastMessageOnChannel("Server", "User: " + userUsernameMap.get(user) + " left the channel" );
        sessionChannelMap.remove(user);
        menuChannel.removeUser(user);
        sendMessagToUser(user);
        menuChannel.addUser(user);
        System.out.println("wywalam go");
    }

    private void deleteChannel(Map.Entry<String, Channel> m) {
        channelNameChannelMap.remove(m.getKey());
    }

    private void joinChannel(Session user, String message) {
        String chanelName = getChannelName(message.substring(5, message.length()));
        Channel chan = channelNameChannelMap.get(chanelName);
        chan.addUser(user);
        chan.broadcastMessageOnChannel("Server", "User: " + userUsernameMap.get(user) + " joined the channel" );
        menuChannel.removeUser(user);
        sessionChannelMap.put(user, channelNameChannelMap.get(chanelName));
    }

    private void createChannel(Session user, String message) {
        String chanelName = getChannelName(message.substring(7, message.length()));
        if (!channelNameChannelMap.containsKey(chanelName))
            channelNameChannelMap.put(chanelName, new Channel(chanelName, this));
        sendMessagToUser(user);
        menuChannel.broadcastMessageOnChannel("Server", "");
    }

    private String getChannelName(String message) {
        if (message.startsWith(" ")) {
            return message.substring(1, message.length());
        } else
            return message;
    }

    public String getUsername(Session user) {
        return user
                .getUpgradeRequest()
                .getCookies()
                .stream()
                .filter(p -> p.getName().equals("username"))
                .map(HttpCookie::getValue)
                .reduce("",String::concat);
    }

    public void addNewUser(Session user){
        menuChannel.addUser(user);
    }


}
