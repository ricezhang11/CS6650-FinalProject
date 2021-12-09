import JMSPublisher.JMSPublisher;
import JMSReceiver.JMSReceiver;
import ProxyServer.ProxyServer;

import javax.jms.JMSException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
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
        AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/Utility/ClientQueueRegistry.txt"), StandardOpenOption.WRITE);
        asyncChannel.write(buffer, asyncChannel.size());
    }

    /**
     * clean up client files when client stops
     */
    private void cleanUpFile() {
        File file = new File(System.getProperty("user.dir") + "/JMSReceiver/" + this.serverResponseFileName);
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
    public static void main(String[] args) throws JMSException, IOException {
        Logger logger = Logger.getLogger("DataStoreClient");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Client is up and running!");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Sending initial requests");
        DataStoreClient client = new DataStoreClient();
        Random random = new Random();
        String assignedServer = null;
        // client will start to listen to server's messages
        client.startReceivingServerResponse();

        DataInputStream input = new DataInputStream(System.in);
        String userInput = "";

        // continue to receive following requests until user enters "quit"
        // to gracefully stop the client (clean up files etc.) use "quit" to kill client instead of Ctrl + C.
        while (true) {
            try {
                System.out.println("Please enter a valid operation below:");
                System.out.println("----Valid operations include PUT (key value)/GET (key)/DELETE (key), e.g. PUT 2 3, GET 2, DELETE 2----");
                // retrieve user input
                userInput = input.readLine();
                //
                if (userInput.toLowerCase().equals("quit")) {
                    break;
                }
                // send message to message queue, not to any server
                client.jmsPublisher.sendMessage(userInput);
                logger.info(new Timestamp(System.currentTimeMillis()) + " Request sent successfully");
            } catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception " + e);
            }
        }
        // clean up file and deregister queue when stopping the client
        client.closeConnections();
        client.cleanUpFile();
        client.deregisterQueue();
    }
}
