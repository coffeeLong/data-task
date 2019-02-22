package com.longlong.base;

/**
 * 管理加载数据的线程池的抽象实现
 *
 * @author liaolonglong
 */

public abstract class AbstractDataServicePool<T> implements DataServicePool<T> {

    private int threads;

    protected AbstractDataServicePool(int threads) {
        this.threads = threads;
    }


    /**
     * 获取并发加载数据线程数
     *
     * @return
     */
    public int getThreads() {
        return threads;
    }

}
