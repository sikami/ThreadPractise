package com.lis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static final String EOF = "EOF";

    public static void main(String[] args) {
        ArrayBlockingQueue<String> buffer = new ArrayBlockingQueue<String>(6);
        ReentrantLock bufferLock = new ReentrantLock();
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        MyProducer myProducer = new MyProducer(buffer, ThreadColor.ANSI_BLUE);
        MyConsumer myConsumer = new MyConsumer(buffer, ThreadColor.ANSI_PURPLE);
        MyConsumer myConsumer1 = new MyConsumer(buffer, ThreadColor.ANSI_CYAN);

        executorService.execute(myProducer);
        executorService.execute(myConsumer);
        executorService.execute(myConsumer1);

        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(ThreadColor.ANSI_RED + "i am from callable class");
                return "This is callable result";
            }
        });

        try {
            System.out.println(future.get());
        } catch (ExecutionException e) {
            System.out.println("Something is wrong");
        } catch (InterruptedException e) {
            System.out.println("future is interupted");
        }
        executorService.shutdown();
    }
}

class MyProducer implements Runnable {

    private ArrayBlockingQueue<String> buffer;
    private String color;

    public MyProducer(ArrayBlockingQueue<String> buffer, String color) {
        this.buffer = buffer;
        this.color = color;
    }

    @Override
    public void run() {
        Random random = new Random();
        String[] nums = {"1", "2", "3", "4", "5"};

        for (String num : nums) {
            try {
                System.out.println(color + "Adds... " + num);
                buffer.put(num);
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                System.out.println("Producer is interrupted");
            }
        }

        System.out.println(color + "Adding EOF and exiting..");
        try {
            buffer.put(Main.EOF);
        } catch (InterruptedException e) {
        }

    }
}

class MyConsumer implements Runnable {
    private ArrayBlockingQueue<String> buffer;
    private String color;

    public MyConsumer(ArrayBlockingQueue<String> buffer, String color) {
        this.buffer = buffer;
        this.color = color;
    }


    @Override
    public void run() {
        while (true) {
            synchronized (buffer) {
                try {
                    if (buffer.isEmpty()) {
                        continue;
                    }

                    if (buffer.peek().equals(Main.EOF)) {
                        System.out.println(color + "exiting");
                        break;
                    } else {
                        System.out.println(color + "Removed " + buffer.take());
                    }
                } catch (InterruptedException e) {

                }
            }
        }

    }
}

