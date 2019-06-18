package com.mamba.benchmark.common.executor;

import com.mamba.benchmark.common.executor.impl.ConcurrencyExecutor;
import com.mamba.benchmark.common.executor.impl.ThroughputExecutor;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public abstract class PressureExecutor<T extends Runnable> implements Closeable {

    /**
     * Concurrency limiter
     */
    private final IntSupplier limiter;

    /**
     * Task generator
     */
    protected final IntFunction<List<T>> generator;

    /**
     * Task preparer
     */
    protected final ScheduledThreadPoolExecutor preparer;

    /**
     * Task executor
     */
    protected final ThreadPoolExecutor executor;

    protected volatile boolean shutdown = false;

    public PressureExecutor(IntSupplier limiter, IntFunction<List<T>> generator, ScheduledThreadPoolExecutor preparer, ThreadPoolExecutor executor) {
        this.limiter = Objects.requireNonNull(limiter);
        this.generator = Objects.requireNonNull(generator);
        this.preparer = Objects.requireNonNull(preparer);
        this.executor = Objects.requireNonNull(executor);
    }

    public abstract void start(long delay);

    protected abstract void cancel();

    @Override
    public synchronized void close() {
        try {
            this.cancel();
        } catch (Exception e) {
        }
        this.shutdown = true;
        this.executor.shutdownNow();
        this.preparer.shutdownNow();
    }

    protected final int getLimit() {
        int limit = this.limiter.getAsInt();
        if (limit < 0) {
            synchronized (this) {
                this.notify();
            }
        }
        return limit;
    }

    protected void await() {
        do {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }
        } while (this.limiter.getAsInt() >= 0);
        this.cancel();
    }

    public static <T extends Runnable> ConcurrencyExecutor<T> concurrency(IntFunction<List<T>> generator, IntSupplier concurrency) {
        return new ConcurrencyExecutor<>(generator, concurrency);
    }

    public static <T extends Runnable> ThroughputExecutor<T> throughput(IntFunction<List<T>> generator, IntSupplier throughput) {
        return new ThroughputExecutor<>(generator, throughput);
    }
}
