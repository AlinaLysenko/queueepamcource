package com.epam.queue.task;

import com.epam.queue.tasks.rabbit.queue.tasks.FixReceiverWithAckTask;
import com.epam.queue.tasks.rabbit.queue.PersistReceiverToFix;
import com.epam.queue.tasks.utils.HttpClientUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 *  Очереди для автоматизаторов. AMQP. Задание 2.
 *
 *  Класс {@link PersistReceiverToFix} реализует интерфейс {@link com.epam.queue.tasks.interfaces.Receiver}.
 *
 *  Задача: следать так, чтобы тест {@link #testAckReceiver()} проходил. Для этого нужно пофиксить
 *  {@link PersistReceiverToFix#receive()}.
 *
 *  Материалы, которые могут помочь в этом еще более нелегком деле:
 *  <a href="https://www.rabbitmq.com/tutorials/tutorial-two-java.html">
 *  Обратить внимание на ack (что это и зачем).
 */
public class FixReceiverTaskIT {

    private static final String Q_NAME = "p-queue-2";

    private final FixReceiverWithAckTask task = new FixReceiverWithAckTask(Q_NAME);

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
    public void testAckReceiver() {
        task.applyReceiver(new PersistReceiverToFix());
        task.startSender();
        //No messages must be ready:
        assertEquals(0, HttpClientUtils.getMessageReady(Q_NAME));
    }
}
