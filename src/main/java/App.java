import spark.staticfiles.StaticFilesConfiguration;

import static spark.Spark.*;

/**
 * Created by bartek on 20.01.17.
 */
public class App {
    public static void main(String[] args) {
        initRoutes();
    }

    public static void initRoutes() {
        port(getHerokuAssignedPort());

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

    }

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set
    }
}
