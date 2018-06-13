package com.zyh.example;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建消费者
 * Created by yuanhao on 6/13/18.
 */
public class Consumer {

    // ActiveMQ 的默认用户名
    private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;
    // ActiveMQ 的默认登录密码
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    // ActiveMQ 的l链接地址
    private static final String BROKEN_URL = ActiveMQConnection.DEFAULT_BROKER_URL;

    AtomicInteger count = new AtomicInteger(0);
    // 链接工厂
    ConnectionFactory connectionFactory;
    // 链接对象
    Connection connection;
    // 事务管理
    Session session;
    // 线程
    ThreadLocal<MessageConsumer> threadLocal = new ThreadLocal<>();

    public void init() {
        try {
            // 创建一个链接工厂
            connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKEN_URL);
            // 从工厂中创建一个链接
            connection = connectionFactory.createConnection();
            // 开启链接
            connection.start();
            // 创建一个事务（这里通过参数可以设置事务的级别）
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            System.out.println(e.getMessage());
        }
    }

    public void getMessage(String disname) {
        try {
            // 创建一个消息队列
            Queue queue = session.createQueue(disname);
            // 消息生产者
            MessageConsumer messageConsumer = null;
            if (threadLocal.get() != null) {
                messageConsumer = threadLocal.get();
            } else {
                messageConsumer = session.createConsumer(queue);
                threadLocal.set(messageConsumer);
            }
            while (true) {
                Thread.sleep(1000);
                TextMessage msg = (TextMessage) messageConsumer.receive();
                if (msg != null) {
                    msg.acknowledge();
                    System.out.println(Thread.currentThread().getName() +
                            ", consumer: consuming Msg, " + msg.getText() +
                            "--->" + count.getAndIncrement());
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
