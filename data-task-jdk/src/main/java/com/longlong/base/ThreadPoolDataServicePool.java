package com.longlong.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * threadPool线程池管理数据服务任务
 *
 * @author liaolonglong
 */
public class ThreadPoolDataServicePool<T> extends AbstractDataServicePool<T> {

    private final ThreadPoolExecutor threadPoolExecutor;


    public ThreadPoolDataServicePool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ThreadPoolDataServicePool(int threads) {
        super(threads);
        this.threadPoolExecutor = new ThreadPoolExecutor(threads, threads, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000),
                (r, executor) -> r.run());
    }

    @Override
    public void invoke(int start, int end, DataTaskManager.LoadDataService loadDataService) throws Exception {
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            tasks.add(new LoadDataCallable(i, loadDataService));
        }

        threadPoolExecutor.invokeAll(tasks);
    }


    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }


    private class LoadDataCallable implements Callable<Void> {
        private final int pageNo;
        private final DataTaskManager.LoadDataService loadDataService;

        public LoadDataCallable(int pageNo, DataTaskManager.LoadDataService loadDataService) {
            this.pageNo = pageNo;
            this.loadDataService = loadDataService;
        }

        @Override
        public Void call() throws Exception {
            loadDataService.loadData(pageNo);
            return null;
        }
    }


}
