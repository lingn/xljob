import React, { useEffect, useState } from 'react'
import {
  Table, Button, Space, Modal, Form, Input, Radio, message,
  Popconfirm, Typography, Tag, Card
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import {
  getJobGroupList, addJobGroup, updateJobGroup, deleteJobGroup,
  JobGroup
} from '../../api/jobgroup'

const { Title } = Typography
const { TextArea } = Input

const JobGroupPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<JobGroup[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingItem, setEditingItem] = useState<JobGroup | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    fetchJobGroupList()
  }, [])

  const fetchJobGroupList = async () => {
    setLoading(true)
    try {
      const res = await getJobGroupList()
      setDataSource(res.content || [])
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingItem(null)
    form.resetFields()
    form.setFieldsValue({ addressType: 0 })
    setModalVisible(true)
  }

  const handleEdit = (record: JobGroup) => {
    setEditingItem(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteJobGroup(id)
      message.success('删除成功')
      fetchJobGroupList()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (editingItem) {
        await updateJobGroup({ ...values, id: editingItem.id })
        message.success('更新成功')
      } else {
        await addJobGroup(values)
        message.success('添加成功')
      }
      setModalVisible(false)
      fetchJobGroupList()
    } catch {
      // 表单验证失败或请求失败
    }
  }

  const columns: ColumnsType<JobGroup> = [
    {
      title: 'AppName',
      dataIndex: 'appName',
      key: 'appName',
      width: 150
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      width: 150
    },
    {
      title: '地址类型',
      dataIndex: 'addressType',
      key: 'addressType',
      width: 120,
      render: (type: number) => (
        <Tag color={type === 0 ? 'blue' : 'green'}>
          {type === 0 ? '自动注册' : '手动录入'}
        </Tag>
      )
    },
    {
      title: '地址列表',
      dataIndex: 'addressList',
      key: 'addressList',
      ellipsis: true,
      render: (text: string) => text || '-'
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 180
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
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除该执行器组吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
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
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
          <Title level={4} style={{ margin: 0 }}>执行器管理</Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加执行器
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1000 }}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
        />
      </Card>

      <Modal
        title={editingItem ? '编辑执行器' : '添加执行器'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 16 }}
        >
          <Form.Item
            name="appName"
            label="AppName"
            rules={[
              { required: true, message: '请输入 AppName' },
              { pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/, message: 'AppName 必须以字母开头，只能包含字母、数字、下划线和横线' }
            ]}
          >
            <Input placeholder="请输入执行器 AppName" />
          </Form.Item>

          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入执行器标题" />
          </Form.Item>

          <Form.Item
            name="addressType"
            label="地址类型"
            rules={[{ required: true }]}
          >
            <Radio.Group>
              <Radio value={0}>自动注册</Radio>
              <Radio value={1}>手动录入</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) => prevValues.addressType !== currentValues.addressType}
          >
            {({ getFieldValue }) => {
              const addressType = getFieldValue('addressType')
              if (addressType === 1) {
                return (
                  <Form.Item
                    name="addressList"
                    label="机器地址"
                    rules={[{ required: true, message: '请输入机器地址' }]}
                    extra="多个地址用逗号分隔，如：192.168.1.1:9999,192.168.1.2:9999"
                  >
                    <TextArea rows={3} placeholder="请输入执行器机器地址" />
                  </Form.Item>
                )
              }
              return null
            }}
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default JobGroupPage
