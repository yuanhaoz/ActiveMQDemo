package com.zyh.demo;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 通过 JMS 来创建 ActiveMQ 的 topic，并订阅 topic 接收消息
 * 消息发送到对应的 topic 后，需要将 listener 注册到需要订阅的 topic 上，以便能够接收该 topic 的消息
 * Created by yuanhao on 6/13/18.
 */
public class TopicSubscriber {

    public static void getMessage() throws Exception {
        // 创建一个 ActiveMQConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                ActiveMQConnection.DEFAULT_BROKER_URL
        );
        // 通过 ActiveMQConnectionFactory 来创建到 ActiveMQ 的连接，启动连接
        Connection connection = connectionFactory.createConnection();
        connection.start();
        // 通过连接创建 session
        // 第一个boolean参数表示是否采用事务消息。true表示是事务的，此时消息的提交自动由commit处理，消息的回滚自动由rollback处理。
        // false表示不是事务的，此时分为三种情况，Session.AUTO_ACKNOWLEDGE 表示Session会自动确认所接收到的消息/Session.CLIENT_ACKNOWLEDGE 表示由客户端程序通过调用消息的确认方法来确认所接收到的消息/Session.DUPS_OK_ACKNOWLEDGE 使得Session将“懒惰”地确认消息，即不会立即确认消息，这样有可能导致消息重复投递。
        Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        // 通过Session创建一个 topic
        Topic topic = session.createTopic("MessageTopic");

        // 通过 topic 来创建消息的消费者，消息的消费者需要在该 topic 上注册一个 listener，以便消息发送到该 topic之后，消息的消费者能够及时地接收到。
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println(textMessage.getText());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

}
