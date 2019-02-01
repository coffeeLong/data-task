package com.longlong.exporter.task;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 获取数据任务类
 *
 * @author liaolonglong
 */
public class DataCallable<T> implements Callable<Object> {
    private final int pageNo;
    private final ExportService exportService;
    private final ExportTaskConfig<T> config;

    public DataCallable(int pageNo, ExportService exportService, ExportTaskConfig<T> config) {
        this.pageNo = pageNo;
        this.exportService = exportService;
        this.config = config;
        System.out.println(pageNo);
    }

    @Override
    public Object call() throws Exception {

        Object[] params = exportService.getParams();

        Object[] newParams = new Object[params.length];
        for (int i = 0; i < newParams.length; i++) {
            try {
                newParams[i] = params[i].getClass().newInstance();
                for (Field field : params[i].getClass().getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        field.set(newParams[i], field.get(params[i]));
                    } catch (Exception e) {
                        // 常量属性不允许赋值
                    }
                }
            } catch (InstantiationException e) {
                // 基本数据类型不允许使用newInstance创建对象
                newParams[i] = params[i];
            }
        }
        if (Objects.nonNull(config.getParamsTask())) {
            config.getParamsTask().exec(newParams, pageNo, config.getPageSize(), config.getCount());
        }
        try {
            return exportService.getExecuteMethod().invoke(exportService.getExecuteObj(), newParams);
        } catch (Exception e) {
            throw e;
        }
    }
}