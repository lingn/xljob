package com.xl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.scheduler.JobScheduler;
import com.xl.job.admin.service.JobInfoService;
import com.xl.job.admin.service.JobTriggerService;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理 Controller
 */
@RestController
@RequestMapping("/jobinfo")
@RequiredArgsConstructor
public class JobInfoController {

    private final JobInfoService jobInfoService;
    private final JobTriggerService jobTriggerService;
    private final JobScheduler jobScheduler;

    @GetMapping("/list")
    public TriggerResponse list() {
        List<JobInfo> list = jobInfoService.list();
        return TriggerResponse.success("查询成功", list);
    }

    @GetMapping("/pageList")
    public TriggerResponse pageList(
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) Integer triggerStatus,
            @RequestParam(required = false) String jobDesc,
            @RequestParam(required = false) String executorHandler,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length) {

        LambdaQueryWrapper<JobInfo> wrapper = new LambdaQueryWrapper<>();
        if (jobGroup != null && !jobGroup.isEmpty()) {
            wrapper.eq(JobInfo::getJobGroup, jobGroup);
        }
        if (triggerStatus != null) {
            wrapper.eq(JobInfo::getStatus, triggerStatus);
        }
        if (jobDesc != null && !jobDesc.isEmpty()) {
            wrapper.like(JobInfo::getJobDesc, jobDesc);
        }
        if (executorHandler != null && !executorHandler.isEmpty()) {
            wrapper.like(JobInfo::getExecutorHandler, executorHandler);
        }
        wrapper.orderByDesc(JobInfo::getId);

        int pageNum = start / length + 1;
        Page<JobInfo> page = jobInfoService.page(new Page<>(pageNum, length), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("recordsTotal", page.getTotal());
        result.put("recordsFiltered", page.getTotal());
        result.put("data", page.getRecords());

        return TriggerResponse.success("查询成功", result);
    }

    @GetMapping("/nextTriggerTime")
    public TriggerResponse nextTriggerTime(@RequestParam String scheduleConf) {
        try {
            // 简单返回当前时间和5个未来时间点
            java.util.List<String> times = new java.util.ArrayList<>();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < 5; i++) {
                times.add(now.plusMinutes(i).format(formatter));
            }
            return TriggerResponse.success("查询成功", times);
        } catch (Exception e) {
            return TriggerResponse.fail("Cron表达式格式错误");
        }
    }

    @PostMapping("/add")
    public TriggerResponse add(@RequestBody JobInfo jobInfo) {
        if (jobInfo.getJobGroup() == null || jobInfo.getJobGroup().isEmpty()) {
            return TriggerResponse.fail("执行器不能为空");
        }
        if (jobInfo.getExecutorHandler() == null || jobInfo.getExecutorHandler().isEmpty()) {
            return TriggerResponse.fail("JobHandler不能为空");
        }
        if (jobInfo.getScheduleConf() == null || jobInfo.getScheduleConf().isEmpty()) {
            return TriggerResponse.fail("Cron表达式不能为空");
        }

        jobInfo.setStatus(0); // 默认停止
        jobInfoService.save(jobInfo);
        return TriggerResponse.success("添加成功");
    }

    @PostMapping("/update")
    public TriggerResponse update(@RequestBody JobInfo jobInfo) {
        if (jobInfo.getId() == null) {
            return TriggerResponse.fail("ID不能为空");
        }

        JobInfo oldJob = jobInfoService.getById(jobInfo.getId());
        if (oldJob == null) {
            return TriggerResponse.fail("任务不存在");
        }

        jobInfoService.updateById(jobInfo);

        // 如果任务是运行状态, 重新调度
        if (oldJob.getStatus() == 1) {
            try {
                jobScheduler.unscheduleJob(oldJob);
                jobScheduler.scheduleJob(jobInfo);
            } catch (Exception e) {
                return TriggerResponse.fail("重新调度失败: " + e.getMessage());
            }
        }

        return TriggerResponse.success("更新成功");
    }

    @PostMapping("/remove")
    public TriggerResponse remove(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id == null) {
            return TriggerResponse.fail("ID不能为空");
        }
        JobInfo jobInfo = jobInfoService.getById(id);
        if (jobInfo != null && jobInfo.getStatus() == 1) {
            return TriggerResponse.fail("请先停止任务");
        }
        jobInfoService.removeById(id);
        return TriggerResponse.success("删除成功");
    }

    @PostMapping("/start")
    public TriggerResponse start(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id == null) {
            return TriggerResponse.fail("ID不能为空");
        }
        JobInfo jobInfo = jobInfoService.getById(id);
        if (jobInfo == null) {
            return TriggerResponse.fail("任务不存在");
        }

        jobInfoService.startJob(id);
        jobInfo.setStatus(1);

        try {
            jobScheduler.scheduleJob(jobInfo);
        } catch (Exception e) {
            return TriggerResponse.fail("启动失败: " + e.getMessage());
        }

        return TriggerResponse.success("启动成功");
    }

    @PostMapping("/stop")
    public TriggerResponse stop(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id == null) {
            return TriggerResponse.fail("ID不能为空");
        }
        JobInfo jobInfo = jobInfoService.getById(id);
        if (jobInfo == null) {
            return TriggerResponse.fail("任务不存在");
        }

        jobInfoService.stopJob(id);

        try {
            jobScheduler.unscheduleJob(jobInfo);
        } catch (Exception e) {
            return TriggerResponse.fail("停止失败: " + e.getMessage());
        }

        return TriggerResponse.success("停止成功");
    }

    @PostMapping("/trigger")
    public TriggerResponse trigger(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        if (id == null) {
            return TriggerResponse.fail("ID不能为空");
        }
        String executorParam = params.get("executorParam") != null ? params.get("executorParam").toString() : null;

        JobInfo jobInfo = jobInfoService.getById(id);
        if (jobInfo == null) {
            return TriggerResponse.fail("任务不存在");
        }

        jobTriggerService.trigger(id, executorParam, null);
        return TriggerResponse.success("触发成功");
    }
}
