package com.xl.job.executor.registry;

import com.xl.job.executor.annotations.XxlJob;
import com.xl.job.executor.processor.DefaultJobProcessor;
import com.xl.job.executor.processor.JobProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 任务注册扫描器 - 扫描 @XxlJob 注解并注册
 */
@Slf4j
public class JobRegistryScanner implements SmartInstantiationAwareBeanPostProcessor {

    private final JobProcessorFactory jobProcessorFactory;

    public JobRegistryScanner(JobProcessorFactory jobProcessorFactory) {
        this.jobProcessorFactory = jobProcessorFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Map<Method, XxlJob> annotatedMethods = MethodIntrospector.selectMethods(
                bean.getClass(),
                (MethodIntrospector.MetadataLookup<XxlJob>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class)
        );

        for (Map.Entry<Method, XxlJob> entry : annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            XxlJob annotation = entry.getValue();
            String jobName = annotation.value();

            if (jobName.isEmpty()) {
                log.warn("任务名称不能为空, bean: {}, method: {}", beanName, method.getName());
                continue;
            }

            DefaultJobProcessor processor = new DefaultJobProcessor(bean, method);
            jobProcessorFactory.register(jobName, processor);
            log.info("注册任务: {} -> {}.{}", jobName, bean.getClass().getName(), method.getName());
        }

        return bean;
    }
}
