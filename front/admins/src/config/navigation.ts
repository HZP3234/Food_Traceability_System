export type RoleKey = 'super-admin' | 'manufacturer' | 'supplier' | 'seller' | 'logistics' | 'regulator'

export interface NavigationItem {
  id: string
  label: string
  icon: string
  roles: RoleKey[]
  desc?: string
}

export interface NavigationGroup {
  label: string
  items: NavigationItem[]
}

export const roles: Record<RoleKey, string> = {
  'super-admin': '超级管理员',
  manufacturer: '加工生产商',
  supplier: '原料供应商',
  seller: '销售商',
  logistics: '物流商',
  regulator: '监管机构',
}

/**
 * 导航权限矩阵：
 * - 加工生产商(manufacturer) 是核心，能看到原料/加工/生产/销售/溯源码
 * - 原料供应商(supplier) 只看原料批次，上传源头信息
 * - 销售商(seller) 只看销售终端，补充储存销售信息
 * - 物流商(logistics) 只看冷链物流
 */
export const navigation: NavigationGroup[] = [
  {
    label: '工作台',
    items: [
      { id: 'dashboard', label: '系统首页', icon: '⌂', roles: ['super-admin', 'manufacturer', 'supplier', 'seller', 'logistics', 'regulator'], desc: '全链可信溯源运营中心' },
    ],
  },
  {
    label: '批次管理',
    items: [
      { id: 'raw-batch', label: '原料批次管理', icon: '原', roles: ['super-admin', 'manufacturer', 'regulator'], desc: '创建原料批次、录入质检、查看供应商匹配状态' },
      { id: 'supplier-raw', label: '原料信息上传', icon: '源', roles: ['super-admin', 'supplier'], desc: '上传源头详情、主动上传、批次匹配' },
      { id: 'process-batch', label: '加工批次管理', icon: '工', roles: ['super-admin', 'manufacturer', 'regulator'], desc: '关联原料批次，记录工艺参数' },
      { id: 'production-batch', label: '生产批次管理', icon: '产', roles: ['super-admin', 'manufacturer', 'regulator'], desc: '创建生产批次，质检，生成溯源码' },
    ],
  },
  {
    label: '流通销售',
    items: [
      { id: 'cold-chain', label: '冷链物流管理', icon: '冷', roles: ['super-admin', 'manufacturer', 'logistics', 'regulator'], desc: '运输订单、车辆、仓库、温湿度监控' },
      { id: 'sales-terminal', label: '销售终端管理', icon: '售', roles: ['super-admin', 'manufacturer', 'seller', 'regulator'], desc: '生产商设置销售商，销售商补充储存与销售信息' },
    ],
  },
  {
    label: '可信追溯',
    items: [
      { id: 'trace-code', label: '溯源码管理', icon: '码', roles: ['super-admin', 'manufacturer', 'regulator'], desc: '生产商生成溯源码，信息随供应商和销售商动态累积' },
    ],
  },
  {
    label: '监管与治理',
    items: [
      { id: 'enterprise-qualification', label: '企业资质管理', icon: '企', roles: ['super-admin', 'regulator'] },
      { id: 'regulatory-trace', label: '监管全链追溯', icon: '监', roles: ['super-admin', 'regulator'] },
      { id: 'audit-log', label: '操作日志审计', icon: '志', roles: ['super-admin', 'regulator'] },
      { id: 'warnings', label: '异常预警推送', icon: '警', roles: ['super-admin', 'regulator', 'manufacturer'] },
    ],
  },
]
