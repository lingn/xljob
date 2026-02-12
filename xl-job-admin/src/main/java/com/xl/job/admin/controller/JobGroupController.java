package com.xl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.job.admin.entity.JobGroup;
import com.xl.job.admin.service.JobGroupService;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行器组管理 Controller
 */
@RestController
@RequestMapping("/jobgroup")
@RequiredArgsConstructor
public class JobGroupController {

    private final JobGroupService jobGroupService;

    @GetMapping("/list")
    public TriggerResponse list() {
        List<JobGroup> list = jobGroupService.list();
        return TriggerResponse.success("查询成功", list);
    }

    @GetMapping("/pageList")
    public TriggerResponse pageList(
            @RequestParam(required = false) String appname,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length) {

        LambdaQueryWrapper<JobGroup> wrapper = new LambdaQueryWrapper<>();
        if (appname != null && !appname.isEmpty()) {
            wrapper.like(JobGroup::getAppName, appname);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like(JobGroup::getTitle, title);
        }

        int pageNum = start / length + 1;
        Page<JobGroup> page = jobGroupService.page(new Page<>(pageNum, length), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("recordsTotal", page.getTotal());
        result.put("recordsFiltered", page.getTotal());
        result.put("data", page.getRecords());

        return TriggerResponse.success("查询成功", result);
    }

    @GetMapping("/all")
    public TriggerResponse all() {
        List<JobGroup> list = jobGroupService.list();
        return TriggerResponse.success("查询成功", list);
    }

    @PostMapping("/add")
    public TriggerResponse add(@RequestBody JobGroup jobGroup) {
        if (jobGroup.getAppName() == null || jobGroup.getAppName().isEmpty()) {
            return TriggerResponse.fail("AppName不能为空");
        }
        if (jobGroup.getTitle() == null || jobGroup.getTitle().isEmpty()) {
            return TriggerResponse.fail("标题不能为空");
        }

        JobGroup exists = jobGroupService.getByAppName(jobGroup.getAppName());
        if (exists != null) {
            return TriggerResponse.fail("AppName已存在");
        }

        jobGroupService.save(jobGroup);
        return TriggerResponse.success("添加成功");
    }

    @PostMapping("/update")
    public TriggerResponse update(@RequestBody JobGroup jobGroup) {
        if (jobGroup.getId() == null) {
            return TriggerResponse.fail("ID不能为空");
        }
        jobGroupService.updateById(jobGroup);
        return TriggerResponse.success("更新成功");
    }

    @PostMapping("/remove")
    public TriggerResponse remove(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id == null) {
            return TriggerResponse.fail("ID不能为空");
        }
        jobGroupService.removeById(id);
        return TriggerResponse.success("删除成功");
    }
}
