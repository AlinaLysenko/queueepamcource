package com.epam.queue.tasks.rabbit.simple;

import com.epam.queue.tasks.interfaces.Sender;
import com.epam.queue.tasks.rabbit.Connections;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class SimpleSender implements Sender {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSender.class);

    private final Connection connection;
    private final Channel channel;
    private String queueName;

    public SimpleSender() {
        this.connection = Connections.getConnection();
        this.channel = Connections.getChannel(connection);
    }

    @Override
    public void setQueueName(String name) {
        this.queueName = name;
    }

    public void send(String... msgs) {
        try {
            channel.queueDeclare(queueName, false, false, false, null);
            for (String msg : msgs) {
                msg = msg + " : " + this.hashCode();
                channel.basicPublish("", queueName, null, msg.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + msg + "'");
            }
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
