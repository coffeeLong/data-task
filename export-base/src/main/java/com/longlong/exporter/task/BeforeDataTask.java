package com.longlong.exporter.task;

public interface BeforeDataTask<T> {
    void exec(T export);
}
