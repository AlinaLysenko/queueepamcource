package com.epam.queue.tasks.rabbit.queue;

import com.epam.queue.tasks.interfaces.Sender;
import com.epam.queue.tasks.rabbit.Connections;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class PersistSender implements Sender {

    private static final Logger logger = LoggerFactory.getLogger(PersistSender.class);

    private final Connection connection;
    private final Channel channel;
    private String queueName;

    public PersistSender() {
        this.connection = Connections.getConnection();
        this.channel = Connections.getChannel(connection);
    }

    @Override
    public void setQueueName(String name) {
        this.queueName = name;
    }

    @Override
    public void send(String... argv) {
        try {
            boolean durable = true;
            channel.queueDeclare(queueName, durable, false, false, null);
            for (String message : argv) {
                channel.basicPublish("", queueName,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
