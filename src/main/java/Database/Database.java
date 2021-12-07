//package Database;
//
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.client.MongoClients;
//import io.github.cdimascio.dotenv.Dotenv;
//import org.bson.Document;
//import com.mongodb.client.MongoCollection;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Database {
//    public static MongoClient mongoClient;
//    public static void main(String[] args) throws Exception {
//        try {
//            Dotenv dotenv = Dotenv.load();
//            ConnectionString connectionString = new ConnectionString("mongodb+srv://6650final:" + dotenv.get("DBPASSWORD") + "@cluster0.kuupt.mongodb.net/defaultDatabase?retryWrites=true&w=majority");
//            MongoClientSettings settings = MongoClientSettings.builder()
//                    .applyConnectionString(connectionString)
//                    .build();
//            mongoClient = MongoClients.create(settings);
//            MongoDatabase database = mongoClient.getDatabase("defaultDatabase");
//            System.out.println("Connect to database successfully");
//
//            System.out.println("list all the collections in database：");
//            for (String name : database.listCollectionNames()) {
//                System.out.println(name);
//            }
//        } catch (Exception e) {
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//        }
//
//        storeTXT("defaultDatabase", "testCollection", "/Users/april/Desktop/maven.txt");
//    }
//
//    public static void storeTXT(String MongoDBName, String MongoDBCollection, String filePath) throws Exception {
//        if (!filePath.endsWith("txt")) {
//            throw new Exception("file is not txt");
//        }
//        MongoDatabase db = mongoClient.getDatabase(MongoDBName);
//        MongoCollection<Document> collection = db.getCollection(MongoDBCollection);
//
//        File file = new File(filePath);
//        BufferedReader reader;
//        try {
//            reader = new BufferedReader(new FileReader(file));
//            String tempString;
//            String wholeString = "";
//            while ((tempString = reader.readLine()) != null) {
//                wholeString += tempString;
//            }
//            reader.close();
//            Document document = new Document();
//            document.append("txt", wholeString);
//            collection.insertOne(document);
//            System.out.println("read txt successfully!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //TODO: need a delete method
//}
