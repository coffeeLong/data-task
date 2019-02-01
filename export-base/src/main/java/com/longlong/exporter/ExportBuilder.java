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
     * 构建导出任务配置
     *
     * @param <T>
     * @return
     */
    public static <T> ExportTaskConfig<T> buildConfig() {
        return new ExportTaskConfig<T>();
    }

    /**
     * 构建导出任务服务
     *
     * @param executeObj
     * @param executeMethodName
     * @param params
     * @return
     */
    public static ExportService buildService(Object executeObj, String executeMethodName, Object... params) {
        return new ExportService(executeObj, executeMethodName, params);
    }

    /**
     * 构建导出任务
     * @param exportTask
     * @param config
     * @param export
     * @param executeObj
     * @param executeMethodName
     * @param params
     * @param <T>
     */
    public static <T> void export(ExportTask<T> exportTask, ExportTaskConfig<T> config, T export, Object executeObj, String executeMethodName, Object... params) {
        export(exportTask,config,export,buildService(executeObj,executeMethodName,params));
    }


    /**
     * 构建导出任务
     *
     * @param exportTask
     * @param export
     * @param service
     * @param config
     * @param <T>
     */
    public static <T> void export(ExportTask<T> exportTask, ExportTaskConfig<T> config, T export, ExportService service) {
        exportTask.export(export, service, config);
    }

}
