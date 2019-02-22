package com.longlong.base;

/**
 * 管理加载数据的线程池
 *
 * @author liaolonglong
 */
public interface DataServicePool<T> {

    /**
     * 获取并发加载数据线程数
     *
     * @return
     */
    int getThreads();

    /**
     * 同步完成start到end间的所有任务,并把每个子任务获取的数据添加到缓存
     *
     * @param start           导出任务的起始页
     * @param end             导出任务的结束页
     * @param loadDataService 导出任务获取数据参数
     * @throws Exception 抛出异常
     */
    void invoke(int start, int end, DataTaskManager.LoadDataService loadDataService) throws Exception;

    /**
     * 导出任务线程池停止时调用
     */
    void shutdown();

}
