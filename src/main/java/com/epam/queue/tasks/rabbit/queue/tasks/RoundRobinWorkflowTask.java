package com.epam.queue.tasks.rabbit.queue.tasks;

import com.epam.queue.tasks.interfaces.Receiver;
import com.epam.queue.tasks.interfaces.Sender;
import com.epam.queue.tasks.rabbit.queue.PersistReceiver;
import com.epam.queue.tasks.rabbit.queue.PersistSender;

import java.util.Objects;
import java.util.concurrent.*;

public class RoundRobinWorkflowTask {

    public static final String QUERY_NAME = "rr-query-3";

    private final CountDownLatch latch = new CountDownLatch(1);

    public void init() {

        Receiver rcv1 = new PersistReceiver();
        rcv1.setQueueName(QUERY_NAME);

        Sender snd1 = new PersistSender();
        Sender snd2 = new PersistSender();
        snd1.setQueueName(QUERY_NAME);
        snd2.setQueueName(QUERY_NAME);

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

        Future<?> sf = exec.submit(() -> {
            for (int i = 0; i < 25; i++) {
                snd1.send("persist message " + i);
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Future<?> sf2 = exec.submit(() -> {
            for (int i = 25; i < 50; i++) {
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
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            Thread.sleep(5000);
            latch.countDown();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            snd1.close();
            snd2.close();
            rcv1.close();
        }
    }

    public void addMagic(Object magicObject) {
        if (!(magicObject instanceof Receiver)) {
            return;
        }
        Receiver receiver = null;
        try {
            latch.await();
            receiver = (Receiver) magicObject;
            receiver.setQueueName(QUERY_NAME);
            receiver.receive();
            TimeUnit.MILLISECONDS.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(receiver)) {
                receiver.close();
            }
        }
    }
}
