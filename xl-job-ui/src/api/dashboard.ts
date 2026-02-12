import request, { ApiResponse } from './request'

// 仪表盘统计信息
export interface DashboardInfo {
  jobGroupCount: number
  jobInfoCount: number
  jobLogCount: number
  todayLogCount: number
  runningJobCount: number
}

// 获取仪表盘信息
export function getDashboardInfo(): Promise<ApiResponse<DashboardInfo>> {
  return request.get('/dashboard/info')
}

// 获取调度报表
export function getChartData(startDate: string, endDate: string): Promise<ApiResponse<{
  triggerDayList: string[]
  triggerCountList: number[]
  triggerCountFailList: number[]
}>> {
  return request.get('/dashboard/chartInfo', { params: { startDate, endDate } })
}
