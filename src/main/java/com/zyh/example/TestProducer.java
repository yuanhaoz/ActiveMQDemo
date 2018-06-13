package com.zyh.example;

/**
 * 生产者开始生产消息
 * Created by yuanhao on 6/13/18.
 */
public class TestProducer {

    public static void main(String[] args) {
        Producer producer = new Producer();
        producer.init();
        TestProducer testProducer = new TestProducer();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // Thread 1
        new Thread(testProducer.new ProducerMq(producer)).start();
        // Thread 2
        new Thread(testProducer.new ProducerMq(producer)).start();
        // Thread 3
        new Thread(testProducer.new ProducerMq(producer)).start();
        // Thread 4
        new Thread(testProducer.new ProducerMq(producer)).start();
        // Thread 5
        new Thread(testProducer.new ProducerMq(producer)).start();
    }

    private class ProducerMq implements Runnable {

        Producer producer;

        public ProducerMq(Producer producer) {
            this.producer = producer;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    producer.sendMessage("messageQueue");
                    Thread.sleep(10000);
                } catch (Exception e) {

                }
            }
        }
    }

}
