package JMSReceiver;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.logging.Logger;


public class JMSReceiver {
    Queue queue;
    MessageConsumer messageConsumer;
    ConnectionFactory connectionFactory;
    Connection con;
    Session session;
    MessageListener messageListener;
    Logger logger = Logger.getLogger("JMSReceiver");

    public JMSReceiver(String queueName, String fileName) throws JMSException, IOException {
        File clientRequestFile = new File(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + fileName);
        if (clientRequestFile.createNewFile()) {
            logger.info(new Timestamp(System.currentTimeMillis()) + " clientRequestFile successfully created");
        } else {
            logger.info(new Timestamp(System.currentTimeMillis()) + " clientRequestFile already exists");
        };

        try {
            connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616"); // ActiveMQ-specific
            con = connectionFactory.createConnection();
            // non-transaction, but session will automatically acknowledges a client's receipt of the message
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // specify which queue we want to retrieve the message from
            queue = session.createQueue(queueName);
            messageConsumer = session.createConsumer(queue);

            messageListener =  new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        String myMessage = ((TextMessage) message).getText();
                        System.out.println("----You have a new message----");
                        System.out.println(myMessage);

                        // message queue can't return message directly. Writing to a local file so that server/client can consume the messages and do actions
                        File file = new File(System.getProperty("user.dir") + "/src/main/java" + "/JMSReceiver/" + fileName);
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        writer.write(myMessage + ";");
                        writer.close();
                    } catch (Exception e) {
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

    public void closeConnection() throws JMSException {
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
