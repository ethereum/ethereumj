package org.ethereum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import java.util.concurrent.*;

/**
 * Created by Anton Nashatyrev on 17.12.2015.
 */
public class FutureTest {

    @Test
    public void interruptTest() throws InterruptedException {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        final ListenableFuture<Object> future = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
//                try {
                    System.out.println("Waiting");
                    Thread.sleep(10000);
                    System.out.println("Complete");
                    return null;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw e;
//                }
            }
        });
        future.addListener(new Runnable() {
            @Override
            public void run() {
                System.out.println("Listener: " + future.isCancelled() + ", " + future.isDone());
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }, MoreExecutors.sameThreadExecutor());

        Thread.sleep(1000);
        future.cancel(true);
    }

    @Test
    public void guavaExecutor() throws ExecutionException, InterruptedException {
//        ListeningExecutorService executor = MoreExecutors.listeningDecorator(
//                new ThreadPoolExecutor(2, 16, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        ExecutorService executor =
                new ThreadPoolExecutor(16, 16, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
        Future<Object> future = null;
        for (int i = 0; i < 4; i++) {
            final int ii = i;
            future = executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        System.out.println("Waiting " + ii);
                        Thread.sleep(5000);
                        System.out.println("Complete " + ii);
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            });
        }
        future.get();
    }

    @Test
    public void anyFutureTest() throws ExecutionException, InterruptedException {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(
                new ThreadPoolExecutor(16, 16, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        AnyFuture<Integer> anyFuture = new AnyFuture<Integer>() {
            @Override
            protected void postProcess(Integer integer) {
                System.out.println("FutureTest.postProcess:" + "integer = [" + integer + "]");
            }
        };
        for (int i = 0; i < 4; i++) {
            final int ii = i;
            ListenableFuture<Integer> future = executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    System.out.println("Waiting " + ii);
                    Thread.sleep(5000 - ii * 500);
                    System.out.println("Complete " + ii);
                    return ii;
                }
            });
            anyFuture.add(future);
        }
        Thread.sleep(1000);
        anyFuture.cancel(true);
        System.out.println("Getting anyFuture...");
        System.out.println("anyFuture: " + anyFuture.isCancelled() + ", " + anyFuture.isDone());

        anyFuture = new AnyFuture<Integer>() {
            @Override
            protected void postProcess(Integer integer) {
                System.out.println("FutureTest.postProcess:" + "integer = [" + integer + "]");
            }
        };
        for (int i = 0; i < 4; i++) {
            final int ii = i;
            ListenableFuture<Integer> future = executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    System.out.println("Waiting " + ii);
                    Thread.sleep(5000 - ii * 500);
                    System.out.println("Complete " + ii);
                    return ii;
                }
            });
            anyFuture.add(future);
        }
        System.out.println("Getting anyFuture...");
        System.out.println("anyFuture.get(): " + anyFuture.get() + ", " + anyFuture.isCancelled() + ", " + anyFuture.isDone());
    }
}
