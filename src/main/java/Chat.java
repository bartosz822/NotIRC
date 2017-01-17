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
public class Chat {

    // this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    private static Map<String, Channel> channelNameChannelMap = new ConcurrentHashMap<>();
    private static Map<Session, Channel> sessionChannelMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        channelNameChannelMap.put("chatbot", new ChatBot("chatbot"));
        initRoutes();
        init();
    }

    private static void initRoutes() {

        webSocket("/chat", ChatWebSocketHandler.class);


        before("/chat.html", (request, response) ->
        {
            if (request.cookie("username") == null)
                response.redirect("/");
        });

        StaticFilesConfiguration staticHandler = new StaticFilesConfiguration();
        staticHandler.configure("/public");
        before((request, response) ->
                staticHandler.consume(request.raw(), response.raw())
        );

        get("/chat", (request, response) -> {
            if (request.cookie("username") == null) {
                response.redirect("/");
            } else {
                response.redirect("/chat.html");
            }
            return null;
        });

        get("/channels", (request, response) ->
                new JSONObject().put("channellist", channelNameChannelMap.keySet())
        );


    }

    //Sends a message from one user to all users, along with a list of current usernames

    public static void sendMessagToUser(Session user) {
        try {
            user.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("channel", "false")
                    .put("channellist", Chat.channelNameChannelMap.keySet())
                    .put("userlist", userUsernameMap.values())
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Builds a HTML element with a sender-name, a message, and a timestamp,
    public static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    //    Evaluates message send through websocket
    public static void evalMessage(Session user, String message) {
        if (message.startsWith("/msg")) {
            chanMsg(user, message);
        } else if (message.startsWith("/join")) {
            joinChannel(user, message);
        } else if (message.startsWith("/create")) {
            createChannel(user, message);
        } else if (message.startsWith("/delete")) {
            deleteChannel(user, message);
        } else if (message.startsWith("/leave")) {
            leaveChannel(user, message);
        }
    }

    private static void chanMsg(Session user, String message) {
        sessionChannelMap
                .get(user)
                .broadcastMessageOnChannel(userUsernameMap.
                        get(user), message.substring(4, message.length()));
    }

    private static void leaveChannel(Session user, String message) {
        Channel chan= sessionChannelMap.get(user);
        chan.removeUser(user);
        chan.broadcastMessageOnChannel("Server", "User: " + userUsernameMap.get(user) + " left the channel" );
        sessionChannelMap.remove(user);
    }

    private static void deleteChannel(Session user, String message) {
        String chanelName = getChannelName(message.substring(7, message.length()));
        if (channelNameChannelMap.containsKey(chanelName)) {
            channelNameChannelMap.get(chanelName).removeUser(user);
            channelNameChannelMap.remove(chanelName);
        }
    }

    private static void joinChannel(Session user, String message) {
        String chanelName = getChannelName(message.substring(5, message.length()));
        Channel chan = channelNameChannelMap.get(chanelName);
        chan.addUser(user);
        chan.broadcastMessageOnChannel("Server", "User: " + userUsernameMap.get(user) + " joined the channel" );
        sessionChannelMap.put(user, channelNameChannelMap.get(chanelName));
    }

    private static void createChannel(Session user, String message) {
        String chanelName = getChannelName(message.substring(7, message.length()));
        if (!channelNameChannelMap.containsKey(chanelName))
            channelNameChannelMap.put(chanelName, new Channel(chanelName));
        sendMessagToUser(user);
    }

    private static String getChannelName(String message) {
        if (message.startsWith(" ")) {
            return message.substring(1, message.length());
        } else
            return message;
    }

    public static String getUsername(Session user) {
        return user
                .getUpgradeRequest()
                .getCookies()
                .stream()
                .filter(p -> p.getName().equals("username"))
                .map(HttpCookie::getValue)
                .reduce("",String::concat);
    }
}
