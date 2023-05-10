package com.epam.queue.tasks.interfaces;

public interface Sender {

    void setQueueName(String name);

    void send(String... argv);

    void close();
}
