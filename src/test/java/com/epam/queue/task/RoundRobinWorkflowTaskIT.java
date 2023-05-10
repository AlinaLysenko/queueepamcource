package com.epam.queue.task;

import com.epam.queue.tasks.rabbit.queue.tasks.RoundRobinWorkflowTask;
import com.epam.queue.tasks.utils.HttpClientUtils;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * Очереди для автоматизаторов. AMQP. Задание 3.
 * <p>
 * Запускается тест, начинается отправка и прием сообщений, но потом происходит что-то ужасное и тест падает.
 * <p>
 * Задача: следать так, чтобы тест {@link #testRoundRobin()} проходил. Для этого нужно положить что-то в
 * {@link RoundRobinWorkflowTask#addMagic(Object)}.
 * <p>
 * Материалы, которые могут помочь в этом еще более нелегком деле:
 * <a href="https://www.rabbitmq.com/tutorials/tutorial-two-java.html">
 */
public class RoundRobinWorkflowTaskIT {

    private final RoundRobinWorkflowTask task = new RoundRobinWorkflowTask();

    @Test
    public void testRoundRobin() {
        task.init();
        String queryName = RoundRobinWorkflowTask.QUERY_NAME;
        //Нужно добавить шаг прямо сюда:
        //--------/---------/---------/---------/
        assertEquals(0, HttpClientUtils.getMessageReady(queryName));
    }
}
