import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Math.floor;

/**
 * Created by bartek on 1/17/17.
 */
public class ChatBot extends Channel {

    private WeatherProvider weatherProvider;

    public ChatBot(String name, Chat chat) {
        super(name, chat);
        this.weatherProvider = new WeatherProvider();
    }

    private static final String weatherApiUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20item"+
            ".condition%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places"+
            "(1)+%20where%20text%3D%22Cracow%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";


    @Override
    public void broadcastMessageOnChannel(String sender, String message) {
        super.broadcastMessageOnChannel(sender, message);
        evalMsg(message);
    }


    @Override
    public boolean hasUsers() {
        return true;
    }


    private void evalMsg(String msg) {
        switch (msg) {
            case "Która godzina?":
                respondWithTime();
                break;
            case"Jaki dziś dzień tygodnia?":
                respondWithDay();
                break;
            case "Jaka jest pogoda w Krakowie?":
                respondWithWeather();
                break;
        }
    }

    private void respondWithWeather() {
        respondWithString("Już sprawdzam");
        respondWithString(weatherProvider.getCurrentWeather());
    }

    private void respondWithDay() {
        String day = processToDayInPolish(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        respondWithString("Jest: " + day);
    }

    private String processToDayInPolish(int i) {
        switch (i){
            case Calendar.MONDAY:
                return "Poniedziałek";
            case Calendar.TUESDAY:
                return "Wtorek";
            case Calendar.WEDNESDAY:
                return "Środa";
            case Calendar.THURSDAY:
                return "Czwartek";
            case Calendar.FRIDAY:
                return "Piąte";
            case Calendar.SATURDAY:
                return "Sobota";
            case Calendar.SUNDAY:
                return "Niedziela";
            default:
                return "";
        }
    }

    private void respondWithTime() {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        respondWithString(timeStamp);
    }

    private void respondWithString(String message){
        super.broadcastMessageOnChannel("ChatBot", message);
    }


}
