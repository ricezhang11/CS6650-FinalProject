package JMSReceiver;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;


public class JMSReceiver {
    Queue queue;
    MessageConsumer messageConsumer;
    ConnectionFactory connectionFactory;
    Connection con;
    Session session;
    MessageListener messageListener;

    public JMSReceiver() throws JMSException {
        try {
            connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616"); // ActiveMQ-specific
            con = connectionFactory.createConnection();
            // non-transaction, but session will automatically acknowledges a client's receipt of the message
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // specify which queue we want to retrieve the message from
            queue = session.createQueue("message queue");
            messageConsumer = session.createConsumer(queue);

            messageListener =  new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        String myMessage = ((TextMessage) message).getText();
                        System.out.println("----You have a new message----");
                        System.out.println(myMessage);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void startReceiving() throws JMSException {
        try {
            messageConsumer.setMessageListener(messageListener);
            con.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
