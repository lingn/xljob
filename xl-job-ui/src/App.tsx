import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import Layout from './components/Layout'
import Home from './pages/home'
import JobGroupPage from './pages/jobgroup'
import JobInfoPage from './pages/jobinfo'
import JobLogPage from './pages/joblog'

const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/jobgroup" element={<JobGroupPage />} />
            <Route path="/jobinfo" element={<JobInfoPage />} />
            <Route path="/joblog" element={<JobLogPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </ConfigProvider>
  )
}

export default App
