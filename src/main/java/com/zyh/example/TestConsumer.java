package com.zyh.example;

/**
 * 消费者开始消费消息
 * Created by yuanhao on 6/13/18.
 */
public class TestConsumer {

    public static void main(String[] args) {
        Consumer consumer = new Consumer();
        consumer.init();
        TestConsumer testConsumer = new TestConsumer();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
    }

    private class ConsumerMq implements Runnable {

        Consumer consumer;

        public ConsumerMq(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    consumer.getMessage("messageQueue");
                    Thread.sleep(10000);
                } catch (Exception e) {

                }
            }
        }
    }

}
