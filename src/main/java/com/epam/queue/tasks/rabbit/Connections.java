package com.epam.queue.tasks.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Connections {

    private static final ConnectionFactory factory = new ConnectionFactory();

    public static Connection getConnection() {
        try {
            factory.setHost("localhost");
            return factory.newConnection();
        } catch (TimeoutException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Channel getChannel(Connection connection) {
        try {
            return connection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
