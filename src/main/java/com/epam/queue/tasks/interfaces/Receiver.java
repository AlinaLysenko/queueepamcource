package com.epam.queue.tasks.interfaces;

public interface Receiver {

    void setQueueName(String name);

    void receive();

    void close();

}
