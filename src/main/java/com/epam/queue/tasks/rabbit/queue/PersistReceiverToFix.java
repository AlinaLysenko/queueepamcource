package com.epam.queue.tasks.rabbit.queue;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.rabbit.Connections;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public class PersistReceiverToFix implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(PersistReceiver.class);

    private final int id = ThreadLocalRandom.current().nextInt(100);
    private final Connection connection;
    private final Channel channel;
    private String queueName;

    public PersistReceiverToFix() {
        this.connection = Connections.getConnection();
        this.channel = Connections.getChannel(connection);
    }

    @Override
    public void setQueueName(String name) {
        this.queueName = name;
    }

    /* TODO: fix this method. */
    @Override
    public void receive() {
        try {
            boolean durable = true;
            channel.queueDeclare(queueName, durable, false, false, null);
            System.out.println(" [*] Waiting for messages. [rcvId = " + id + "]");

            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.println(" [x] Received '" + message + "'" + ". [rcvId = " + id + "]");

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                System.out.println(" [x] Message acked. [msg=" + message + ", rcvId = " + id + "]");

            };
            boolean autoAck = true;
            channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(channel) && channel.isOpen()) {
            try {
                channel.close();
                logger.info("Channel closed.");
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        if (Objects.nonNull(connection) && connection.isOpen()) {
            try {
                connection.close();
                logger.info("Connection closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
