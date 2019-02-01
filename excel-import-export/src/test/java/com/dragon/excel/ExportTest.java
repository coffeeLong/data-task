package com.dragon.excel;

import com.dragon.excel.entity.Page;
import com.dragon.excel.entity.TestEntity;
import com.dragon.excel.entity.TestParam;
import com.dragon.excel.service.TestService;
import com.longlong.excel.ExportExcelBuilder;
import com.longlong.excel.exporter.AbstractExportExcel;
import com.longlong.exporter.ExportBuilder;
import com.longlong.exporter.ExportTask;
import com.longlong.exporter.ForkJoinExportTaskPool;
import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;
import com.longlong.exporter.exception.ExportException;
import com.longlong.exporter.task.ExportTaskPool;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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



    private static ExportTask<AbstractExportExcel> exportTask;

    @BeforeClass
    public static void init() {
        service = new TestService();
        param = new TestParam();
        param.setPageNo(1);
        param.setPageSize(50);
        param.setCount(4300);
        /** ＝＝＝＝＝＝＝＝＝＝＝＝设置全局属性或任务 开始＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝ */
        ExportTaskPool<AbstractExportExcel> forkJoinExportTaskPool = new ForkJoinExportTaskPool<>();
        exportTask = new ExportTask<>(forkJoinExportTaskPool);

        exportTask.getDefaultConfig()
                .setDataTask((export, pageData) -> {
                    export.setDataList((List) pageData);
                })
                .setWriteTask((export) -> {
                    // 设置默认写入任务
                    try {
                        String testResources = "src/test/resources";
                        export.write(testResources, export.getFileName());
                        System.out.println("导出Excel行数：" + export.getRownum());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .setParamsTask((params, pageNo, pageSize, count) -> {
                    // 设置默认获取数据参数任务
                    Page<?> page = (Page<?>) params[0];
                    page.setCount(count);
                    page.setPageNo(pageNo);
                    page.setPageSize(pageSize);
                })
                .setCountTask((params) -> {
                    // 设置默认获取总记录数任务
                    return ((Page<?>) params[0]).getCount();
                });

        /** ＝＝＝＝＝＝＝＝＝＝＝＝设置全局属性或任务 结束＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝ */
    }

    private void export(ExportTaskConfig<AbstractExportExcel> config) {
        export(config, ExportExcelBuilder.build("测试导出", TestEntity.class));
    }

    private void export(ExportTaskConfig<AbstractExportExcel> config, AbstractExportExcel exportExcel) {
        export(config, exportExcel, ExportBuilder.buildService(service, "findList", param));
    }

    private void export(ExportTaskConfig<AbstractExportExcel> config, AbstractExportExcel exportExcel, ExportService exportService) {
        long start = System.currentTimeMillis();
        ExportBuilder.export(exportTask, config, exportExcel, exportService);
        System.out.println(System.currentTimeMillis() - start);
    }


    /**
     * <p>
     *
     * @throws ExportException
     */
    @Test
    public void exportAnno1() throws ExportException {
        ExportTaskConfig<AbstractExportExcel> config = ExportBuilder.buildConfig();
        config.setLogErrorMessage("测试导出报表导出异常")
                .setErrorMessage("导出报表失败")
                .setAfterDataTask(exportExcel -> {
                    int rowNumber = exportExcel.getRownum();
                    exportExcel.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    exportExcel.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                })
        .setMaxThread(2);
        export(config);
    }

    @Test
    public void exportAnno2() throws ExportException {
        ExportTaskConfig<AbstractExportExcel> config = ExportBuilder.buildConfig();

        config.setLogErrorMessage("测试导出报表导出异常")
                .setAfterDataTask(exporter -> {
                    AbstractExportExcel exportExcel = (AbstractExportExcel) exporter;
                    int rowNumber = exportExcel.getRownum();
                    exportExcel.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    exportExcel.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                }).setPageSize(200).setCount(param.getCount()).setParamsTask((params, pageNo, pageSize, count) -> {
            params[0] = pageNo;
            params[1] = pageSize;
            params[2] = count;
        });

        export(config,
                ExportExcelBuilder.build("测试导出", TestEntity.class),
                ExportBuilder.buildService(service, "findList", param.getPageNo(), param.getPageSize(), param.getCount()));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void exportMapAnno1() throws ExportException {
        AtomicInteger i = new AtomicInteger(1);
        // 设置默认获取总记录数任务
        ExportTaskConfig<AbstractExportExcel> config = ExportBuilder.buildConfig();
        config.setDataTask((export, data) -> {
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

    @SuppressWarnings("unchecked")
    @Test
    public void exportMapAnno2() throws ExportException {

        ExportTaskConfig<AbstractExportExcel> config = ExportBuilder.buildConfig();

        config.setDataTask((exportExcel, data) -> {
            exportExcel.setDataMap((Map<String, List<TestEntity>>) data);
        }).setPageSize(500);

        export(config,
                ExportExcelBuilder.build("测试导出", TestEntity.class),
                ExportBuilder.buildService(service, "findListMap", param));

    }

    @Test
    public void exportField() throws ExportException {

        ExportTaskConfig<AbstractExportExcel> config = ExportBuilder.buildConfig();

        export(config,
                ExportExcelBuilder
                        .buildEx("测试导出").addExcelField("id", "ID")
                        .addExcelField("cityPos", "销售城市")
                        .addExcelField("countRequests", "申请团队")
                        .addExcelField("countPassedRequests", "证实团队")
                        .addExcelField("countCanceledRequests", "撤回团队"));

    }

    @Test
    public void exportBigData() throws ExportException {
        // 设置默认获取总记录数任务
        int count = 80000;
        ExportTaskConfig<AbstractExportExcel> config = ExportBuilder.buildConfig();

        config.setLogErrorMessage("测试导出报表导出异常")
                .setAfterDataTask(export -> {
                    int rowNumber = export.getRownum();
                    export.addCell(++rowNumber, 2, new String[]{"生成人", "超级管理员"}, 2);
                    export.addCell(++rowNumber, 2, new String[]{"生成日期：", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, 2);
                })
                .setPageSize(50)
                .setCount(count)
                .setParamsTask((params, pageNo, pageSize, paramCount) -> {
                    params[0] = pageNo;
                    params[1] = pageSize;
                    params[2] = paramCount;
                });

        export(config,
                ExportExcelBuilder.build("测试导出", TestEntity.class),
                ExportBuilder.buildService(service, "findList", param.getPageNo(), param.getPageSize(), param.getCount()));


    }

}
