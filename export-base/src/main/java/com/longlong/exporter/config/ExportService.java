package com.longlong.exporter.config;

import com.longlong.exporter.exception.ExportException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 导出任务获取数据配置
 *
 * @author liaolonglong
 */
public abstract class ExportService {

    /**
     * 获取数据的对象
     */
    private Object executeObj;

    /**
     * 获取数据的方法,根据方法名获取，如果是重载方法可能获取不准确
     */
    private String executeMethodName;

    /**
     * 获取数据的方法,根据方法名获取，如果是重载方法可能获取不准确
     */
    private Method executeMethod;

    /**
     * 获取数据的方法的参数列表，如果全部是是基本数据类型，务必使用 {@link}setParamsTask重新指定参数
     */
    protected Object[] params;

    private final ConcurrentLinkedQueue<Object[]> paramsQueue = new ConcurrentLinkedQueue<>();


    public ExportService(Object executeObj, String executeMethodName, Object... params) {
        this.executeObj = executeObj;
        this.executeMethodName = executeMethodName;
        this.params = params;
    }


    public abstract void setCount(Object[] params, int count);

    public abstract void setPageNo(Object[] params, int pageNo);

    public abstract void setPageSize(Object[] params, int pageSize);

    public void init(int threads) {
        // 根据方法名找到方法
        for (Method method : executeObj.getClass().getDeclaredMethods()) {
            if (method.getName().equals(executeMethodName)) {
                // 一般情况下只需要方法名不同获取Method就可以了，如果还需要按方法类型判断可取消注释
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == params.length) {
                    boolean sameMethod = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        //校验基本数据类型
                        boolean checkPrimitive = parameterTypes[i].isPrimitive() && (params[i].getClass().getSimpleName().equalsIgnoreCase(parameterTypes[i].getSimpleName())
                                || ("int".equals(parameterTypes[i].getSimpleName()) && params[i].getClass().equals(Integer.class)));
                        boolean checkClass = params[i].getClass() == parameterTypes[i];
                        try {
                            params[i].getClass().asSubclass(parameterTypes[i]);
                        } catch (Exception e) {
                            if (checkPrimitive || checkClass) {
                                continue;
                            }
                            sameMethod = false;
                            break;
                        }
                    }
                    if (sameMethod) {
                        this.executeMethod = method;
                        break;
                    }
                }
            }
        }

        //校验参数
        if (Objects.isNull(executeObj) || Objects.isNull(executeMethod)) {
            throw new ExportException("获取数据对象不可为空");
        }

        //复制参数副本
        for (int i = 0; i < threads; i++) {
            paramsQueue.add(Arrays.copyOf(params, params.length));
        }
    }

    public Object[] getParams() {
        return params;
    }

    public void setPageNo(int pageNo) {
        setPageNo(params, pageNo);
    }

    public void setPageSize(int pageSize) {
        setPageSize(params, pageSize);
    }

    public void setCount(int count) {
        setCount(params, count);
    }

    public Object invoke(int pageNo) throws InvocationTargetException, IllegalAccessException {
        Object[] params = paramsQueue.poll();
        try {
            setPageNo(params, pageNo);
            return executeMethod.invoke(executeObj, params);
        } finally {
            paramsQueue.add(params);
        }
    }

}
