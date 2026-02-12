import request, { ApiResponse, PageResponse } from './request'

// 任务相关类型
export interface JobInfo {
  id: number
  jobGroup: string
  jobDesc: string
  scheduleConf: string // Cron 表达式
  glueType: string // BEAN, GLUE
  executorHandler: string
  executorParam: string
  executorBlockStrategy: string // SERIALIZATION, DISCARD_LATER, COVER_EARLY
  executorTimeout: number
  executorFailRetryCount: number
  glueSource?: string
  glueRemark?: string
  glueUpdatetime?: string
  childJobid?: string
  status: number // 0=停止 1=运行
  createTime?: string
  updateTime?: string
  jobGroupName?: string
}

export interface JobInfoForm {
  id?: number
  jobGroup: string
  jobDesc: string
  scheduleConf: string
  glueType: string
  executorHandler: string
  executorParam?: string
  executorBlockStrategy: string
  executorTimeout?: number
  executorFailRetryCount?: number
  glueSource?: string
  glueRemark?: string
  childJobid?: string
  misfireStrategy?: number
  executorRouteStrategy?: string
  scheduleType?: number
}

export interface JobInfoQuery {
  jobGroup?: string
  triggerStatus?: number
  jobDesc?: string
  executorHandler?: string
  start?: number
  length?: number
}

// 获取任务分页列表
export function getJobInfoPage(params: JobInfoQuery): Promise<ApiResponse<PageResponse<JobInfo>>> {
  return request.get('/jobinfo/pageList', { params })
}

// 添加任务
export function addJobInfo(data: JobInfoForm): Promise<ApiResponse<string>> {
  return request.post('/jobinfo/add', data)
}

// 更新任务
export function updateJobInfo(data: JobInfoForm): Promise<ApiResponse<string>> {
  return request.post('/jobinfo/update', data)
}

// 删除任务
export function deleteJobInfo(id: number): Promise<ApiResponse<string>> {
  return request.post('/jobinfo/remove', { id })
}

// 启动任务
export function startJob(id: number): Promise<ApiResponse<string>> {
  return request.post('/jobinfo/start', { id })
}

// 停止任务
export function stopJob(id: number): Promise<ApiResponse<string>> {
  return request.post('/jobinfo/stop', { id })
}

// 手动触发任务
export function triggerJob(id: number, executorParam?: string): Promise<ApiResponse<string>> {
  return request.post('/jobinfo/trigger', { id, executorParam })
}

// 获取下一个触发时间
export function getNextTriggerTime(scheduleConf: string): Promise<ApiResponse<string[]>> {
  return request.get('/jobinfo/nextTriggerTime', { params: { scheduleConf } })
}
