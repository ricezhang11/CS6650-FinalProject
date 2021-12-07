package JMSPublisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class JMSPublisher {
    Queue queue;
    MessageProducer messageProducer;
    ConnectionFactory connectionFactory;
    Connection con;
    Session session;
    //TODO: define a publisher, initiate connection etc.
    public JMSPublisher() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            con = connectionFactory.createConnection();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue("message queue");
            messageProducer = session.createProducer(queue);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //TODO: here I want another method to call when we want to send messages
    public void sendMessage(String message) throws JMSException {
        try {
            Message msg = session.createTextMessage(message);
            messageProducer.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
    //TODO: close connection
    public void closeConnection() throws JMSException {
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
