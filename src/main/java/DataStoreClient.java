import JMSPublisher.JMSPublisher;
import ProxyServer.ProxyServer;

import javax.jms.JMSException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class DataStoreClient {
    private final ArrayList<String> initialRequests;
    private final ArrayList<String> servers;
    private final JMSPublisher jmsPublisher;

    public DataStoreClient() throws JMSException {
        this.servers = new ArrayList<>();
        this.initialRequests = new ArrayList<>();
        this.jmsPublisher = new JMSPublisher();
        this.populateServers();
    }

    private void populateServers() {
        this.servers.add("5000");
        this.servers.add("5010");
        this.servers.add("5020");
        this.servers.add("5030");
        this.servers.add("5040");
    }

    public ArrayList<String> getServers () { return this.servers; }

    public static void main(String[] args) throws JMSException {
        Logger logger = Logger.getLogger("DataStoreClient");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Client is up and running!");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Sending initial requests");
        DataStoreClient client = new DataStoreClient();
        Random random = new Random();
        String assignedServer = null;
        // continue to receive following requests until forced to stop
        while (true) {
            try {
                System.out.println("Please enter a valid operation below:");
                System.out.println("----Valid operations include PUT (key value)/GET (key)/DELETE (key), e.g. PUT 2 3, GET 2, DELETE 2----");
                // retrieve user input
                DataInputStream input = new DataInputStream(System.in);
                String userInput = input.readLine();

                // randomly select a server
                assignedServer = client.getServers().get(random.nextInt(client.getServers().size()));
                ProxyServer c = (ProxyServer) Naming.lookup("rmi://localhost:" + assignedServer + "/ProxyServer");
//                ProxyServer c = (ProxyServer) Naming.lookup("rmi://host.docker.internal:" + assignedServer + "/ProxyServer");
                //TODO: send message to message queue
                client.jmsPublisher.sendMessage(userInput);
                logger.info(new Timestamp(System.currentTimeMillis()) + " Request sent successfully");
//                String response = c.operate(userInput);
//                System.out.println( response );
            }
            catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception " + e);
            }
        }
    }
}
