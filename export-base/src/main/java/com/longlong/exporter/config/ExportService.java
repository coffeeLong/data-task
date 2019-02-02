package com.longlong.exporter.config;

import java.lang.reflect.Method;

/**
 *  导出任务获取数据配置
 * @author liaolonglong
 */
public class ExportService {

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
    private Object[] params;

    public ExportService(Object executeObj, String executeMethodName, Object... params) {
        this.executeObj = executeObj;
        this.executeMethodName = executeMethodName;
        this.params = params;

        init();
    }

    private void init() {
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
    }

    public Object getExecuteObj() {
        return executeObj;
    }

    public void setExecuteObj(Object executeObj) {
        this.executeObj = executeObj;
    }

    public Method getExecuteMethod() {
        return executeMethod;
    }

    public void setExecuteMethod(Method executeMethod) {
        this.executeMethod = executeMethod;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public String getExecuteMethodName() {
        return executeMethodName;
    }

    public void setExecuteMethodName(String executeMethodName) {
        this.executeMethodName = executeMethodName;
    }
}
