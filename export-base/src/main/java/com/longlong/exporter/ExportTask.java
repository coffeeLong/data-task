package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;
import com.longlong.exporter.exception.ExportException;
import com.longlong.exporter.task.ExportTaskPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 导出任务，支持大数据报表导出
 *
 * @author liaolonglong
 */
public class ExportTask<T> {
    private Logger logger = LoggerFactory.getLogger(ExportTask.class);

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
    private final ExportTaskConfig<T> defaultConfig;

    /**
     * 导出任务线程池
     */
    private final ExportTaskPool<T> exportTaskPool;


    ExportTask(ExportTaskConfig<T> defaultConfig, ExportTaskPool<T> exportTaskPool) {
        this.defaultConfig = defaultConfig;
        this.exportTaskPool = exportTaskPool;

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
     * 执行导出任务，主要工作是设置参数、参数校验和控制整个流程，核心导出处理流程由doExport方法实现
     *
     * @param export        导出处理对象
     * @param exportService
     * @param config
     * @throws ExportException
     */
    public void export(T export, ExportService exportService, ExportTaskConfig<T> config) throws ExportException {

        copyDefaultConfigAndRequireCheck(exportService, config);

        if (Objects.nonNull(config.getStopDataTask()) && false == config.getStopDataTask()) {
            if (Objects.isNull(exportService.getExecuteObj()) || Objects.isNull(exportService.getExecuteMethod()) || Objects.isNull(export)) {
                throw new ExportException("ExportExcelEntity对象的executeClass、executeMethod、exportExcel不可为空");
            }

            if (Objects.isNull(config.getCount())) {
                if (config.getCountTask() == null) {
                    throw new ExportException("count属性必须设置");
                }
                config.setCount(config.getCountTask().exec(exportService.getParams()));
            }

            if (Objects.nonNull(config.getBeforeDataTask())) {
                config.getBeforeDataTask().exec(export);
            }

            // 创建线程池对象
            if (Objects.isNull(config.getPageSize())) {
                if (config.getCount() < DEFAULT_PAGE_SIZE) {
                    config.setPageSize(config.getCount());
                } else {
                    config.setPageSize(DEFAULT_PAGE_SIZE);
                }
            }
            int taskTotal = Objects.isNull(config.getTasks()) || config.getTasks() == 0 ? ((config.getCount() / config.getPageSize() + (config.getCount() % config.getPageSize() == 0 ? 0 : 1))) : config.getTasks();

            //存储执行异常
            Set<Throwable> exceptions = new CopyOnWriteArraySet<>();

            try {
                doExport(export, exportService, config, taskTotal, exceptions);
            } catch (Exception e) {
                throw new ExportException(config.getErrorMessage(), e);
            }

            if (!exceptions.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    exceptions.forEach(e -> logger.error(config.getLogErrorMessage() + " exportService={}", exportService, e));
                }
                throw new ExportException(config.getErrorMessage(), exceptions.iterator().next());
            }
        } else {
            if (Objects.nonNull(config.getBeforeDataTask())) {
                config.getBeforeDataTask().exec(export);
            }
            config.getDataTask().exec(export, null);
        }
        if (Objects.nonNull(config.getAfterDataTask())) {
            config.getAfterDataTask().exec(export);
        }
        // 调用Excel导出写入方法
        config.getWriteTask().run(export);
    }

    /**
     * 1.从默认配置获取配置信息
     * 2.校验必要配置属性
     *
     * @param exportService
     * @param config
     */
    private void copyDefaultConfigAndRequireCheck(ExportService exportService, ExportTaskConfig config) {

        Field[] fields = ExportTaskConfig.class.getDeclaredFields();

        for (Field field : fields) {
            try {

                field.setAccessible(true);

                if (Objects.isNull(field.get(config))) {
                    field.set(config, field.get(defaultConfig));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Objects.isNull(config.getDataTask())) {
            throw new ExportException("必须设置数据导出任务或设置默认数据导出任务");
        }

        if (Objects.isNull(config.getWriteTask())) {
            throw new ExportException("必须设置写入任务或设置默认写入任务");
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
     * @param export
     * @param exportService
     * @param config
     * @param taskTotal
     * @param exceptions
     * @throws InterruptedException
     */
    private synchronized void doExport(T export, ExportService exportService, ExportTaskConfig<T> config, int taskTotal, Set<Throwable> exceptions) throws InterruptedException {
        Thread masterThread = Thread.currentThread();

        int taskSize = config.getTaskSize();

        //同步执行第一个任务，并把请求完数据临时缓存
        AtomicInteger start = new AtomicInteger(1), end = new AtomicInteger(taskSize);

        exportTaskPool.invoke(start.get(), end.get() > taskTotal ? taskTotal : end.get(), export, exportService, config, CACHE, exceptions);

        retainAllCacheValues();

        //循环执行后面的任务
        for (; start.addAndGet(taskSize) <= taskTotal; ) {

            asyncExecutorService.execute(() -> {

                exportTaskPool.invoke(start.get(), end.addAndGet(taskSize) > taskTotal ? taskTotal : end.get(), export, exportService, config, CACHE, exceptions);

                LockSupport.unpark(masterThread);

            });

            handleDataTask(export, config);

            LockSupport.park();

            retainAllCacheValues();

        }

        handleDataTask(export, config);

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
    private void handleDataTask(T export, ExportTaskConfig config) {
        CACHE_SORTED_VALUES.forEach(list -> config.getDataTask().exec(export, list));
        CACHE_SORTED_VALUES.clear();
    }

    public void shutdown() {
        exportTaskPool.shutdown();
        asyncExecutorService.shutdown();
    }

}
