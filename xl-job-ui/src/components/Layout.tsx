import React, { useState } from 'react'
import { Layout as AntLayout, Menu, theme } from 'antd'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  ClusterOutlined,
  ScheduleOutlined,
  FileTextOutlined
} from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'

const { Header, Sider, Content } = AntLayout

interface LayoutProps {
  children: React.ReactNode
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const {
    token: { colorBgContainer, borderRadiusLG }
  } = theme.useToken()

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '首页'
    },
    {
      key: '/jobgroup',
      icon: <ClusterOutlined />,
      label: '执行器管理'
    },
    {
      key: '/jobinfo',
      icon: <ScheduleOutlined />,
      label: '任务管理'
    },
    {
      key: '/joblog',
      icon: <FileTextOutlined />,
      label: '调度日志'
    }
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
  }

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div style={{
          height: 32,
          margin: 16,
          background: 'rgba(255, 255, 255, 0.2)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontWeight: 'bold',
          fontSize: collapsed ? 14 : 16
        }}>
          {collapsed ? 'XL' : 'XL-JOB'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout>
        <Header style={{
          padding: '0 16px',
          background: colorBgContainer,
          display: 'flex',
          alignItems: 'center'
        }}>
          {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
            className: 'trigger',
            onClick: () => setCollapsed(!collapsed),
            style: { fontSize: 18, cursor: 'pointer' }
          })}
          <span style={{ marginLeft: 16, fontSize: 16, fontWeight: 500 }}>
            分布式任务调度平台
          </span>
        </Header>
        <Content style={{
          margin: '24px 16px',
          padding: 24,
          background: colorBgContainer,
          borderRadius: borderRadiusLG,
          minHeight: 280,
          overflow: 'auto'
        }}>
          {children}
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout
