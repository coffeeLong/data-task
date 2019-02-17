package com.dragon.excel;

import com.dragon.excel.entity.Page;
import com.dragon.excel.entity.TestEntity;
import com.dragon.excel.entity.TestParam;
import com.dragon.excel.service.TestService;
import com.longlong.base.*;
import com.longlong.excel.ExportExcelBuilder;
import com.longlong.excel.exporter.AbstractExportExcel;
import com.longlong.exporter.exception.ExportException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExportMainTest {
    /**
     * 获取数据执行任务对象
     */
    private static TestService service;

    /**
     * 获取数据执行任务方法参数
     */
    private static TestParam param;

    private static DataTaskManager<AbstractExportExcel> dataTaskManager;


    public static void main(String[] args) {
        init();

        exportBigData();

        dataTaskManager.shutdown();
    }


    public static void init() {
        service = new TestService();
        param = new TestParam();
        param.setPageNo(1);
        param.setPageSize(50);
        param.setCount(4300);
        /** ＝＝＝＝＝＝＝＝＝＝＝＝设置全局属性或任务 开始＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝ */
        DataServicePool<AbstractExportExcel> dataServicePool = new ThreadPoolDataServicePool<>();
//        ExportTaskPool<AbstractExportExcel> exportTaskPool = new MasterWorkerExportTaskPool<>();
//        ForkJoinExportTaskPool<AbstractExportExcel> exportTaskPool = new ForkJoinExportTaskPool<>();

        //默认配置属性
        DataTaskConfig<AbstractExportExcel> defaultConfig = DataTaskBuilder.buildTaskConfig();

        dataTaskManager = DataTaskBuilder.buildTaskManager(defaultConfig, dataServicePool);

        defaultConfig
                .setDataHandleTask((export, pageData) -> {
                    export.setDataList((List) pageData);
                })
                .setPageSize(200)
                .setCompletedTask((export) -> {
                    // 设置默认写入任务
                    try {
                        String testResources = "src/test/resources";
                        export.write(testResources, export.getFileName());
                        System.out.println("导出Excel行数：" + export.getRownum());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .setTotalTask((params) -> {
                    // 设置默认获取总记录数任务
                    return ((Page<?>) params[0]).getCount();
                });

        /** ＝＝＝＝＝＝＝＝＝＝＝＝设置全局属性或任务 结束＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝ */
    }

    /**
     * 大数据导出Excel
     *
     * @throws DataTaskException
     */
    public static void exportBigData() throws ExportException {
        // 设置默认获取总记录数任务
        int count = 150000;
        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();

        config.setLogErrorMessage("测试导出报表导出异常")
                .setAfterHandleTask(run -> {
                    int rowNumber = run.getRownum();
                    run.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    run.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                })
                .setCount(count);
        export(config, ExportExcelBuilder.build("测试导出", TestEntity.class), new DataService() {
            @Override
            public Object[] initParams() {
                return new Object[]{param.getPageNo(), param.getPageSize(), param.getCount()};
            }

            @Override
            public Object getData(Object[] params, int pageNo) {
                params[0] = pageNo;
                return service.findList((int) params[0], (int) params[1], (int) params[2]);
            }

            @Override
            public void setParamsPageSize(Object[] params, int pageSize) {
                params[1] = pageSize;
            }

            @Override
            public void setParamsCount(Object[] params, int count) {
                params[2] = count;
            }
        });
    }

    private static void export(DataTaskConfig<AbstractExportExcel> config, AbstractExportExcel exportExcel, DataService dataService) {
        long start = System.currentTimeMillis();
        DataTaskBuilder.run(dataTaskManager, config, exportExcel, dataService);
        System.out.println(System.currentTimeMillis() - start);
    }

}
