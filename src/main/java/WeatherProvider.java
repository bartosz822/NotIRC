import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by bartek on 20.01.17.
 */
public class WeatherProvider {

    private static String apiLink = "http://api.openweathermap.org/" +
    "data/2.5/weather?q=Krakow,uk&appid=bf5a5bd346890c81c4bea71ed5bc62b5";

    public String getCurrentWeather(){
        try (InputStream in =
                     new URL(apiLink).openStream()
        ) {
            JSONObject weather = new JSONObject(IOUtils.toString(in, "UTF-8"));
            String temp = weather.getJSONObject("main").getString("temp");
            String pressure = weather.getJSONObject("main").getString("pressure");
            String humidity = weather.getJSONObject("main").getString("humidity");
            return "Temperatura: " + temp + " Ciśnienie: " + pressure + " Wilgotność powietrza " + humidity;
        }catch (JSONException e){
            System.out.println("Api prawdopodobnie nie zadziałało");
            return "Nie udało się sprawdzić pogody";
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return "Nie udało się sprawdzić pogody";
        }
    }


}
