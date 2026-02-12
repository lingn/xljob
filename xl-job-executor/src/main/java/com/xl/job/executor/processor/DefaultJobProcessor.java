package com.xl.job.executor.processor;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 默认任务处理器 - 基于 @XxlJob 注解方法
 */
@Slf4j
public class DefaultJobProcessor implements JobProcessor {

    private final Object target;
    private final Method method;

    public DefaultJobProcessor(Object target, Method method) {
        this.target = target;
        this.method = method;
        this.method.setAccessible(true);
    }

    @Override
    public String process(String param) throws Exception {
        try {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object result;
            if (paramTypes.length == 0) {
                result = method.invoke(target);
            } else if (paramTypes.length == 1 && paramTypes[0] == String.class) {
                result = method.invoke(target, param);
            } else {
                throw new IllegalArgumentException("不支持的方法参数类型");
            }

            if (result == null) {
                return null;
            }
            return result.toString();
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            log.error("任务执行异常: {}", targetException.getMessage(), targetException);
            throw new Exception(targetException.getMessage(), targetException);
        } catch (Exception e) {
            log.error("任务执行异常: {}", e.getMessage(), e);
            throw e;
        }
    }
}
