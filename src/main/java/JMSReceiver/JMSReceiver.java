package JMSReceiver;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.concurrent.Future;
import java.util.logging.Logger;


public class JMSReceiver {
    Queue queue;
    MessageConsumer messageConsumer;
    ConnectionFactory connectionFactory;
    Connection con;
    Session session;
    MessageListener messageListener;
    Logger logger = Logger.getLogger("JMSReceiver");

    public JMSReceiver() throws JMSException, IOException {
        File clientRequestFile = new File(System.getProperty("user.dir") + "/JMSReceiver/" + "ClientRequest.txt");

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
            queue = session.createQueue("message queue");
            messageConsumer = session.createConsumer(queue);

            messageListener =  new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        String myMessage = ((TextMessage) message).getText();
                        System.out.println("----You have a new message----");
                        System.out.println(myMessage);

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        String toWrite = myMessage + ";";
                        buffer.put(toWrite.getBytes());
                        buffer.flip();
                        System.out.println("about to write!");
                        AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/JMSReceiver/" + "ClientRequest.txt"), StandardOpenOption.WRITE);
                        asyncChannel.write(buffer, asyncChannel.size());
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
}
