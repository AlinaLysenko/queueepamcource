package com.epam.queue.tasks.rabbit.queue.tasks;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.interfaces.Sender;
import com.epam.queue.tasks.rabbit.queue.PersistReceiver;
import com.epam.queue.tasks.rabbit.queue.PersistSender;

import java.util.concurrent.*;

/*
 * Функциональне тестирование. Задание 2.

 * Цель: знакомство с ack.
 *
 * 1) запустите брокер в докере:
 *    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 *
 * 2) Запустите класс, дождитесь надписи 'Execution finished' в стандартном выводе.
 *
 * 3) Проверьте состояние очереди 'pers-query-1'. Сколько сообщений ушло, сколько осталось (если осталось).
 *    Для выполнения данного задания понадобится доступ в консоль:
 *    docker container exec -it rabbitmq /bin/sh
 *
 * 4) Перезапустите контейнер с брокером. Проверьте состояние очереди. Сохранилось ли ее состояние? Почему
 *    сохранилось/не сохранилось?
 *
 */
public class FuncPersistDisconnectTask {

    public static void main(String[] args) {
        String q = "pers-query-1";
        Receiver rcv1 = new PersistReceiver();
        rcv1.setQueueName(q);

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

        Future<?> sf = exec.submit(() -> {
            for (int i = 0; i < 25; i++) {
                snd1.send("persist message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(499);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Future<?> sf2 = exec.submit(() -> {
            for (int i = 25; i < 50; i++) {
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
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            System.out.println("Execution finished.");

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            snd1.close();
            snd2.close();
            rcv1.close();
        }
    }
}
