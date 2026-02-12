import request, { ApiResponse, PageResponse } from './request'

// 日志相关类型
export interface JobLog {
  id: number
  jobGroup: number
  jobId: number
  executorAddress: string
  executorHandler: string
  executorParam: string
  executorShardingParam: string
  executorFailRetryCount: number
  triggerTime: number
  triggerCode: number
  triggerMsg: string
  handleTime: number
  handleCode: number
  handleMsg: string
  alarmStatus: number
  jobGroupName?: string
}

export interface JobLogQuery {
  jobGroup?: number
  jobId?: number
  logId?: number
  filterTime?: string
  triggerTimeStart?: string
  triggerTimeEnd?: string
  start?: number
  length?: number
}

export interface LogDetail {
  executorAddress: string
  triggerCode: number
  triggerMsg: string
  handleCode: number
  handleMsg: string
}

// 获取日志分页列表
export function getJobLogPage(params: JobLogQuery): Promise<ApiResponse<PageResponse<JobLog>>> {
  return request.get('/joblog/pageList', { params })
}

// 获取日志详情
export function getJobLogDetail(id: number): Promise<ApiResponse<LogDetail>> {
  return request.get('/joblog/logDetailPage', { params: { id } })
}

// 获取执行日志
export function getExecutionLog(executorAddress: string, logId: number, fromLineNum: number): Promise<ApiResponse<{
  content: string
  end: boolean
}>> {
  return request.get('/joblog/logDetailCat', {
    params: { executorAddress, logId, fromLineNum }
  })
}

// 终止任务
export function killJob(logId: number): Promise<ApiResponse<string>> {
  return request.post('/joblog/logKill', { logId })
}

// 清除日志
export function clearLog(jobGroup: number, jobId: number, type: number): Promise<ApiResponse<string>> {
  return request.post('/joblog/clearLog', { jobGroup, jobId, type })
}
