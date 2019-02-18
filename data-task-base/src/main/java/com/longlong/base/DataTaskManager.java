package com.longlong.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 导出任务架构设计，处理大数据报表导出
 * 使用说明：
 * 1.创建对象时指定处理任务的线程池和处理任务类型
 * 2.导出时调用export方法即可
 * 3.当项目停止时调用shutdown
 *
 * @author liaolonglong
 */
public class DataTaskManager<T> {
    private Logger logger = LoggerFactory.getLogger(DataTaskManager.class);

    /**
     * 全局默认导出报表失败显示信息
     */
    private static final String DEFAULT_ERROR_MESSAGE = "导出报表失败";
    /**
     * 全局默认记录导出报表异常信息
     */
    private static final String DEFAULT_LOG_ERROR_MESSAGE = "导出报表异常";

    /**
     * 默认每页加载记录数
     */
    private static final int DEFAULT_PAGE_SIZE = 10000;

    /**
     * 默认一次任务处理的页数
     */
    private static final int DEFAULT_TASK_SIZE = 16;

    /**
     * 缓存一个任务读取的所有页数数据
     */
    protected final ConcurrentMap<Integer, Object> CACHE = new ConcurrentSkipListMap<>(Integer::compareTo);

    /**
     * 临时缓存一个任务加载的排序后数据
     */
    protected final List CACHE_SORTED_VALUES = new ArrayList(DEFAULT_TASK_SIZE);

    /**
     * 异步调用线程池最大并发数
     */
    private static final int ASYNC_THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * 异步调用线程池
     */
    private final ExecutorService asyncExecutorService = new ThreadPoolExecutor(ASYNC_THREADS, ASYNC_THREADS, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), (r, executor) -> r.run());

    /**
     * 默认导出任务配置属性，用作全局配置
     */
    private final DataTaskConfig<T> defaultConfig;

    /**
     * 导出任务线程池
     */
    private final DataServicePool<T> dataServicePool;


    DataTaskManager(DataTaskConfig<T> defaultConfig, DataServicePool<T> dataServicePool) {
        this.defaultConfig = defaultConfig;
        this.dataServicePool = dataServicePool;

        this.dataServicePool.setDataTaskManager(this);

        //加载默认配置属性
        if (Objects.isNull(defaultConfig.getLogErrorMessage())) {
            defaultConfig.setLogErrorMessage(DEFAULT_LOG_ERROR_MESSAGE);
        }
        if (Objects.isNull(defaultConfig.getErrorMessage())) {
            defaultConfig.setErrorMessage(DEFAULT_ERROR_MESSAGE);
        }
        if (Objects.isNull(defaultConfig.getTaskSize())) {
            defaultConfig.setTaskSize(DEFAULT_TASK_SIZE);
        }
        if (Objects.isNull(defaultConfig.getStopDataTask())) {
            defaultConfig.setStopDataTask(false);
        }
    }

    /**
     * 执行任务    使用默认配置
     * @param dataHandler
     * @param dataService
     * @throws DataTaskException
     */
    public void run(T dataHandler, DataService dataService) throws DataTaskException {
        run(dataHandler, dataService, defaultConfig);
    }

    /**
     * 执行任务，主要工作是设置参数、参数校验和控制整个流程，核心导出处理流程由doExport方法实现
     *
     * @param dataHandler 导出处理对象
     * @param dataService
     * @param config
     * @throws DataTaskException
     */
    public void run(T dataHandler, DataService dataService, DataTaskConfig<T> config) throws DataTaskException {

        copyDefaultConfigAndRequireCheck(dataHandler, dataService, config);

        if (Objects.nonNull(config.getStopDataTask()) && false == config.getStopDataTask()) {

            Object[] params = dataService.initParams();

            if (Objects.isNull(config.getCount())) {
                if (config.getTotalTask() == null) {
                    throw new DataTaskException("count属性必须设置");
                }
                dataService.setParamsCount(params, config.getTotalTask().run(params));
            } else {
                dataService.setParamsCount(params, config.getCount());
            }

            if (Objects.nonNull(config.getBeforeHandleTask())) {
                config.getBeforeHandleTask().run(dataHandler);
            }

            // 创建线程池对象
            if (Objects.isNull(config.getPageSize())) {
                if (config.getCount() < DEFAULT_PAGE_SIZE) {
                    dataService.setParamsPageSize(params, config.getCount());
                } else {
                    dataService.setParamsPageSize(params, DEFAULT_PAGE_SIZE);
                }
            } else {
                dataService.setParamsPageSize(params, config.getPageSize());
            }

            //计算总任务数
            int taskTotal = Objects.isNull(config.getTasks()) || config.getTasks() == 0 ? ((config.getCount() / config.getPageSize() + (config.getCount() % config.getPageSize() == 0 ? 0 : 1))) : config.getTasks();

            //存储执行异常
            Set<Throwable> exceptions = new CopyOnWriteArraySet<>();

            //创建加载数据服务对象
            LoadDataService loadDataService = new LoadDataService(dataService, params, dataServicePool.getThreads(), exceptions);

            try {
                doRun(dataHandler, loadDataService, config, taskTotal, exceptions);
            } catch (Exception e) {
                throw new DataTaskException(config.getErrorMessage(), e);
            }

            if (!exceptions.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    exceptions.forEach(e -> logger.error(config.getLogErrorMessage() + " exportService={}", dataService, e));
                }
                throw new DataTaskException(config.getErrorMessage(), exceptions.iterator().next());
            }
        } else {
            if (Objects.nonNull(config.getBeforeHandleTask())) {
                config.getBeforeHandleTask().run(dataHandler);
            }
            config.getDataHandleTask().run(dataHandler, null);
        }
        if (Objects.nonNull(config.getAfterHandleTask())) {
            config.getAfterHandleTask().run(dataHandler);
        }
        // 调用Excel导出写入方法
        config.getCompletedTask().run(dataHandler);
    }

    /**
     * 1.从默认配置获取配置信息
     * 2.校验必要配置属性
     *
     * @param dataService
     * @param dataTaskConfig
     */
    private void copyDefaultConfigAndRequireCheck(T dataHandler, DataService dataService, DataTaskConfig<T> dataTaskConfig) {

        if (Objects.isNull(dataHandler) || Objects.isNull(dataService) || Objects.isNull(dataTaskConfig)) {
            throw new DataTaskException("传入参数dataHandler,dataService,dataTaskConfig不可为空");
        }

        //复制默认属性
        if (dataTaskConfig != defaultConfig) {
            Field[] fields = DataTaskConfig.class.getDeclaredFields();

            for (Field field : fields) {
                try {

                    field.setAccessible(true);

                    if (Objects.isNull(field.get(dataTaskConfig))) {
                        field.set(dataTaskConfig, field.get(defaultConfig));
                    }
                } catch (Exception e) {
                    logger.error("复制默认属性异常field={}", field, e);
                }
            }
        }

        if (Objects.isNull(dataTaskConfig.getDataHandleTask())) {
            throw new DataTaskException("必须设置数据导出任务或设置默认数据导出任务");
        }

        if (Objects.isNull(dataTaskConfig.getCompletedTask())) {
            throw new DataTaskException("必须设置写入任务或设置默认写入任务");
        }


    }

    /**
     * 采用同步锁实现，同一导出任务对象同一时刻只允许一个导出任务执行，减少占用内存和降低风险
     * <p>
     * 实现核心逻辑
     * 1.第一次读取第一批数据,并把读取的数据加入缓存，默认16页
     * 2.把缓存排序后的一批数据转移到CACHE_SORTED_VALUES
     * 3.循环遍历异步加载一批批数据，异步加载数据时对当前主线程解锁
     * 4.同步调用数据处理任务，处理完后对主线程加锁
     *
     * @param dataHandler
     * @param loadDataService
     * @param config
     * @param taskTotal
     * @throws Exception
     */
    private synchronized void doRun(T dataHandler, LoadDataService loadDataService, DataTaskConfig<T> config, int taskTotal, Set<Throwable> exceptions) throws Exception {
        Thread masterThread = Thread.currentThread();

        int taskSize = config.getTaskSize();

        //同步执行第一个任务，并把请求完数据临时缓存
        AtomicInteger start = new AtomicInteger(1), end = new AtomicInteger(taskSize);

        dataServicePool.invoke(start.get(), end.get() > taskTotal ? taskTotal : end.get(), loadDataService);

        retainAllCacheValues();

        //循环执行后面的任务
        for (; start.addAndGet(taskSize) <= taskTotal; ) {

            asyncExecutorService.execute(() -> {

                try {
                    dataServicePool.invoke(start.get(), end.addAndGet(taskSize) > taskTotal ? taskTotal : end.get(), loadDataService);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    LockSupport.unpark(masterThread);
                }
            });

            handleDataTask(dataHandler, config);

            LockSupport.park();

            retainAllCacheValues();

        }

        handleDataTask(dataHandler, config);

    }

    /**
     * 把缓存的数据转移到values
     */
    private void retainAllCacheValues() {
        Iterator<Object> iterator = CACHE.values().iterator();
        for (; iterator.hasNext(); ) {
            CACHE_SORTED_VALUES.add(iterator.next());
            iterator.remove();
        }
    }

    /**
     * 执行数据导出任务
     *
     * @param export
     * @param config
     */
    private void handleDataTask(T export, DataTaskConfig config) {
        CACHE_SORTED_VALUES.forEach(list -> config.getDataHandleTask().run(export, list));
        CACHE_SORTED_VALUES.clear();
    }

    /**
     * 销毁对象时调用
     */
    public void shutdown() {
        dataServicePool.shutdown();
        asyncExecutorService.shutdown();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dataServicePool.shutdown();
                asyncExecutorService.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * 调用导出任务获取数据接口加载数据，并把数据加入缓存
     */
    public class LoadDataService {
        private DataService dataService;
        private ArrayBlockingQueue<Object[]> paramsQueue;
        private Set<Throwable> exceptions;

        private LoadDataService(DataService dataService, Object[] params, int threads, Set<Throwable> exceptions) {
            this.dataService = dataService;
            this.exceptions = exceptions;

            paramsQueue = new ArrayBlockingQueue<>(threads);

            //为每个线程创建一个参数副本
            for (int i = 0; i < threads; i++) {
                paramsQueue.add(Arrays.copyOf(params, params.length));
            }
        }

        /**
         * 加载数据到缓存，缓存把数据排序
         *
         * @param pageNo
         */
        public void loadData(int pageNo) {
            Object[] params = paramsQueue.poll();
            try {
                CACHE.put(pageNo, dataService.getData(params, pageNo));
            } catch (Exception e) {
                exceptions.add(e);
            } finally {
                paramsQueue.add(params);
            }
        }

    }


}
