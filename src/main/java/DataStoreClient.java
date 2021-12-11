import JMSPublisher.JMSPublisher;
import JMSReceiver.JMSReceiver;
import ProxyServer.ProxyServer;
import Utility.Request;
import Utility.Response;

import javax.jms.JMSException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
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
        this.populateServers();
    }

    private void populateServers() {
        this.servers.add("5000");
        this.servers.add("5010");
        this.servers.add("5020");
        this.servers.add("5030");
        this.servers.add("5040");
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
        AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/src/main/java/Utility/ClientQueueRegistry.txt"), StandardOpenOption.WRITE);
        asyncChannel.write(buffer, asyncChannel.size());
    }

    /**
     * clean up client files when client stops
     */
    private void cleanUpFile() {
        File file = new File(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + this.serverResponseFileName);
        if (file.delete()) {
            System.out.println("File deleted");
        } else {
            System.out.println("File not deleted");
        }
    }

    /**
     * deregister client's MQ when client stops
     * @throws IOException
     */
    private void deregisterQueue() throws IOException {
        File inputFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/Utility/ClientQueueRegistry.txt");
        File tempFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/Utility/ClientQueueRegistryTemp.txt");

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
        logger.info(new Timestamp(System.currentTimeMillis()) + " Sending initial requests");
        DataStoreClient client = new DataStoreClient();
        Random random = new Random();
        String assignedServer = null;
        // client will start to listen to server's messages
        client.startReceivingServerResponse();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String userInput = "";

        // start a separate worker thread to read the files
        ReadWorker readWorker = new ReadWorker(client.serverResponseFileName);
        readWorker.start();

        // continue to receive following requests until user enters "quit"
        // to gracefully stop the client (clean up files etc.) use "quit" to kill client instead of Ctrl + C.
        while (true) {
            try {
                System.out.println("Please enter a valid operation below:");
                System.out.println("----VALID operations includes operation:UPLOAD filename:sample.txt|DOWNLOAD filename|UPDATE filename|DELETE filename----");
                // retrieve user input
                userInput = reader.readLine();

                if (userInput.toLowerCase().equals("quit")) {
                    break;
                }
                // Create a request based on user input.
                Request currRequest = Request.createRequest(userInput);

                // send message to message queue
                client.jmsPublisher.sendMessage(currRequest.toString());
                logger.info(new Timestamp(System.currentTimeMillis()) + " Request sent successfully");

            } catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception " + e);
            }
        }
        // clean up thread, file and deregister queue when stopping the client
        // TODO: thread clean up is not working correctly at this point
        readWorker.interrupt();
        client.closeConnections();
        client.cleanUpFile();
        client.deregisterQueue();
    }
}

class ReadWorker extends Thread {
    String serverResponseFileName;
    public ReadWorker(String serverResponseFileName) {
        this.serverResponseFileName = serverResponseFileName;
    }

    public void run(){
        // TODO: delete this line after done testing!!!!
        System.out.println("MyThread running");
        // retrieve response from message queue
        File responseFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + this.serverResponseFileName);
        // non-stop check whether there are new messages (responses) coming in. If so, print out the message (responses)
        while(true) {
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
                    AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + this.serverResponseFileName), StandardOpenOption.WRITE).truncate(0).close();
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
                Response newResponse = Response.createResponse(response);
                System.out.println("The response received: " + newResponse.toString());
            }
        }
    }
}


