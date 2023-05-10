package com.epam.queue.tasks.rabbit.simple.tasks;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.interfaces.Sender;
import com.epam.queue.tasks.rabbit.simple.SimpleReceiver;
import com.epam.queue.tasks.rabbit.simple.SimpleSender;

import java.util.concurrent.*;

/*
* Функциональне тестирование. Задание 1.

* Цель: знакомство с очередями и основными инструментами мониторинга.
*
* 1) запустите брокер в докере:
* docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
*
* 2) Запустите класс, дождитесь надписи 'Execution finished' в стандартном выводе.
*
* 3) Проверьте состояние очереди 'query-name-1'. Сколько сообщений ушло, сколько осталось (если осталось).
*    Для выполнения данного задания понадобится доступ в консоль:
*    docker container exec -it rabbitmq /bin/sh
*
* 4) Перезапустите контейнер с брокером. Проверьте состояние очереди. Сохранилось ли ее состояние? Почему
*    сохранилось/не сохранилось?
*
*/
public class FuncBasicDisconnectTask {

    public static void main(String[] args) throws InterruptedException {
        String q = "query-name-1";
        Receiver rcv = new SimpleReceiver();
        Sender snd = new SimpleSender();

        rcv.setQueueName(q);
        snd.setQueueName(q);

        ExecutorService exec = Executors.newFixedThreadPool(4);

        Future<?> sf = exec.submit(() -> {
            for (int i = 0; i < 10; i++) {
                snd.send("my message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Future<?> rf = exec.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
                rcv.receive();
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rcv.close();
        });

        try {
            sf.get();
            rf.get();
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            System.out.println("Execution finished.");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            snd.close();
            rcv.close();
        }
    }
}
