import React, { useEffect, useState } from 'react'
import {
  Table, Button, Space, Modal, Card, Row, Col, Select, Tag,
  Typography, DatePicker, Descriptions, message
} from 'antd'
import { EyeOutlined, StopOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import {
  getJobLogPage, getJobLogDetail, killJob, getExecutionLog,
  JobLog, JobLogQuery, LogDetail
} from '../../api/joblog'
import { getJobGroupList, JobGroup } from '../../api/jobgroup'
import { getJobInfoPage, JobInfo } from '../../api/jobinfo'

const { Title } = Typography
const { RangePicker } = DatePicker

const JobLogPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<JobLog[]>([])
  const [jobGroups, setJobGroups] = useState<JobGroup[]>([])
  const [jobs, setJobs] = useState<JobInfo[]>([])
  const [total, setTotal] = useState(0)
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [currentLogDetail, setCurrentLogDetail] = useState<LogDetail | null>(null)
  const [logContent, setLogContent] = useState('')
  const [logLoading, setLogLoading] = useState(false)

  const [searchParams, setSearchParams] = useState<{ jobGroup: string | number, jobId: number }>({
    jobGroup: -1,
    jobId: -1
  })
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null] | null>(null)

  useEffect(() => {
    fetchJobGroupList()
    fetchJobInfoList()
    fetchJobLogList()
  }, [])

  const fetchJobGroupList = async () => {
    try {
      const res = await getJobGroupList()
      setJobGroups(res.content || [])
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const fetchJobInfoList = async () => {
    try {
      const res = await getJobInfoPage({ length: 1000 })
      setJobs(res.content?.data || [])
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const fetchJobLogList = async () => {
    setLoading(true)
    try {
      const params: Record<string, unknown> = {
        start: (currentPage - 1) * pageSize,
        length: pageSize
      }
      if (searchParams.jobGroup && searchParams.jobGroup !== -1 && typeof searchParams.jobGroup === 'string') {
        params.jobGroup = searchParams.jobGroup
      }
      if (searchParams.jobId && searchParams.jobId !== -1) {
        params.jobId = searchParams.jobId
      }
      if (dateRange && dateRange[0] && dateRange[1]) {
        params.filterTime = `${dateRange[0].format('YYYY-MM-DD')} - ${dateRange[1].format('YYYY-MM-DD')}`
      }
      const res = await getJobLogPage(params)
      setDataSource(res.content?.data || [])
      setTotal(res.content?.recordsTotal || 0)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setCurrentPage(1)
    fetchJobLogList()
  }

  const handleViewDetail = async (record: JobLog) => {
    setDetailModalVisible(true)
    setLogLoading(true)
    setLogContent('')

    try {
      // 获取详情
      const detailRes = await getJobLogDetail(record.id)
      setCurrentLogDetail(detailRes.content)

      // 获取执行日志
      if (record.executorAddress) {
        const logRes = await getExecutionLog(record.executorAddress, record.id, 1)
        setLogContent(logRes.content?.content || '暂无日志内容')
      }
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setLogLoading(false)
    }
  }

  const handleKill = async (id: number) => {
    try {
      await killJob(id)
      message.success('终止任务成功')
      fetchJobLogList()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const columns: ColumnsType<JobLog> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '执行器',
      dataIndex: 'jobGroupName',
      key: 'jobGroupName',
      width: 120,
      render: (text: string) => text || '-'
    },
    {
      title: '任务ID',
      dataIndex: 'jobId',
      key: 'jobId',
      width: 80
    },
    {
      title: '调度时间',
      dataIndex: 'triggerTime',
      key: 'triggerTime',
      width: 180,
      render: (time: number) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
    },
    {
      title: '调度结果',
      dataIndex: 'triggerCode',
      key: 'triggerCode',
      width: 100,
      render: (code: number) => {
        if (code === 200) {
          return <Tag color="green">成功</Tag>
        } else if (code === 500) {
          return <Tag color="red">失败</Tag>
        } else if (code === 0) {
          return <Tag color="orange">等待</Tag>
        }
        return <Tag>{code}</Tag>
      }
    },
    {
      title: '执行时间',
      dataIndex: 'handleTime',
      key: 'handleTime',
      width: 180,
      render: (time: number) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
    },
    {
      title: '执行结果',
      dataIndex: 'handleCode',
      key: 'handleCode',
      width: 100,
      render: (code: number) => {
        if (code === 200) {
          return <Tag color="green">成功</Tag>
        } else if (code === 500) {
          return <Tag color="red">失败</Tag>
        } else if (code === 0) {
          return <Tag color="blue">执行中</Tag>
        }
        return <Tag>{code}</Tag>
      }
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          {record.handleCode === 0 && (
            <Button
              type="link"
              size="small"
              danger
              icon={<StopOutlined />}
              onClick={() => handleKill(record.id)}
            >
              终止
            </Button>
          )}
        </Space>
      )
    }
  ]

  // 过滤当前选中执行器的任务
  const filteredJobs = searchParams.jobGroup && searchParams.jobGroup !== -1
    ? jobs.filter(j => j.jobGroup === searchParams.jobGroup)
    : jobs

  // 修复：jobGroup 比较使用字符串
  const jobGroupFilter = typeof searchParams.jobGroup === 'string' ? searchParams.jobGroup : null

  return (
    <div>
      <Card>
        {/* 搜索区域 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col>
            <Space>
              <span>执行器：</span>
              <Select
                style={{ width: 180 }}
                value={searchParams.jobGroup}
                onChange={(val) => {
                  setSearchParams({ ...searchParams, jobGroup: val, jobId: -1 })
                }}
                options={[
                  { value: -1, label: '全部' },
                  ...jobGroups.map(g => ({ value: g.appName, label: g.title }))
                ]}
              />
            </Space>
          </Col>
          <Col>
            <Space>
              <span>任务：</span>
              <Select
                style={{ width: 180 }}
                value={searchParams.jobId}
                onChange={(val) => setSearchParams({ ...searchParams, jobId: val })}
                options={[
                  { value: -1, label: '全部' },
                  ...filteredJobs.map(j => ({
                    value: j.id,
                    label: `${j.id} - ${j.jobDesc}`
                  }))
                ]}
              />
            </Space>
          </Col>
          <Col>
            <Space>
              <span>时间范围：</span>
              <RangePicker
                showTime
                value={dateRange}
                onChange={(dates) => setDateRange(dates)}
                style={{ width: 360 }}
              />
            </Space>
          </Col>
          <Col>
            <Button type="primary" onClick={handleSearch}>
              搜索
            </Button>
          </Col>
        </Row>

        {/* 表格 */}
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            current: currentPage,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (t) => `共 ${t} 条记录`,
            onChange: (page, size) => {
              setCurrentPage(page)
              setPageSize(size)
              fetchJobLogList()
            }
          }}
        />
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title="调度日志详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {logLoading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        ) : (
          <>
            <Descriptions bordered column={2} size="small" style={{ marginBottom: 16 }}>
              <Descriptions.Item label="执行器地址">
                {currentLogDetail?.executorAddress || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="调度结果">
                <Tag color={currentLogDetail?.triggerCode === 200 ? 'green' : 'red'}>
                  {currentLogDetail?.triggerCode === 200 ? '成功' : '失败'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="执行结果">
                <Tag color={currentLogDetail?.handleCode === 200 ? 'green' :
                  currentLogDetail?.handleCode === 0 ? 'blue' : 'red'}>
                  {currentLogDetail?.handleCode === 200 ? '成功' :
                    currentLogDetail?.handleCode === 0 ? '执行中' : '失败'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            {currentLogDetail?.triggerMsg && (
              <div style={{ marginBottom: 16 }}>
                <Title level={5}>调度信息</Title>
                <pre style={{
                  background: '#f5f5f5',
                  padding: 12,
                  borderRadius: 4,
                  maxHeight: 150,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all'
                }}>
                  {currentLogDetail.triggerMsg}
                </pre>
              </div>
            )}

            {currentLogDetail?.handleMsg && (
              <div style={{ marginBottom: 16 }}>
                <Title level={5}>执行信息</Title>
                <pre style={{
                  background: '#f5f5f5',
                  padding: 12,
                  borderRadius: 4,
                  maxHeight: 150,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all'
                }}>
                  {currentLogDetail.handleMsg}
                </pre>
              </div>
            )}

            {logContent && (
              <div>
                <Title level={5}>执行日志</Title>
                <pre style={{
                  background: '#1e1e1e',
                  color: '#d4d4d4',
                  padding: 12,
                  borderRadius: 4,
                  maxHeight: 300,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all'
                }}>
                  {logContent}
                </pre>
              </div>
            )}
          </>
        )}
      </Modal>
    </div>
  )
}

export default JobLogPage
