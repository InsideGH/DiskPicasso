package com.peter100.home.pablopicasso.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Use the Builder to configure the executor. See Builder for default values.
 */
public class CacheExecutor implements ExecutorService {
    private final ThreadPoolExecutor mExecutor;

    /**
     * Default one single background thread with 1 minute timeout using a min thread priority.
     */
    public static class Builder {
        private int mCoreCount;
        private int mMaxCount;
        private TimeUnit mTimeUnit;
        private int mTimeOut;
        private BlockingQueue mQueue;
        private ThreadFactory mThreadFactory;

        public Builder() {
            mCoreCount = 1;
            mMaxCount = 1;
            mTimeOut = 1;
            mTimeUnit = TimeUnit.MINUTES;
        }

        public Builder setTimeOut(TimeUnit unit, int time) {
            mTimeUnit = unit;
            mTimeOut = time;
            return this;
        }

        public Builder setCore(int core) {
            mCoreCount = core;
            return this;
        }

        public Builder setMax(int max) {
            mMaxCount = max;
            return this;
        }

        public Builder setQueue(BlockingQueue<Runnable> queue) {
            mQueue = queue;
            return this;
        }

        public Builder setThreadFactory(ThreadFactory threadFactory) {
            mThreadFactory = threadFactory;
            return this;
        }

        public CacheExecutor build() {
            if (mQueue == null) {
                mQueue = new LinkedBlockingQueue();
            }
            if (mThreadFactory == null) {
                mThreadFactory = new CacheThreadFactory();
            }
            return new CacheExecutor(mCoreCount, mMaxCount, mTimeUnit, mTimeOut, mThreadFactory, mQueue);
        }

        private class CacheThreadFactory implements ThreadFactory {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "PabloCacheExecutor");
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
        }
    }

    private CacheExecutor(int core, int max, TimeUnit timeUnit, int timeOut, ThreadFactory factory, BlockingQueue<Runnable> queue) {
        mExecutor = new ThreadPoolExecutor(core, max, timeOut, timeUnit, queue, factory);
        mExecutor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void shutdown() {
        mExecutor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return mExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return mExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return mExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return mExecutor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return mExecutor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return mExecutor.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return mExecutor.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return mExecutor.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return mExecutor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return mExecutor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mExecutor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        mExecutor.execute(command);
    }
}
