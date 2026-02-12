package com.xl.job.executor.annotations;

import java.lang.annotation.*;

/**
 * XxlJob 注解 - 用于标记任务处理方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface XxlJob {

    /**
     * 任务Handler名称
     */
    String value();
}
