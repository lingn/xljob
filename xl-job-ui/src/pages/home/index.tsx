import React, { useEffect, useState } from 'react'
import { Card, Row, Col, Statistic, Button, Typography, Space } from 'antd'
import {
  ClusterOutlined,
  ScheduleOutlined,
  CheckCircleOutlined,
  SyncOutlined,
  ArrowRightOutlined
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getDashboardInfo, DashboardInfo } from '../../api/dashboard'

const { Title, Text } = Typography

const Home: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [dashboardInfo, setDashboardInfo] = useState<DashboardInfo>({
    jobGroupCount: 0,
    jobInfoCount: 0,
    jobLogCount: 0,
    todayLogCount: 0,
    runningJobCount: 0
  })

  useEffect(() => {
    fetchDashboardInfo()
  }, [])

  const fetchDashboardInfo = async () => {
    setLoading(true)
    try {
      const res = await getDashboardInfo()
      setDashboardInfo(res.content)
    } finally {
      setLoading(false)
    }
  }

  const quickActions = [
    {
      title: '执行器管理',
      description: '管理执行器组，配置注册信息',
      icon: <ClusterOutlined style={{ fontSize: 32, color: '#1890ff' }} />,
      path: '/jobgroup'
    },
    {
      title: '任务管理',
      description: '创建和管理调度任务',
      icon: <ScheduleOutlined style={{ fontSize: 32, color: '#52c41a' }} />,
      path: '/jobinfo'
    },
    {
      title: '调度日志',
      description: '查看任务执行日志',
      icon: <CheckCircleOutlined style={{ fontSize: 32, color: '#faad14' }} />,
      path: '/joblog'
    }
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>系统概览</Title>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="执行器数量"
              value={dashboardInfo.jobGroupCount}
              prefix={<ClusterOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="任务数量"
              value={dashboardInfo.jobInfoCount}
              prefix={<ScheduleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="今日调度次数"
              value={dashboardInfo.todayLogCount}
              prefix={<SyncOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="运行中任务"
              value={dashboardInfo.runningJobCount}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 快捷操作 */}
      <Title level={4} style={{ marginBottom: 24 }}>快捷操作</Title>
      <Row gutter={16}>
        {quickActions.map((action, index) => (
          <Col xs={24} sm={12} lg={8} key={index}>
            <Card
              hoverable
              style={{ marginBottom: 16 }}
              onClick={() => navigate(action.path)}
            >
              <Space direction="vertical" style={{ width: '100%' }}>
                <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                  {action.icon}
                  <Button type="text" icon={<ArrowRightOutlined />} />
                </Space>
                <Title level={5} style={{ margin: 0 }}>{action.title}</Title>
                <Text type="secondary">{action.description}</Text>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>

      {/* 系统信息 */}
      <Title level={4} style={{ marginBottom: 24, marginTop: 16 }}>系统信息</Title>
      <Card>
        <Row gutter={[16, 16]}>
          <Col span={8}>
            <Text type="secondary">版本号：</Text>
            <Text>v2.4.0</Text>
          </Col>
          <Col span={8}>
            <Text type="secondary">API 地址：</Text>
            <Text>http://localhost:8080</Text>
          </Col>
          <Col span={8}>
            <Text type="secondary">当前时间：</Text>
            <Text>{new Date().toLocaleString('zh-CN')}</Text>
          </Col>
        </Row>
      </Card>
    </div>
  )
}

export default Home
