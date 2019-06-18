package com.mamba.benchmark.common.executor.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom ThreadFactory, For set thread name, priority
 */
public class CustomThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    private final String poolName;

    private final ThreadGroup threadGroup;

    private int threadPriority;

    private final String threadNamePrefix;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public CustomThreadFactory(String poolName) {
        this(poolName, Thread.NORM_PRIORITY);
    }

    public CustomThreadFactory(String poolName, int threadPriority) {
        SecurityManager securityManager = System.getSecurityManager();
        this.poolName = poolName;
        this.threadGroup = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.threadPriority = threadPriority;
        this.threadNamePrefix = poolName + '-' + POOL_NUMBER.getAndIncrement() + '-';
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.threadGroup, r, this.threadNamePrefix + this.threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != this.threadPriority) {
            t.setPriority(this.threadPriority);
        }
        return t;
    }
}
