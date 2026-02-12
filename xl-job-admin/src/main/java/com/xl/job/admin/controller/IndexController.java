package com.xl.job.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首页 Controller
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "XL-JOB 分布式任务调度系统");
        result.put("version", "1.0.0");
        result.put("status", "running");

        Map<String, String> apis = new LinkedHashMap<>();
        apis.put("执行器组管理", "/jobgroup/list");
        apis.put("任务管理", "/jobinfo/list");
        apis.put("日志管理", "/joblog/list");
        apis.put("心跳检测", "/api/beat");
        result.put("apis", apis);

        return result;
    }
}
