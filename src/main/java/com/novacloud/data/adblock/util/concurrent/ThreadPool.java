package com.novacloud.data.adblock.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPool {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);

	private static class NamedThreadFactory implements ThreadFactory {
	    private static final AtomicInteger poolNumber = new AtomicInteger(1);
	    private final ThreadGroup group;
	    private final AtomicInteger threadNumber = new AtomicInteger(1);
	    private final String namePrefix;
	    private final int priority;

	    protected NamedThreadFactory(final String name, final int priority) {
	        final SecurityManager s = System.getSecurityManager();
	        group = s != null? s.getThreadGroup(): Thread.currentThread().getThreadGroup();
	        namePrefix = "pool-" +
	                      poolNumber.getAndIncrement() +
	                     "-thread-" + name + "-";
	        this.priority = priority;
	    }

	    @Override
		public Thread newThread(@Nonnull final Runnable r) {
	        final Thread t = new Thread(group, r,
	                              namePrefix + threadNumber.getAndIncrement(),
	                              0);
            t.setDaemon(true);
            t.setPriority(priority);
	        return t;
	    }
	}
	// singleton
	private final static ThreadPool THREAD_POOL = new ThreadPool();
	public static ThreadPool getInstance() {
		return THREAD_POOL;
	}

	private final ExecutorService delegateHigh;
	private final ExecutorService delegateLow;


	private ThreadPool() {
		delegateHigh = Executors.newCachedThreadPool(new NamedThreadFactory("high", Thread.NORM_PRIORITY));
//		delegateLow = Executors.newCachedThreadPool(new NamedThreadFactory("low", Thread.MIN_PRIORITY));
		delegateLow = Executors.newFixedThreadPool(10, new NamedThreadFactory("low", Thread.MIN_PRIORITY));
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				ThreadPool.this.shutdown();
			}
		}));
		LOGGER.info("started!");
	}

	public void shutdown() {
		shutdown(delegateHigh);
		shutdown(delegateLow);
	}

	private void shutdown(final ExecutorService service) {
		if (!service.isShutdown()) {
			service.shutdown();
			LOGGER.info("shutdown {}", service);
		} else if (!service.isTerminated()) {
			service.shutdownNow();
			LOGGER.info("shutdown now {}", service);
		}
	}

	public Future<?> submit(final Runnable runnable) {
		return delegateHigh.submit(runnable);
	}

	public <T> Future<T> submit(final Callable<T> callable) {
		return delegateHigh.submit(callable);
	}

	public Future<?> submitLow(final Runnable runnable) {
		return delegateLow.submit(runnable);
	}

	public <T> Future<T> submitLow(final Callable<T> callable) {
		return delegateLow.submit(callable);
	}

	public Executor getExecutor() {
		return delegateLow;
	}
}
