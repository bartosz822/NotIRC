import static spark.Spark.*;
/**
 * Created by bartek on 13.01.17.
 */
public class JustATest {

    public static void main(String[] args) {
        get("/hello", (req,res) -> "Hello World");
    }

}
