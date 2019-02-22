package com.dragon.excel;

import com.dragon.excel.entity.Page;
import com.dragon.excel.entity.TestEntity;
import com.dragon.excel.entity.TestParam;
import com.dragon.excel.service.TestService;
import com.longlong.base.*;
import com.longlong.excel.ExportExcelBuilder;
import com.longlong.excel.exporter.AbstractExportExcel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportTest {

    /**
     * 获取数据执行任务对象
     */
    private static TestService service;

    /**
     * 获取数据执行任务方法参数
     */
    private static TestParam param;

    private static DataTaskManager<AbstractExportExcel> dataTaskManager;

    @BeforeClass
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

    private void export(DataTaskConfig<AbstractExportExcel> config) {
        export(config, ExportExcelBuilder.build("测试导出", TestEntity.class));
    }

    private void export(DataTaskConfig<AbstractExportExcel> config, AbstractExportExcel exportExcel) {
        export(config, exportExcel,
                new DataService() {
                    @Override
                    public Object[] initParams() {
                        return new Object[]{param};
                    }

                    @Override
                    public Object getData(Object[] params, int pageNo) {
                        TestParam param = (TestParam) params[0];
                        param.setPageNo(pageNo);
                        return service.findList(param);
                    }

                    @Override
                    public void setParamsPageSize(Object[] params, int pageSize) {
                        ((TestParam) params[0]).setPageSize(pageSize);
                    }

                    @Override
                    public void setParamsCount(Object[] params, int count) {
                        ((TestParam) params[0]).setCount(count);
                    }
                }
        );
    }

    private void export(DataTaskConfig<AbstractExportExcel> config, AbstractExportExcel exportExcel, DataService dataService) {
        long start = System.currentTimeMillis();
        DataTaskBuilder.run(dataTaskManager, config, exportExcel, dataService);
        System.out.println(System.currentTimeMillis() - start);
    }


    /**
     * 使用注解导出报表
     *
     * @throws DataTaskException
     */
    @Test
    public void exportAnno1() throws DataTaskException {
        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();
        config.setLogErrorMessage("测试导出报表导出异常")
                .setErrorMessage("导出报表失败")
                .setAfterHandleTask(export -> {
                    int rowNumber = export.getRownum();
                    export.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    export.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                });
        export(config);
    }

    /**
     * 使用注解导出报表
     *
     * @throws DataTaskException
     */
    @Test
    public void exportAnno2() throws DataTaskException {
        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();

        config.setLogErrorMessage("测试导出报表导出异常")
                .setAfterHandleTask(export -> {
                    int rowNumber = export.getRownum();
                    export.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    export.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                }).setPageSize(200).setCount(param.getCount());

        export(config,
                ExportExcelBuilder.build("测试导出", TestEntity.class),
                new DataService() {
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

    /**
     * 通过配置属性以map方式导出
     *
     * @throws DataTaskException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void exportFieldMap() throws DataTaskException {
        AtomicInteger i = new AtomicInteger(1);
        // 设置默认获取总记录数任务
        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();
        config.setDataHandleTask((export, data) -> {
            Map<String, List<TestEntity>> map = new HashMap<>();
            map.put("Export" + i.getAndIncrement(), (List<TestEntity>) data);
            export.setDataMap(map);
        }).setPageSize(500);

        export(config, ExportExcelBuilder.buildEx("测试导出")
                .addExcelField("id", "ID")
                .addExcelField("cityPos", "销售城市")
                .addExcelField("countRequests", "申请团队")
                .addExcelField("countPassedRequests", "证实团队")
                .addExcelField("countCanceledRequests", "撤回团队"));
    }


    /**
     * 以map方式导出多个sheet
     *
     * @throws DataTaskException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void exportMapAnno2() throws DataTaskException {

        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();

        config.setDataHandleTask((exportExcel, data) -> {
            exportExcel.setDataMap((Map<String, List<TestEntity>>) data);
        }).setPageSize(500);

        export(config, ExportExcelBuilder.build("测试导出", TestEntity.class), new DataService() {
            @Override
            public Object[] initParams() {
                return new Object[]{param};
            }

            @Override
            public Object getData(Object[] params, int pageNo) {
                return service.findListMap(param);
            }

            @Override
            public void setParamsPageSize(Object[] params, int pageSize) {
                ((TestParam) params[0]).setPageSize(pageSize);
            }

            @Override
            public void setParamsCount(Object[] params, int count) {
                ((TestParam) params[0]).setCount(count);
            }
        });

    }

    /**
     * 添加属性导出Excel
     *
     * @throws DataTaskException
     */
    @Test
    public void exportField() throws DataTaskException {

        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();

        export(config,
                ExportExcelBuilder
                        .buildEx("测试导出").addExcelField("id", "ID")
                        .addExcelField("cityPos", "销售城市")
                        .addExcelField("countRequests", "申请团队")
                        .addExcelField("countPassedRequests", "证实团队")
                        .addExcelField("countCanceledRequests", "撤回团队"));

    }

    /**
     * 大数据导出Excel
     *
     * @throws DataTaskException
     */
    @Test
    public void exportBigData() throws DataTaskException {
        // 设置默认获取总记录数任务
        int count = 2000000;
        DataTaskConfig<AbstractExportExcel> config = DataTaskBuilder.buildTaskConfig();

        config.setLogErrorMessage("测试导出报表导出异常")
                .setAfterHandleTask(run -> {
                    int rowNumber = run.getRownum();
                    run.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    run.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                })
                .setPageSize(500)
                .setTaskSize(32)
                .setCount(count);

        export(config,
                ExportExcelBuilder.build("测试导出", TestEntity.class),
                new DataService() {
                    @Override
                    public Object[] initParams() {
                        return new Object[3];
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


    /**
     * 多线程环境测试导出
     *
     * @throws InterruptedException
     */
    @Test
    public void exportMultiThread() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        CountDownLatch countDownLatch = new CountDownLatch(5);

        executorService.execute(() -> {
            exportAnno1();
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            exportAnno2();
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            exportFieldMap();
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            exportMapAnno2();
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            exportField();
            countDownLatch.countDown();
        });

        countDownLatch.await();

        executorService.shutdown();
    }

}
