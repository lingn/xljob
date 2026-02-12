# XL-JOB 分布式任务调度系统

一个轻量级的分布式任务调度系统，类似 XXL-JOB，基于 Spring Boot 3.x + MyBatis-Plus + Netty 实现。

## 项目结构

```
xl-job/
├── xl-job-core/           # 公共核心模块
├── xl-job-executor/       # 执行器模块
├── xl-job-admin/          # 调度中心模块
└── xl-job-demo/           # 示例项目
```

## 功能特性

### 调度中心 (xl-job-admin)
- 任务管理 (CRUD)
- Cron 调度
- 执行器管理
- 日志查询
- 失败重试

### 执行器 (xl-job-executor)
- Netty 服务接收调度
- @XxlJob 注解支持
- 心跳上报
- 状态回调

### 路由策略
- **ROUND**: 轮询，按顺序依次选择执行器
- **RANDOM**: 随机选择一个执行器
- **FAILOVER**: 故障转移，心跳检测选择存活执行器

### 阻塞策略
- **SERIAL**: 单机串行，任务排队执行
- **PARALLEL**: 并行执行，不限制并发
- **DISCARD**: 丢弃后续，有任务运行时丢弃新任务

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE xl_job DEFAULT CHARACTER SET utf8mb4;
```

执行 `xl-job-admin/src/main/resources/schema.sql` 创建表结构。

### 2. 配置调度中心

修改 `xl-job-admin/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xl_job?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password
```

### 3. 启动调度中心

```bash
cd xl-job-admin
mvn spring-boot:run
```

调度中心启动后访问 http://localhost:8080

### 4. 配置执行器

在执行器项目中添加依赖:

```xml
<dependency>
    <groupId>com.xl.job</groupId>
    <artifactId>xl-job-executor</artifactId>
    <version>1.0.0</version>
</dependency>
```

配置 application.yml:

```yaml
xl:
  job:
    executor:
      appname: my-executor
      title: 我的执行器
      port: 9999
      admin-addresses: http://127.0.0.1:8080
```

### 5. 编写任务

```java
@Component
public class MyJob {

    @XxlJob("myJobHandler")
    public void execute() {
        System.out.println("执行任务: " + LocalDateTime.now());
    }

    @XxlJob("myJobWithParam")
    public String executeWithParam(String param) {
        return "处理完成: " + param;
    }
}
```

## API 接口

### 执行器组管理

| 接口 | 方法 | 说明 |
|------|------|------|
| /jobgroup/list | GET | 查询执行器组列表 |
| /jobgroup/add | POST | 添加执行器组 |
| /jobgroup/update | POST | 更新执行器组 |
| /jobgroup/remove | POST | 删除执行器组 |

### 任务管理

| 接口 | 方法 | 说明 |
|------|------|------|
| /jobinfo/list | GET | 查询任务列表 |
| /jobinfo/add | POST | 添加任务 |
| /jobinfo/update | POST | 更新任务 |
| /jobinfo/remove | POST | 删除任务 |
| /jobinfo/start | POST | 启动任务 |
| /jobinfo/stop | POST | 停止任务 |
| /jobinfo/trigger | POST | 手动触发任务 |

### 日志管理

| 接口 | 方法 | 说明 |
|------|------|------|
| /joblog/list | GET | 查询日志列表 |
| /joblog/detail | GET | 查询日志详情 |

## 添加任务示例

```bash
# 1. 添加执行器组
curl -X POST http://localhost:8080/jobgroup/add \
  -H "Content-Type: application/json" \
  -d '{"appName":"xl-job-demo","title":"示例执行器","addressType":0}'

# 2. 添加任务
curl -X POST http://localhost:8080/jobinfo/add \
  -H "Content-Type: application/json" \
  -d '{
    "jobGroup":"xl-job-demo",
    "jobDesc":"示例任务",
    "scheduleConf":"0/10 * * * * ?",
    "executorHandler":"demoJob",
    "executorRouteStrategy":"ROUND",
    "executorBlockStrategy":"SERIAL"
  }'

# 3. 启动任务
curl -X POST "http://localhost:8080/jobinfo/start?id=1"

# 4. 手动触发任务
curl -X POST "http://localhost:8080/jobinfo/trigger?id=1"
```

## 技术栈

- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Netty 4.1.x
- Quartz (Cron 调度)
- Hutool (工具类)

## 编译运行

```bash
# 编译整个项目
cd xl-job
mvn clean package -DskipTests

# 启动调度中心
java -jar xl-job-admin/target/xl-job-admin-1.0.0.jar

# 启动示例执行器
java -jar xl-job-demo/target/xl-job-demo-1.0.0.jar
```
