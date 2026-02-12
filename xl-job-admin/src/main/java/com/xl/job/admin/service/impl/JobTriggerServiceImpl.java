package com.xl.job.admin.service.impl;

import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.entity.JobLog;
import com.xl.job.admin.service.JobGroupService;
import com.xl.job.admin.service.JobInfoService;
import com.xl.job.admin.service.JobLogService;
import com.xl.job.admin.service.JobTriggerService;
import com.xl.job.admin.router.Router;
import com.xl.job.admin.router.RouterFactory;
import com.xl.job.admin.client.ExecutorClient;
import com.xl.job.core.enums.RouteStrategyEnum;
import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务触发服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobTriggerServiceImpl implements JobTriggerService {

    private final JobInfoService jobInfoService;
    private final JobGroupService jobGroupService;
    private final JobLogService jobLogService;
    private final ExecutorClient executorClient;

    @Override
    public void trigger(Long jobId) {
        trigger(jobId, null, null);
    }

    @Override
    public void trigger(Long jobId, String executorParam, Integer failRetryCount) {
        JobInfo jobInfo = jobInfoService.getById(jobId);
        if (jobInfo == null) {
            log.warn("任务不存在: jobId={}", jobId);
            return;
        }

        // 覆盖参数
        if (executorParam != null) {
            jobInfo.setExecutorParam(executorParam);
        }
        if (failRetryCount != null) {
            jobInfo.setExecutorFailRetryCount(failRetryCount);
        }

        trigger(jobInfo);
    }

    @Override
    public void trigger(JobInfo jobInfo) {
        // 创建日志
        Long logId = jobLogService.createTriggerLog(jobInfo);
        long logDateTime = System.currentTimeMillis();

        // 获取执行器地址列表
        List<String> addressList = jobGroupService.getAddressList(jobInfo.getJobGroup());
        if (addressList.isEmpty()) {
            String msg = "没有可用的执行器地址";
            log.warn("任务调度失败: jobId={}, msg={}", jobInfo.getId(), msg);
            jobLogService.updateTriggerResult(logId, 500, msg);
            return;
        }

        // 路由选择
        RouteStrategyEnum routeStrategy = RouteStrategyEnum.fromCode(jobInfo.getExecutorRouteStrategy());
        Router router = RouterFactory.getRouter(routeStrategy);
        String address = router.route(addressList, jobInfo.getId());

        if (address == null) {
            String msg = "路由选择失败, 没有可用的执行器";
            log.warn("任务调度失败: jobId={}, msg={}", jobInfo.getId(), msg);
            jobLogService.updateTriggerResult(logId, 500, msg);
            return;
        }

        // 更新日志中的执行器地址
        updateLogAddress(logId, address);

        // 构建触发请求
        TriggerRequest request = TriggerRequest.builder()
                .jobId(jobInfo.getId())
                .executorHandler(jobInfo.getExecutorHandler())
                .executorParams(jobInfo.getExecutorParam())
                .failRetryCount(jobInfo.getExecutorFailRetryCount())
                .blockStrategy(jobInfo.getExecutorBlockStrategy())
                .timeout(jobInfo.getExecutorTimeout())
                .logId(logId)
                .logDateTime(logDateTime)
                .build();

        // 发送触发请求
        TriggerResponse response = executorClient.trigger(address, request);

        // 更新调度结果
        jobLogService.updateTriggerResult(logId, response.getCode(), response.getMsg());
        log.info("任务调度完成: jobId={}, address={}, code={}", jobInfo.getId(), address, response.getCode());
    }

    private void updateLogAddress(Long logId, String address) {
        JobLog jobLog = new JobLog();
        jobLog.setId(logId);
        jobLog.setExecutorAddress(address);
        jobLogService.updateById(jobLog);
    }
}
