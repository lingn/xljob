import request, { ApiResponse, PageResponse } from './request'

// 执行器组相关类型
export interface JobGroup {
  id: number
  appName: string
  title: string
  addressType: number // 0=自动注册 1=手动录入
  addressList: string
  updateTime: string
}

export interface JobGroupForm {
  id?: number
  appName: string
  title: string
  addressType: number
  addressList?: string
}

// 获取执行器组列表
export function getJobGroupList(): Promise<ApiResponse<JobGroup[]>> {
  return request.get('/jobgroup/list')
}

// 获取执行器组分页列表
export function getJobGroupPage(params: {
  appname?: string
  title?: string
  start?: number
  length?: number
}): Promise<ApiResponse<PageResponse<JobGroup>>> {
  return request.get('/jobgroup/pageList', { params })
}

// 添加执行器组
export function addJobGroup(data: JobGroupForm): Promise<ApiResponse<string>> {
  return request.post('/jobgroup/add', data)
}

// 更新执行器组
export function updateJobGroup(data: JobGroupForm): Promise<ApiResponse<string>> {
  return request.post('/jobgroup/update', data)
}

// 删除执行器组
export function deleteJobGroup(id: number): Promise<ApiResponse<string>> {
  return request.post('/jobgroup/remove', { id })
}
