package com.zyh.example;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建生产者
 * Created by yuanhao on 6/13/18.
 */
public class Producer {

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
    ThreadLocal<MessageProducer> threadLocal = new ThreadLocal<>();

    public void init() {
        try {
            // 创建一个链接工厂
            connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKEN_URL);
            // 从工厂中创建一个链接
            connection = connectionFactory.createConnection();
            // 开启链接
            connection.start();
            // 创建一个事务（这里通过参数可以设置事务的级别）
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(String disname) {
        try {
            // 创建一个消息队列
            Queue queue = session.createQueue(disname);
            // 消息生产者
            MessageProducer messageProducer = null;
            if (threadLocal.get() != null) {
                messageProducer = threadLocal.get();
            } else {
                messageProducer = session.createProducer(queue);
                threadLocal.set(messageProducer);
            }
            while (true) {
                Thread.sleep(1000);
                int num = count.getAndIncrement();
                // 创建一条消息
                TextMessage msg = session.createTextMessage(Thread.currentThread().getName() +
                        ", consumer: producing things, count: " + count);
                System.out.println(Thread.currentThread().getName() +
                        ", consumer: producing things, count: " + count);
                // 发送消息
                messageProducer.send(msg);
                // 提交事务
                session.commit();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
