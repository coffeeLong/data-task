package com.longlong.exporter.task;

import com.longlong.exporter.ExportTask;

public abstract class AbstractExportTaskPool<T> implements ExportTaskPool<T> {

    private ExportTask<T> exportTask;

    @Override
    public void exportTask(ExportTask<T> exportTask) {
        if (this.exportTask == null) {
            this.exportTask = exportTask;
        }
    }

    public ExportTask<T> getExportTask() {
        return exportTask;
    }
}
