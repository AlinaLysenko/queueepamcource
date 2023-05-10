package com.epam.queue.tasks.rabbit.queue;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.interfaces.Sender;

import java.util.concurrent.*;

public class PersistApp {

    public static void main(String[] args) {
        String q = "pers-query-1";
        Receiver rcv1 = new PersistReceiver();
        Receiver rcv2 = new PersistReceiver();
        rcv1.setQueueName(q);
        rcv2.setQueueName(q);

        Sender snd1 = new PersistSender();
        Sender snd2 = new PersistSender();
        snd1.setQueueName(q);
        snd2.setQueueName(q);

        ExecutorService exec = Executors.newFixedThreadPool(4);

        Future<?> rf1 = exec.submit(() -> {
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
                TimeUnit.MILLISECONDS.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rcv2.receive();
        });

        Future<?> sf = exec.submit(() -> {
            for (int i = 0; i < 50; i++) {
                snd1.send("persist message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Future<?> sf2 = exec.submit(() -> {
            for (int i = 50; i < 100; i++) {
                snd1.send("persist message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(400);
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
