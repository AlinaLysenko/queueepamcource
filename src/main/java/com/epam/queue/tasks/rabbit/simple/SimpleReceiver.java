package com.epam.queue.tasks.rabbit.simple;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.rabbit.Connections;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public class SimpleReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(SimpleReceiver.class);

    private final int id = ThreadLocalRandom.current().nextInt(100);
    private final Connection connection;
    private final Channel channel;
    private String queueName;

    public SimpleReceiver() {
        this.connection = Connections.getConnection();
        this.channel = Connections.getChannel(connection);
    }

    public void setQueueName(String name) {
        this.queueName = name;
    }

    @Override
    public void receive() {
        try {
            channel.queueDeclare(queueName, false, false, false, null);
            System.out.println(" [*] Waiting for messages. [rcvId = " + id + "]");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'" + "[rcvId=" + id + "]");
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void close() {
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
