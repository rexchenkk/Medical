package com.mobiuspace.medical.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtil {
  //CPU核心数
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  //cpu密集型核心线程数
  private static final int CPUINTENSIVE_CORE_POOL_SIZE = CPU_COUNT + 1;
  //cpu密集型最大线程数量
  private static final int CPUINTENSIVE_MAXMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
  //cpu密集型线程工厂类
  private static final ThreadFactory mCPUIntensiveThreadFactory = new ThreadFactoryInner("CPUIntensiveThread");
  //io密集型核心线程数
  private static final int IOINTENSIVE_CORE_POOL_SIZE = CPU_COUNT * 2;
  //io密集型最大线程数量
  private static final int IOINTENSIVE_MAXMUM_POOL_SIZE = IOINTENSIVE_CORE_POOL_SIZE * 2 ;
  //io密集型线程工厂类
  private static final ThreadFactory mIOIntensiveThreadFactory = new ThreadFactoryInner("IOIntensiveThread");
  //非核心线程闲置时间为1秒
  private static final int KEEP_TIME = 1;
  //线程池任务队列容量为1024
  private static final BlockingQueue<Runnable> mPoolWorkQueue = new LinkedBlockingQueue<>(1024);
  //拒绝策略使用老化淘汰策略，目前任务队列容量已经设置很大，如果这样都超过了，会选择淘汰老旧任务
  private static final RejectedExecutionHandler mRejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
  private static final ThreadPoolExecutor sCPUIntensiveExecutor = new ThreadPoolExecutor(
          CPUINTENSIVE_CORE_POOL_SIZE, CPUINTENSIVE_MAXMUM_POOL_SIZE, KEEP_TIME, TimeUnit.SECONDS, mPoolWorkQueue, mCPUIntensiveThreadFactory,mRejectedExecutionHandler);
  private static final ThreadPoolExecutor sIOIntensiveExecutor = new ThreadPoolExecutor(
          IOINTENSIVE_CORE_POOL_SIZE, IOINTENSIVE_MAXMUM_POOL_SIZE, KEEP_TIME, TimeUnit.SECONDS, mPoolWorkQueue, mIOIntensiveThreadFactory,mRejectedExecutionHandler);

  private static Handler sHandler = new Handler(Looper.getMainLooper());

  static class ThreadFactoryInner implements ThreadFactory {
    private String threadFactoryName;
    private final AtomicInteger mCount = new AtomicInteger(1);

    public ThreadFactoryInner(String threadFactoryName) {
      this.threadFactoryName = threadFactoryName;
    }

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, threadFactoryName + " Current ID = " + mCount.getAndIncrement());
    }
  }

  //默认跑在cpu密集型
  public static void runOnSubThread(Runnable runnable) {
    runOnCPUIntensiveThread(runnable);
  }


  /**
   * 在 cpu 密集型线程执行任务，如果当前已经在线程中，不会做线程切换
   * @param runnable
   */
  public static void runOnCPUIntensiveThread(Runnable runnable) {
    if (runnable == null)
      return;
    if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
      sCPUIntensiveExecutor.execute(runnable);
    } else {
      runnable.run();
    }
  }

  /**
   * 在 io 密集型线程执行任务，如果当前已经在线程中，不会做线程切换
   * @param runnable
   */
  public static void runOnIOIntensiveThread(Runnable runnable) {
    if (runnable == null)
      return;
    if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
      sIOIntensiveExecutor.execute(runnable);
    } else {
      runnable.run();
    }
  }

  public static void runOnUiThread(Runnable runnable) {
    if (runnable == null)
      return;
    if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
      runnable.run();
    } else {
      sHandler.post(runnable);
    }
  }

  public static void runOnUIThreadDelayed(Runnable runnable,long delayMillis) {
    sHandler.postDelayed(runnable,delayMillis);
  }

  public static boolean isInterrupted() {
    return Thread.currentThread().isInterrupted();
  }

  public static boolean currentOnMainThread() {
    return Thread.currentThread() == Looper.getMainLooper().getThread();
  }

}
