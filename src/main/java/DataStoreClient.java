import JMSPublisher.JMSPublisher;
import JMSReceiver.JMSReceiver;
import Utility.Request;
import Utility.Response;

import javax.jms.JMSException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Logger;

public class DataStoreClient {
    private final ArrayList<String> servers;
    private final JMSPublisher jmsPublisher;
    private final JMSReceiver jmsReceiver;
    private final String clientCreationTimestamp = new Timestamp(System.currentTimeMillis()).toString();
    private final String clientQueueName = "cs6650-server response queue" + clientCreationTimestamp;
    private final String serverResponseFileName = "ServerResponse" + clientCreationTimestamp + ".txt";
    private final String clientFolderName = ("Client" + clientCreationTimestamp).replace(":", "-");

    /**
     * initialize message publisher and receiver, register client message queue with JMS
     * @throws JMSException
     * @throws IOException
     */
    public DataStoreClient() throws JMSException, IOException {
        this.servers = new ArrayList<>();
        // client publishes messages to "cs6650-client request queue"
        this.jmsPublisher = new JMSPublisher("cs6650-client request queue");
        this.jmsReceiver = new JMSReceiver(clientQueueName, serverResponseFileName);
        // register client queue names so that server can retrieve them
        this.registerClientQueueName(this.clientQueueName);
        this.createClientFolder();
        this.populateServers();
    }

    private void populateServers() {
        this.servers.add("5000");
        this.servers.add("5010");
        this.servers.add("5020");
        this.servers.add("5030");
        this.servers.add("5040");
    }

    private void createClientFolder() {
        if (new File(System.getProperty("user.dir") + "/" + this.clientFolderName).mkdir()) {
            System.out.println("created folder");
        } else {
            System.out.println("folder already exists");
        }
    }
    /**
     * tell client's MQ to start receiving server messages
     * @throws JMSException
     */
    private void startReceivingServerResponse() throws JMSException {
        this.jmsReceiver.startReceiving();
    }

    /**
     * register client's MQ so that server can send messages to it
     * @param queueName
     * @throws IOException
     */
    private void registerClientQueueName(String queueName) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // write each client queue in a new line
        String toWrite = queueName + System.lineSeparator();
        buffer.put(toWrite.getBytes());
        buffer.flip();
//        AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/src/main/java/Utility/ClientQueueRegistry.txt"), StandardOpenOption.WRITE);
        AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/Utility/ClientQueueRegistry.txt"), StandardOpenOption.WRITE);

        asyncChannel.write(buffer, asyncChannel.size());
    }

    /**
     * clean up client files when client stops
     */
    private void cleanUpFile() {
//        File file = new File(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + this.serverResponseFileName);
        File file = new File(System.getProperty("user.dir") + "/JMSReceiver/" + this.serverResponseFileName);

        if (file.delete()) {
            System.out.println("File deleted");
        } else {
            System.out.println("File not deleted");
        }
    }

    private void cleanUpFolder(File f) {
        File[] contents = f.listFiles();
        if (contents != null) {
            for (File content : contents) {
                cleanUpFolder(content);
            }
        }
        f.delete();
    }

    /**
     * deregister client's MQ when client stops
     * @throws IOException
     */
    private void deregisterQueue() throws IOException {
//        File inputFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/Utility/ClientQueueRegistry.txt");
//        File tempFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/Utility/ClientQueueRegistryTemp.txt");

        File inputFile = new File(System.getProperty("user.dir") + "/Utility/ClientQueueRegistry.txt");
        File tempFile = new File(System.getProperty("user.dir") + "/Utility/ClientQueueRegistryTemp.txt");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String lineToRemove = this.clientQueueName;
        String currentLine;

        while((currentLine = reader.readLine()) != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if(trimmedLine.equals(lineToRemove)) continue;
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        reader.close();
        tempFile.renameTo(inputFile);
    }

    /**
     * clean up publisher and receiver connection
     * @throws JMSException
     */
    private void closeConnections() throws JMSException {
        this.jmsReceiver.closeConnection();
        this.jmsPublisher.closeConnection();
    }


    public ArrayList<String> getServers () { return this.servers; }

    /**
     * main method to send and receive messages to server
     * @param args
     * @throws JMSException
     * @throws IOException
     */
    public static void main(String[] args) throws JMSException, IOException, InterruptedException {
        Logger logger = Logger.getLogger("DataStoreClient");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Client is up and running!");
        DataStoreClient client = new DataStoreClient();
        Random random = new Random();
        String assignedServer = null;
        // client will start to listen to server's messages
        client.startReceivingServerResponse();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String userInput = "";

        // start a separate worker thread to read the files
        ReadAndUpdateWorker readAndUpdateWorker = new ReadAndUpdateWorker(client.serverResponseFileName, client.clientFolderName);
        readAndUpdateWorker.start();

        // continue to receive following requests until user enters "quit"
        // to gracefully stop the client (clean up files etc.) use "quit" to kill client instead of Ctrl + C.
        while (true) {
            try {
                System.out.println("Please enter a valid operation below:");
                System.out.println("----Examples: operation:UPLOAD, filename:sample.txt|operation:DOWNLOAD, filename:sample.txt|operation:UPDATE, filename:sample.txt|operation:DELETE, filename:sample.txt----");
                // retrieve user input
                userInput = reader.readLine();

                if (userInput.toLowerCase().equals("quit")) {
                    break;
                }
                // Create a request based on user input.
                Request currRequest = Request.createRequest(userInput, client.clientFolderName);

                // send message to message queue
                client.jmsPublisher.sendMessage(currRequest.toString());
                logger.info(new Timestamp(System.currentTimeMillis()) + " Request sent successfully");

            } catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception " + e);
            }
        }
        // clean up thread, file and deregister queue when stopping the client
        // TODO: thread clean up is not working correctly at this point
        readAndUpdateWorker.interrupt();
        client.closeConnections();
        client.cleanUpFile();
        client.cleanUpFolder(new File(System.getProperty("user.dir") + "/" + client.clientFolderName));
        client.deregisterQueue();
    }
}

class ReadAndUpdateWorker extends Thread {
    String serverResponseFileName;
    String clientFolderName;

    public ReadAndUpdateWorker(String serverResponseFileName, String clientFolderName) {
        this.serverResponseFileName = serverResponseFileName;
        this.clientFolderName = clientFolderName;
    }

    public void run() {
        // retrieve response from message queue
//        File responseFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + this.serverResponseFileName);
        File responseFile = new File(System.getProperty("user.dir") + "/JMSReceiver/" + this.serverResponseFileName);

        // non-stop check whether there are new messages (responses) coming in. If so, print out the message (responses)
        while (true) {
            Scanner fileReader = null;
            try {
                fileReader = new Scanner(responseFile);
            } catch (FileNotFoundException e) {
                break;
            }
            String output = null;
            if (fileReader.hasNext()) {
                output = fileReader.nextLine();
                try {
//                    AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + this.serverResponseFileName), StandardOpenOption.WRITE).truncate(0).close();
                    AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/JMSReceiver/" + this.serverResponseFileName), StandardOpenOption.WRITE).truncate(0).close();

                } catch (IOException e) {
                    break;
                }
            }
            fileReader.close();
            List<String> responses = new ArrayList<>();
            if (output != null) {
                responses = Arrays.asList(output.split(";"));
            }

            for (String response : responses) {
//                System.out.println("hello!" + response);
                Response newResponse = Response.createResponse(response);
                System.out.println(newResponse.toString());
                System.out.println("----Automatically updating your local files....----");
                if (newResponse.getStatus() == Response.Status.SUCCEED) {
                    if (newResponse.getOperation().equals(Request.Operation.UPLOAD.toString())) {
                        try {
                            new File(System.getProperty("user.dir") + "/" + this.clientFolderName + "/" + newResponse.getFilename()).createNewFile();
                            File file = new File(System.getProperty("user.dir") + "/" + this.clientFolderName + "/" + newResponse.getFilename());

                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            writer.write(newResponse.getContent());
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (newResponse.getOperation().equals(Request.Operation.UPDATE.toString()) || newResponse.getOperation().equals(Request.Operation.DOWNLOAD.toString())) {
                        File file = new File(System.getProperty("user.dir") + "/" + this.clientFolderName + "/" + newResponse.getFilename());
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            writer.write(newResponse.getContent());
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (newResponse.getOperation().equals(Request.Operation.DELETE.toString())) {
                        new File(System.getProperty("user.dir") + "/" + this.clientFolderName + "/" + newResponse.getFilename()).delete();
                    }
                }
                System.out.println("----Update finished :)----");
                System.out.println("Please enter a valid operation below:");
                System.out.println("----Examples: operation:UPLOAD, filename:sample.txt|operation:DOWNLOAD, filename:sample.txt|operation:UPDATE, filename:sample.txt|operation:DELETE, filename:sample.txt----");
            }
        }
    }
}


