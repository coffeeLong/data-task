package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;

/**
 * 导出任务构建器
 *
 * @author liaolonglong
 */
public class ExportBuilder {

    /**
     * 构建导出任务配置信息
     *
     * @param <T>
     * @return
     */
    public static <T> ExportTaskConfig<T> buildConfig() {
        return new ExportTaskConfig<>();
    }

    /**
     * 构建导出任务对象
     *
     * @param defaultConfig  默认配置
     * @param exportTaskPool 导出任务线程池
     * @param <T>            导出任务数据处理对象类型
     * @return 导出任务
     */
    public static <T> ExportTask<T> buildTask(ExportTaskConfig<T> defaultConfig, ExportTaskPool<T> exportTaskPool) {
        return new ExportTask<>(defaultConfig, exportTaskPool);
    }

    /**
     * 构建并执行导出任务
     *
     * @param exportTask 导出任务对象
     * @param config     导出任务配置
     * @param export     导出任务数据处理对象
     * @param service    导出任务获取数据服务配置
     * @param <T>        导出任务数据处理对象类型
     */
    public static <T> void export(ExportTask<T> exportTask, ExportTaskConfig<T> config, T export, ExportService service) {
        exportTask.export(export, service, config);
    }

}
