import static spark.Spark.*;
//
public class Main {
    public static void main(String[] args) {

        get("/hello", (req, res)->"Hello, world");

        get("/hello/:name", (req,res)->{
            return "Hello, "+ req.params(":name");
        });
    }
}