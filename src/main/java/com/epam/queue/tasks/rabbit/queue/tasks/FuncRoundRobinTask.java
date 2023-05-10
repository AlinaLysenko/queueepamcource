package com.epam.queue.tasks.rabbit.queue.tasks;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.interfaces.Sender;
import com.epam.queue.tasks.rabbit.queue.PersistReceiver;
import com.epam.queue.tasks.rabbit.queue.PersistSender;

import java.util.concurrent.*;

/*
 * Функциональне тестирование. Задание 3.

 * Цель: знакомство с round-robin.
 *
 * 1) запустите брокер в докере:
 *    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 *
 * 2) Запустите класс, дождитесь надписи 'Execution finished' в стандартном выводе.
 *
 * 3) Посмотрите на сообщения в стандартном выводе.
 *    а) Какой получатель (id) начал обработку (прием) сообщений?
 *    б) Какой получатель (id) завершил обработку (прием) сообщений?
 *    в) Почему какой-то промежуток времени сообщения никем не обрабатывались?
 *
 */
public class FuncRoundRobinTask {

    public static void main(String[] args) {
        String q = "rr-query-1";
        Receiver rcv1 = new PersistReceiver();
        Receiver rcv2 = new PersistReceiver();
        rcv1.setQueueName(q);
        rcv2.setQueueName(q);

        Sender snd1 = new PersistSender();
        Sender snd2 = new PersistSender();
        snd1.setQueueName(q);
        snd2.setQueueName(q);

        ExecutorService exec = Executors.newFixedThreadPool(4);

        Future<?> rf1 = exec.submit(() ->{
            rcv1.receive();
            try {
                TimeUnit.MILLISECONDS.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rcv1.close();
        });

        Future<?> rf2 = exec.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(25000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rcv2.receive();
        });

        Future<?> sf = exec.submit(() -> {
            for (int i = 0; i < 50; i++) {
                snd1.send("persist message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(499);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Future<?> sf2 = exec.submit(() -> {
            for (int i = 50; i < 100; i++) {
                snd1.send("persist message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(501);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            sf.get();
            sf2.get();
            rf1.get();
            rf2.get();
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            System.out.println("Execution finished.");

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            snd1.close();
            snd2.close();
            rcv1.close();
            rcv2.close();
        }
    }
}
