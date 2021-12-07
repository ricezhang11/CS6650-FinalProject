package JMSPublisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class JMSPublisher {
    //TODO: define a publisher, initiate connection etc.
    public JMSPublisher() {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection con = null;
        try {

        } catch(Exception e) {

        }
    }

    //TODO: here I want another method to call when we want to send messages
}
