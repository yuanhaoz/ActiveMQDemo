# ActiveMQ & JMS

ActiveMQ是Apache所提供的一个开源的消息系统，完全采用Java来实现，因此，它能够很好地支持J2EE提出的JMS规范。JMS(Java Message Service, 即Java消息服务)是一组Java应用程序接口，它提供消息的创建/发送/接收/读取等一系列服务。JMS定义了一组公共应用程序接口和相应的语法，类似于Java数据库的统一访问接口 JDBC，它是一种与厂商无关的API，使得Java程序能够与不同厂商的消息组件很好地进行通信。

JMS支持的消息类型包括简单文本（TextMessage）、可序列化的对象（ObejctMessage）、键值对（MapMessage）、字节流（BytesMessage）、流（StreamMessage），以及无有效负载的消息（Message）等。消息的发送是异步的，因此，消息的发布者发送完消息之后，不需要等待消息接收者立即响应，这样便提高了分布式系统协作的效率。

JMS支持两种消息发送和接收模型。
- 一种称为 Point-to-Point (P2P) 模型，即采用点对点的方式发送消息。P2P模型是基于 queue (队列)的，消息的生产者发送消息到队列，消息消费者从队列中接收消息，队列的存在使得消息的异步传输成为可能，P2P 模型在点对点的情况下进行消息传递时采用。
- 另一种成为 Pub/Sub (Publish/Subscribe，即发布/订阅) 模型，发布/订阅模型定义了如何向一个内容节点发布和订阅信息，这个内容节点称为 topic（主题）。主题可以认为是消息传递的中介，消息发布者将消息发布到某个主题，而消息订阅者则从主题订阅消息。主题使得消息的订阅者与消息的发布者相互保持独立，不需要进行接触即可保证消息的传递，发布/订阅模型在消息的一对多广播时采用。

如图1所示，对于点对点消息传递模型来说，多个消息的生产者和消息的消费者都可以注册到同一个消息队列，当消息的生产者发送一条消息之后，只有其中一个消息消费者会接收到消息生产者所发送的消息，而不是所有的消息消费者都会接收到该消息。

如图2所示，对于发布/订阅消息传递模型来说，消息的发布者需将消息投递给topic，而消息的订阅者则需要在相应的 topic 进行注册，以便接收相应 topic 的消息。与点对点消息传输模型不同的是，消息发布者的消息将被自动发送给所有订阅了该 topic 的消息订阅者。当消息订阅者某段时间由于某种原因断开了与消息发布者的连接时，这个时间段内的消息将会丢失，除非将消息的订阅模式设置为持久订阅（durable subscription），这时消息的发布者将会为消息的订阅者保留这段时间所产生的消息。当消息的订阅者重新连接消息发布者时，消息订阅者仍然可以获得这部分消息，而不至于丢失这部分消息。

## ActiveMQ 集群部署

针对分布式环境下对系统高可用的严格要求，以及面临高并发的用户访问，海量的消息发送等场景的挑战，单个 ActiveMQ 实例往往难以满足系统高可用与容量扩展的需求，这时 ActiveMQ 的高可用方案及集群部署就显得十分重要了。

当一个应用被部署到生产环境中，进行容错和避免单点故障是十分重要的，这样可以避免因为单个节点的不可用而导致整个系统的不可用。目前 ActiveMQ 所提供的高可用方案主要是**基于 Master-Slave 模式实现的冷备方案**，较为常用的包括：**基于共享文件系统的 Master-Slave 架构** 和 **基于 共享数据库的 Master-Slave 架构**。

- **基于共享文件系统的 Master-Slave 架构**。如图3所示，当 Master 启动时，它会获得共享文件系统的排他锁，而其他 Slave 则 Stand-by，不对外提供服务，同时等待获取 Master 的排它锁。假如 Master 连接中断或者发生异常，那么它的排它锁则会立即释放，此时便会有另外一个 Slave 能够争夺到 Master 的排它锁，从而成为 Master，对外提供服务。当之前因故障或者连接中断而丢失排它锁的 Master 重新连接到共享文件系统时，排它锁已经被抢占了，它将作为 Slave 等待，直到 Master 再一次发生异常。

- **基于共享数据库的 Master-Slave 架构**。如图4所示，当 Master 启动时，会先获取数据库某个表的排它锁，而其他 Slave 则 Stand-by，等待表锁，直到 Master 发生异常，连接丢失。这时表锁将释放，其他 Slave 将获得表锁，从而成为 Master 并对外提供服务，Master 与 Slave 自动完成切换，完全不需要人工干预。

当系统规模不断地发展，产生和消费消息的客户端越来越多，并发的请求数以及发送的消息量不断增加，使得系统逐渐地不堪重负。可以使用以下方式解决：
- 采用垂直扩展可以提升 ActiveMQ 单broker的处理能力。扩展最直接的办法就是提升硬件的性能，如提高CPU和内存的能力。再者就是通过调节ActiveMQ本身的一些配置来提升系统的并发处理能力，如使用nio替代阻塞I/O，或调整JVM和ActiveMQ的可用内存空间等。
- 垂直扩展到一定程度时必然会遇到瓶颈，这时需要对系统进行相应的水平扩展。对于ActiveMQ来说，可以采用broker拆分的方式，将不相关的queue和topic拆分到多个broker，来达到提升系统吞吐能力的目的。

假设使用消息系统来处理订单状态的流转，对应的topic可能包括订单创建、购买者支付、售卖者发货、购买者确定收货、购买者确认付款、购买者发起退款、售卖者处理退款等，如图5所示。

原本一个broker可以承载多个queue或者topic，现在将不相关的queue和topic拆出来放到多个broker当中，这样可以将一部分消息量大并发请求多的queue独立出来单独进行处理，避免了queue或者topic之间的相互影响，提高了系统的吞吐量，使得系统能够支撑更大的并发请求量并处理更多的消息。当然，如有需要，还可以对queue和topic进行进一步拆分，类似于数据库的分库分表策略，以提高系统整体的并发处理能力。