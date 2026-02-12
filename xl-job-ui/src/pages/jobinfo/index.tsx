import React, { useEffect, useState } from 'react'
import {
  Table, Button, Space, Modal, Form, Input, Select, InputNumber,
  Popconfirm, Tag, Card, Row, Col
} from 'antd'
import {
  PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined,
  PauseCircleOutlined, ThunderboltOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import {
  getJobInfoPage, addJobInfo, updateJobInfo, deleteJobInfo,
  startJob, stopJob, triggerJob, JobInfo
} from '../../api/jobinfo'
import { getJobGroupList, JobGroup } from '../../api/jobgroup'

const { TextArea } = Input

// 路由策略选项
const routeStrategyOptions = [
  { value: 'FIRST', label: '第一个' },
  { value: 'LAST', label: '最后一个' },
  { value: 'ROUND', label: '轮询' },
  { value: 'RANDOM', label: '随机' },
  { value: 'CONSISTENT_HASH', label: '一致性哈希' },
  { value: 'LEAST_FREQUENTLY_USED', label: '最不经常使用' },
  { value: 'LEAST_RECENTLY_USED', label: '最近最久未使用' },
  { value: 'FAILOVER', label: '故障转移' },
  { value: 'BUSYOVER', label: '忙碌转移' },
  { value: 'SHARDING_BROADCAST', label: '分片广播' }
]

// 阻塞策略选项
const blockStrategyOptions = [
  { value: 'SERIAL_EXECUTION', label: '单机串行' },
  { value: 'DISCARD_LATER', label: '丢弃后续调度' },
  { value: 'COVER_EARLY', label: '覆盖之前调度' }
]

// 任务状态
const triggerStatusOptions = [
  { value: -1, label: '全部' },
  { value: 0, label: '停止' },
  { value: 1, label: '运行' }
]

const JobInfoPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<JobInfo[]>([])
  const [jobGroups, setJobGroups] = useState<JobGroup[]>([])
  const [total, setTotal] = useState(0)
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingItem, setEditingItem] = useState<JobInfo | null>(null)
  const [triggerModalVisible, setTriggerModalVisible] = useState(false)
  const [triggeringJob, setTriggeringJob] = useState<JobInfo | null>(null)
  const [triggerParam, setTriggerParam] = useState('')
  const [searchParams, setSearchParams] = useState<{ jobGroup: string | number, status: number }>({ jobGroup: -1, status: -1 })
  const [form] = Form.useForm()

  useEffect(() => {
    fetchJobGroupList()
    fetchJobInfoList()
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
    setLoading(true)
    try {
      const params: Record<string, unknown> = {
        start: (currentPage - 1) * pageSize,
        length: pageSize
      }
      if (searchParams.jobGroup !== -1 && searchParams.jobGroup !== '') {
        params.jobGroup = searchParams.jobGroup
      }
      if (searchParams.status !== -1) {
        params.triggerStatus = searchParams.status
      }
      const res = await getJobInfoPage(params)
      setDataSource(res.content?.data || [])
      setTotal(res.content?.recordsTotal || 0)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setCurrentPage(1)
    fetchJobInfoList()
  }

  const handleAdd = () => {
    setEditingItem(null)
    form.resetFields()
    form.setFieldsValue({
      jobGroup: jobGroups[0]?.appName,
      glueType: 'BEAN',
      executorBlockStrategy: 'SERIAL_EXECUTION',
      executorTimeout: 0,
      executorFailRetryCount: 0,
      misfireStrategy: 'DO_NOTHING',
      executorRouteStrategy: 'FIRST'
    })
    setModalVisible(true)
  }

  const handleEdit = (record: JobInfo) => {
    setEditingItem(record)
    form.setFieldsValue({
      ...record,
      scheduleConf: record.scheduleConf
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteJobInfo(id)
      fetchJobInfoList()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleStart = async (id: number) => {
    try {
      await startJob(id)
      fetchJobInfoList()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleStop = async (id: number) => {
    try {
      await stopJob(id)
      fetchJobInfoList()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleTrigger = (record: JobInfo) => {
    setTriggeringJob(record)
    setTriggerParam(record.executorParam || '')
    setTriggerModalVisible(true)
  }

  const doTrigger = async () => {
    if (triggeringJob) {
      try {
        await triggerJob(triggeringJob.id, triggerParam)
        setTriggerModalVisible(false)
      } catch {
        // 错误已在拦截器中处理
      }
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (editingItem) {
        await updateJobInfo({ ...values, id: editingItem.id })
      } else {
        await addJobInfo(values)
      }
      setModalVisible(false)
      fetchJobInfoList()
    } catch {
      // 表单验证失败或请求失败
    }
  }

  const columns: ColumnsType<JobInfo> = [
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
      title: '任务描述',
      dataIndex: 'jobDesc',
      key: 'jobDesc',
      width: 150,
      ellipsis: true
    },
    {
      title: 'Cron',
      dataIndex: 'scheduleConf',
      key: 'scheduleConf',
      width: 120
    },
    {
      title: 'Handler',
      dataIndex: 'executorHandler',
      key: 'executorHandler',
      width: 150,
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '运行' : '停止'}
        </Tag>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          {record.status === 1 ? (
            <Button
              type="link"
              size="small"
              onClick={() => handleStop(record.id)}
              icon={<PauseCircleOutlined />}
            >
              停止
            </Button>
          ) : (
            <Button
              type="link"
              size="small"
              onClick={() => handleStart(record.id)}
              icon={<PlayCircleOutlined />}
            >
              启动
            </Button>
          )}
          <Button
            type="link"
            size="small"
            onClick={() => handleTrigger(record)}
            icon={<ThunderboltOutlined />}
          >
            触发
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => handleEdit(record)}
            icon={<EditOutlined />}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除该任务吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Card>
        {/* 搜索区域 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col>
            <Space>
              <span>执行器：</span>
              <Select
                style={{ width: 200 }}
                value={searchParams.jobGroup}
                onChange={(val) => setSearchParams({ ...searchParams, jobGroup: val })}
                options={[
                  { value: -1, label: '全部' },
                  ...jobGroups.map(g => ({ value: g.appName, label: g.title }))
                ]}
              />
            </Space>
          </Col>
          <Col>
            <Space>
              <span>状态：</span>
              <Select
                style={{ width: 120 }}
                value={searchParams.status}
                onChange={(val) => setSearchParams({ ...searchParams, status: val })}
                options={triggerStatusOptions}
              />
            </Space>
          </Col>
          <Col>
            <Button type="primary" onClick={handleSearch}>
              搜索
            </Button>
          </Col>
          <Col flex="auto" style={{ textAlign: 'right' }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              添加任务
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
              fetchJobInfoList()
            }
          }}
        />
      </Card>

      {/* 添加/编辑弹窗 */}
      <Modal
        title={editingItem ? '编辑任务' : '添加任务'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="jobGroup"
                label="执行器组"
                rules={[{ required: true, message: '请选择执行器组' }]}
              >
                <Select
                  options={jobGroups.map(g => ({ value: g.appName, label: g.title }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="jobDesc"
                label="任务描述"
                rules={[{ required: true, message: '请输入任务描述' }]}
              >
                <Input placeholder="请输入任务描述" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="scheduleConf"
                label="Cron 表达式"
                rules={[{ required: true, message: '请输入 Cron 表达式' }]}
                extra="如：0 0 2 * * ? 表示每天凌晨2点执行"
              >
                <Input placeholder="请输入 Cron 表达式" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="glueType"
                label="运行模式"
                rules={[{ required: true }]}
              >
                <Select
                  options={[
                    { value: 'BEAN', label: 'BEAN 模式' },
                    { value: 'GLUE_GROOVY', label: 'GLUE(Java)' },
                    { value: 'GLUE_SHELL', label: 'GLUE(Shell)' },
                    { value: 'GLUE_PYTHON', label: 'GLUE(Python)' },
                    { value: 'GLUE_PHP', label: 'GLUE(PHP)' },
                    { value: 'GLUE_NODEJS', label: 'GLUE(Node.js)' },
                    { value: 'GLUE_POWERSHELL', label: 'GLUE(PowerShell)' }
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="executorRouteStrategy"
                label="路由策略"
                rules={[{ required: true }]}
              >
                <Select options={routeStrategyOptions} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="executorBlockStrategy"
                label="阻塞策略"
                rules={[{ required: true }]}
              >
                <Select options={blockStrategyOptions} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            noStyle
            shouldUpdate={(prev, curr) => prev.glueType !== curr.glueType}
          >
            {({ getFieldValue }) => {
              const glueType = getFieldValue('glueType')
              if (glueType === 'BEAN') {
                return (
                  <Form.Item
                    name="executorHandler"
                    label="JobHandler"
                    rules={[{ required: true, message: '请输入 JobHandler' }]}
                    extra="任务执行器名称，需与代码中 @XxlJob 注解的值一致"
                  >
                    <Input placeholder="请输入 JobHandler 名称" />
                  </Form.Item>
                )
              }
              return null
            }}
          </Form.Item>

          <Form.Item
            name="executorParam"
            label="任务参数"
          >
            <TextArea rows={3} placeholder="请输入任务参数" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="executorTimeout"
                label="超时时间（秒）"
                extra="0 表示不超时"
              >
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="executorFailRetryCount"
                label="重试次数"
                extra="失败后重试次数"
              >
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* 手动触发弹窗 */}
      <Modal
        title={`手动触发任务 - ${triggeringJob?.jobDesc || ''}`}
        open={triggerModalVisible}
        onOk={doTrigger}
        onCancel={() => setTriggerModalVisible(false)}
        okText="确认触发"
      >
        <div style={{ marginBottom: 16 }}>
          <p>任务ID: {triggeringJob?.id}</p>
          <p>JobHandler: {triggeringJob?.executorHandler}</p>
        </div>
        <Form.Item label="任务参数">
          <TextArea
            rows={4}
            value={triggerParam}
            onChange={(e) => setTriggerParam(e.target.value)}
            placeholder="可选，输入本次触发的任务参数"
          />
        </Form.Item>
      </Modal>
    </div>
  )
}

export default JobInfoPage
