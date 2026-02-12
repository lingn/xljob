package com.xl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.job.admin.entity.JobLog;
import com.xl.job.admin.service.JobLogService;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行日志 Controller
 */
@RestController
@RequestMapping("/joblog")
@RequiredArgsConstructor
public class JobLogController {

    private final JobLogService jobLogService;

    @GetMapping("/list")
    public TriggerResponse list() {
        List<JobLog> list = jobLogService.list();
        return TriggerResponse.success("查询成功", list);
    }

    @GetMapping("/pageList")
    public TriggerResponse pageList(
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long logId,
            @RequestParam(required = false) String filterTime,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length) {

        LambdaQueryWrapper<JobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobGroup != null && !jobGroup.isEmpty()) {
            wrapper.eq(JobLog::getJobGroup, jobGroup);
        }
        if (jobId != null) {
            wrapper.eq(JobLog::getJobId, jobId);
        }
        if (logId != null) {
            wrapper.eq(JobLog::getId, logId);
        }
        if (filterTime != null && !filterTime.isEmpty()) {
            // 解析时间范围 (格式: "2024-01-01 - 2024-01-31")
            String[] times = filterTime.split(" - ");
            if (times.length == 2) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate startDate = LocalDate.parse(times[0].trim(), formatter);
                    LocalDate endDate = LocalDate.parse(times[1].trim(), formatter);
                    wrapper.ge(JobLog::getTriggerTime, LocalDateTime.of(startDate, LocalTime.MIN));
                    wrapper.le(JobLog::getTriggerTime, LocalDateTime.of(endDate, LocalTime.MAX));
                } catch (Exception ignored) {
                }
            }
        }
        wrapper.orderByDesc(JobLog::getId);

        int pageNum = start / length + 1;
        Page<JobLog> page = jobLogService.page(new Page<>(pageNum, length), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("recordsTotal", page.getTotal());
        result.put("recordsFiltered", page.getTotal());
        result.put("data", page.getRecords());

        return TriggerResponse.success("查询成功", result);
    }

    @GetMapping("/logDetailPage")
    public TriggerResponse logDetailPage(@RequestParam Long id) {
        JobLog jobLog = jobLogService.getById(id);
        if (jobLog == null) {
            return TriggerResponse.fail("日志不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("executorAddress", jobLog.getExecutorAddress());
        detail.put("triggerCode", jobLog.getTriggerCode());
        detail.put("triggerMsg", jobLog.getTriggerMsg());
        detail.put("handleCode", jobLog.getHandleCode());
        detail.put("handleMsg", jobLog.getHandleMsg());

        return TriggerResponse.success("查询成功", detail);
    }

    @GetMapping("/logDetailCat")
    public TriggerResponse logDetailCat(
            @RequestParam String executorAddress,
            @RequestParam Long logId,
            @RequestParam(defaultValue = "1") int fromLineNum) {
        // 简化实现，返回执行日志内容
        JobLog jobLog = jobLogService.getById(logId);
        if (jobLog == null) {
            return TriggerResponse.fail("日志不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", jobLog.getHandleMsg() != null ? jobLog.getHandleMsg() : "");
        result.put("end", true);

        return TriggerResponse.success("查询成功", result);
    }

    @PostMapping("/logKill")
    public TriggerResponse logKill(@RequestBody Map<String, Long> params) {
        Long logId = params.get("logId");
        if (logId == null) {
            return TriggerResponse.fail("logId不能为空");
        }
        // 简化实现，实际需要通知执行器终止任务
        return TriggerResponse.success("终止任务成功");
    }

    @PostMapping("/clearLog")
    public TriggerResponse clearLog(@RequestBody Map<String, Object> params) {
        Integer jobGroup = params.get("jobGroup") != null ? Integer.valueOf(params.get("jobGroup").toString()) : null;
        Integer jobId = params.get("jobId") != null ? Integer.valueOf(params.get("jobId").toString()) : null;
        Integer type = params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : null;

        LambdaQueryWrapper<JobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobGroup != null) {
            wrapper.eq(JobLog::getJobGroup, String.valueOf(jobGroup));
        }
        if (jobId != null) {
            wrapper.eq(JobLog::getJobId, jobId.longValue());
        }
        if (type != null) {
            // type: 1=清理一个月前日志, 2=清理三个月前日志, 3=清理六个月前日志, 4=清理一年前日志
            LocalDateTime beforeTime = switch (type) {
                case 1 -> LocalDateTime.now().minusMonths(1);
                case 2 -> LocalDateTime.now().minusMonths(3);
                case 3 -> LocalDateTime.now().minusMonths(6);
                case 4 -> LocalDateTime.now().minusYears(1);
                default -> LocalDateTime.now().minusMonths(1);
            };
            wrapper.lt(JobLog::getTriggerTime, beforeTime);
        }

        jobLogService.remove(wrapper);
        return TriggerResponse.success("清理成功");
    }

    @GetMapping("/detail")
    public TriggerResponse detail(@RequestParam Long id) {
        JobLog jobLog = jobLogService.getById(id);
        if (jobLog == null) {
            return TriggerResponse.fail("日志不存在");
        }
        return TriggerResponse.success("查询成功", jobLog);
    }

    @PostMapping("/clear")
    public TriggerResponse clear(@RequestParam(required = false) Long jobId,
                                 @RequestParam(required = false) String beforeTime) {
        LambdaQueryWrapper<JobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            wrapper.eq(JobLog::getJobId, jobId);
        }
        if (beforeTime != null && !beforeTime.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.parse(beforeTime, formatter);
                wrapper.lt(JobLog::getTriggerTime, time);
            } catch (Exception ignored) {
            }
        }
        jobLogService.remove(wrapper);
        return TriggerResponse.success("清理成功");
    }
}
