package com.epam.queue.task;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.rabbit.queue.SimpleReceiver;
import com.epam.queue.tasks.rabbit.simple.tasks.ImplementReceiverTask;
import com.epam.queue.tasks.utils.HttpClientUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 *  Очереди для автоматизаторов. AMQP. Задание 1.
 *
 *  Задача: реализовать интерфейс {@link com.epam.queue.tasks.interfaces.Receiver} и передать объект в
 *  {@link ImplementReceiverTask#applyReceiver(Receiver)} (смотрите метод {@link #testReceiver()}
 *
 *  Если тест прошел, то значит вы все сделали правильно.
 *
 *  Материалы, которые могут помочь в этом нелегком деле:
 *  <a href="https://www.rabbitmq.com/tutorials/tutorial-one-java.html">
 */
public class ImplementReceiverTaskIT {

    private static final String Q_NAME = "simple-queue-1";

    private final ImplementReceiverTask task = new ImplementReceiverTask(Q_NAME);

    @AfterEach
    public void tearDown() {
        try {
            task.exec.shutdown();
            task.exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReceiver() {
        task.applyReceiver(new SimpleReceiver());
        task.startSender();
        //Сообщений в очереди оставаться не должно:
        assertEquals(0, HttpClientUtils.getMessageReady(Q_NAME));
    }
}
