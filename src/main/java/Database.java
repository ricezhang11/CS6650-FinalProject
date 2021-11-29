import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;

public class Database {
    public static void main(String[] args){
        try {
            Dotenv dotenv = Dotenv.load();
            ConnectionString connectionString = new ConnectionString("mongodb+srv://6650final:" + dotenv.get("DBPASSWORD") + "@cluster0.kuupt.mongodb.net/defaultDatabase?retryWrites=true&w=majority");
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("defaultDatabase");
            System.out.println("Connect to database successfully");

            System.out.println("list all the collections in database：");
            for (String name : database.listCollectionNames()) {
                System.out.println(name);
            }
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
}