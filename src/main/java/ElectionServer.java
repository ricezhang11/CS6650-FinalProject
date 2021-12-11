import ProxyServer.ProxyServer;

import javax.jms.JMSException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * server to perform election
 */
public class ElectionServer {
    Logger logger = Logger.getLogger("ElectionServer");
    private final ArrayList<String> allPorts = new ArrayList<>();
    public String leader;

    public ElectionServer() {
        this.initializePorts();
    }

    private void initializePorts() {
        this.allPorts.add("5000");
        this.allPorts.add("5010");
        this.allPorts.add("5020");
        this.allPorts.add("5030");
        this.allPorts.add("5040");
    }

    public void electLeader() throws RemoteException {
        Collections.sort(this.allPorts);
        for(int i = this.allPorts.size() - 1; i > 0; i--) {
            if (rmiIsHealth(this.allPorts.get(i))) {
                this.leader = this.allPorts.get(i);
                logger.info(new Timestamp(System.currentTimeMillis()) + " Proposer with port number "
                        + this.allPorts.get(i) + " is elected as leader");
                break;
            }
        }
    }

    public void startLeaderPaxos() throws IOException, NotBoundException, InterruptedException, JMSException {
        try {
            // find the leader proxy server
            ProxyServer c = (ProxyServer) Naming.lookup("rmi://localhost:" + this.leader + "/ProxyServer");
            // ask the leader to retrieve messages and start paxos
            c.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean rmiIsHealth(String portNumber) {
        try
        {
            ProxyServer c = (ProxyServer) Naming.lookup("rmi://localhost:" + portNumber + "/ProxyServer");
            return c.isHealthy();
        }
        catch (Exception e)
        {
            logger.warning("Proxy server error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String args[]) throws IOException, NotBoundException, InterruptedException, JMSException {
        // can use Timer task to elect periodically
        ElectionServer electionServer = new ElectionServer();
        // do the election
        electionServer.electLeader();
        // ask leader to perform paxos
        electionServer.startLeaderPaxos();
    }
}
