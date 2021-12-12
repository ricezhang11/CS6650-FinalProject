package Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

import java.io.*;

public class Database {
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
    public MongoClient mongoClient;
    public String mongoDBName;
    public String mongoDBCollection;
    public Database(String MongoDBName, String MongoDBCollection) {
//        Dotenv dotenv = Dotenv.load();
        System.out.println("please put your ip address in Mongodb Altas server security, or you will get a timeout error");
        ConnectionString connectionString = new ConnectionString("mongodb+srv://6650final:6650password@cluster0.kuupt.mongodb.net/defaultDatabase?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.mongoDBName = MongoDBName;
        this.mongoDBCollection = MongoDBCollection;
    }

    public String upload(String filePath) throws Exception {
        if (!filePath.endsWith("txt")) {
            throw new Exception("file is not txt");
        }
        MongoDatabase db = mongoClient.getDatabase(this.mongoDBName);
        MongoCollection<Document> collection = db.getCollection(this.mongoDBCollection);

        File file = new File(filePath);
        String fileName = file.getName();
        BufferedReader reader;
        String wholeString = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                wholeString += tempString;
            }
            reader.close();
            Document document = new Document();
            document.append("fileName", fileName);
            document.append("txt", wholeString);
            collection.insertOne(document);
            System.out.printf("[read] txt from %s successfully:%n %s", fileName, wholeString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wholeString;
    }

    public void delete(String filePath) throws Exception {
        if (!filePath.endsWith("txt")) {
            throw new Exception("file is not txt");
        }
        MongoDatabase db = mongoClient.getDatabase(this.mongoDBName);
        MongoCollection<Document> collection = db.getCollection(this.mongoDBCollection);
        File file = new File(filePath);
        String fileName = file.getName();
        collection.deleteOne(Filters.eq("fileName", fileName));
        System.out.printf("[delete] file %s successfully!", fileName);
    }

    public String update(String filePath) throws Exception {
        if (!filePath.endsWith("txt")) {
            throw new Exception("file is not txt");
        }
        MongoDatabase db = mongoClient.getDatabase(this.mongoDBName);
        MongoCollection<Document> collection = db.getCollection(this.mongoDBCollection);

        File file = new File(filePath);
        String fileName = file.getName();
        BufferedReader reader;
        String wholeString = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                wholeString += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        collection.updateOne(Filters.eq("fileName", fileName), Updates.set("txt", wholeString));
        System.out.printf("[update] file %s successfully! with contents %s", fileName, wholeString);
        return wholeString;
    }

    public String download(String fileName) throws Exception {
        if (!fileName.endsWith("txt")) {
            throw new Exception("file is not txt");
        }
        MongoDatabase db = mongoClient.getDatabase(this.mongoDBName);
        MongoCollection<Document> collection = db.getCollection(this.mongoDBCollection);
//        File file = new File(filePath);
//        String fileName = file.getName();
        Document document = collection.find(Filters.eq("fileName", fileName)).first();
        String str = document.get("txt").toString();
        System.out.printf("[download] file %s successfully:%n %s", fileName, str);
        return str;
    }
}
