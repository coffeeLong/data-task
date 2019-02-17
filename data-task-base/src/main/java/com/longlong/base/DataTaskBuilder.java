package com.longlong.base;

/**
 * 导出任务构建器
 *
 * @author liaolonglong
 */
public class DataTaskBuilder {

    /**
     * 构建导出任务配置信息
     *
     * @param <T>
     * @return
     */
    public static <T> DataTaskConfig<T> buildTaskConfig() {
        return new DataTaskConfig<>();
    }

    /**
     * 构建数据任务管理对象
     *
     * @param defaultConfig   默认配置
     * @param dataServicePool 数据服务管理线程池
     * @param <T>             数据任务处理对象类型
     * @return 导出任务
     */
    public static <T> DataTaskManager<T> buildTaskManager(DataTaskConfig<T> defaultConfig, DataServicePool<T> dataServicePool) {
        return new DataTaskManager<>(defaultConfig, dataServicePool);
    }


    /**
     * 构建数据任务管理对象并执行任务
     *
     * @param dataTaskManager 数据任务管理对象
     * @param config          数据任务属性配置
     * @param dataHandler          数据任务处理对象
     * @param dataService     数据服务接口
     * @param <T>             数据任务处理对象类型
     */
    public static <T> void run(DataTaskManager<T> dataTaskManager, DataTaskConfig<T> config, T dataHandler, DataService dataService) {
        dataTaskManager.run(dataHandler, dataService, config);
    }

}
