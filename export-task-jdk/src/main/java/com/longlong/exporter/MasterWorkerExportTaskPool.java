package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class MasterWorkerExportTaskPool<T> extends ExportTaskPool<T> {

    /**
     * 存储工作者
     */
    private final Worker<T>[] workers;

    private final ArrayBlockingQueue<ExportTask<T>.LoadDataCallable> workerQueue;

    private final AtomicInteger tasks = new AtomicInteger(0);

    public MasterWorkerExportTaskPool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }


    public MasterWorkerExportTaskPool(int threads) {
        super(threads);
        this.workers = new Worker[threads];
        this.workerQueue = new ArrayBlockingQueue<>(1000);

        init();
    }

    private void init() {
        for (int i = 1; i <= workers.length; i++) {
            workers[i - 1].start();
        }
    }


    @Override
    public void invoke(int start, int end, T export, ExportService exportService, Set<Throwable> exceptions) throws Exception {

        tasks.set(end - start + 1);

        for (int i = start; i <= end; i++) {
            addTask(getExportTask().new LoadDataCallable(i, exportService, exceptions), exceptions);
        }

        LockSupport.park();
    }

    private void addTask(ExportTask<T>.LoadDataCallable task, Set<Throwable> exceptions) {
        boolean success = false;
        for (Worker worker : workers) {
            if (worker.finished) {
                worker.execute(task, Thread.currentThread());
                success = true;
            }
        }

        if (!success) {
            try {
                workerQueue.offer(task, 60L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                try {
                    task.call();
                } catch (Exception e1) {
                    exceptions.add(e1);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        for (Worker worker : workers) {
            Thread t = worker.thread;
            if (!t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }

        }
    }

    class Worker<T> implements Runnable {
        /**
         * 管理者引用
         */
        private final Thread thread;

        private ExportTask<T>.LoadDataCallable loadDataCallable;

        private volatile boolean finished = true;

        private volatile Thread lockThread;

        Worker(ExportTask<T>.LoadDataCallable loadDataCallable) {
            this.loadDataCallable = loadDataCallable;
            thread = new Thread(this);
        }

        void start() {
            thread.start();
        }

        void execute(ExportTask<T>.LoadDataCallable task, Thread lockThread) {
            loadDataCallable = task;
            this.lockThread = lockThread;
            finished = false;
        }


        @Override
        public void run() {
            for (; ; ) {
                if (loadDataCallable == null) {
                    loadDataCallable = (ExportTask<T>.LoadDataCallable) workerQueue.poll();
                }
                if (!finished && loadDataCallable != null) {
                    try {
                        finished = false;
                        loadDataCallable.call();
                        loadDataCallable = null;
                        tasks.decrementAndGet();
                        finished = true;
                        if (tasks.get() == 0 && lockThread != null) {
                            LockSupport.unpark(lockThread);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
