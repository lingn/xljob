package com.xl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xl.job.admin.entity.JobGroup;
import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.entity.JobLog;
import com.xl.job.admin.service.JobGroupService;
import com.xl.job.admin.service.JobInfoService;
import com.xl.job.admin.service.JobLogService;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 仪表盘 Controller
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JobGroupService jobGroupService;
    private final JobInfoService jobInfoService;
    private final JobLogService jobLogService;

    @GetMapping("/info")
    public TriggerResponse info() {
        DashboardInfo dashboardInfo = new DashboardInfo();

        // 执行器数量
        dashboardInfo.setJobGroupCount(jobGroupService.count());

        // 任务数量
        dashboardInfo.setJobInfoCount(jobInfoService.count());

        // 运行中任务数量
        LambdaQueryWrapper<JobInfo> runningWrapper = new LambdaQueryWrapper<>();
        runningWrapper.eq(JobInfo::getStatus, 1);
        dashboardInfo.setRunningJobCount(jobInfoService.count(runningWrapper));

        // 今日调度次数
        LambdaQueryWrapper<JobLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(JobLog::getTriggerTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        dashboardInfo.setTodayLogCount(jobLogService.count(todayWrapper));

        // 总调度次数
        dashboardInfo.setJobLogCount(jobLogService.count());

        return TriggerResponse.success("查询成功", dashboardInfo);
    }

    @GetMapping("/chartInfo")
    public TriggerResponse chartInfo(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // 默认最近7天
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(6);

        List<String> triggerDayList = new ArrayList<>();
        List<Integer> triggerCountList = new ArrayList<>();
        List<Integer> triggerCountFailList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            triggerDayList.add(date.format(formatter));

            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            // 当天调度总数
            LambdaQueryWrapper<JobLog> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.ge(JobLog::getTriggerTime, dayStart)
                    .le(JobLog::getTriggerTime, dayEnd);
            int count = (int) jobLogService.count(countWrapper);
            triggerCountList.add(count);

            // 当天失败数
            LambdaQueryWrapper<JobLog> failWrapper = new LambdaQueryWrapper<>();
            failWrapper.ge(JobLog::getTriggerTime, dayStart)
                    .le(JobLog::getTriggerTime, dayEnd)
                    .eq(JobLog::getHandleCode, 500);
            int failCount = (int) jobLogService.count(failWrapper);
            triggerCountFailList.add(failCount);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("triggerDayList", triggerDayList);
        result.put("triggerCountList", triggerCountList);
        result.put("triggerCountFailList", triggerCountFailList);

        return TriggerResponse.success("查询成功", result);
    }

    @Data
    public static class DashboardInfo {
        private long jobGroupCount;
        private long jobInfoCount;
        private long jobLogCount;
        private long todayLogCount;
        private long runningJobCount;
    }
}
