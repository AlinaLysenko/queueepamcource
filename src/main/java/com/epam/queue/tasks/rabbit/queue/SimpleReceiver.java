package com.epam.queue.tasks.rabbit.queue;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.rabbit.Connections;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(SimpleReceiver.class);

    private final Connection connection;
    private final Channel channel;
    private String queueName;

    public SimpleReceiver() {
        this.connection = Connections.getConnection();
        this.channel = Connections.getChannel(connection);
    }

    @Override
    public void setQueueName(String name) {
        this.queueName = name;
    }

    public void receive() {
        try {
            channel.queueDeclare(queueName, false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void close() {
        if (Objects.nonNull(channel)) {
            try {
                channel.close();
                logger.info("Channel closed.");
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        if (Objects.nonNull(connection)) {
            try {
                connection.close();
                logger.info("Connection closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
