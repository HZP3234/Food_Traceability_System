export type RoleKey = 'super-admin' | 'enterprise' | 'regulator'

export interface NavigationItem {
  id: string
  label: string
  icon: string
  roles: RoleKey[]
}

export interface NavigationGroup {
  label: string
  items: NavigationItem[]
}

export const roles: Record<RoleKey, string> = {
  'super-admin': '超级管理员',
  enterprise: '企业用户',
  regulator: '监管人员',
}

// 统一维护导航权限；新增模块只需在这里登记一个菜单项即可。
export const navigation: NavigationGroup[] = [
  {
    label: '工作台',
    items: [{ id: 'dashboard', label: '系统首页', icon: '⌂', roles: ['super-admin', 'enterprise', 'regulator'] }],
  },
  {
    label: '批次管理',
    items: [
      { id: 'raw-batch', label: '原料批次管理', icon: '原', roles: ['super-admin', 'enterprise', 'regulator'] },
      { id: 'process-batch', label: '加工批次管理', icon: '工', roles: ['super-admin', 'enterprise', 'regulator'] },
      { id: 'production-batch', label: '生产批次管理', icon: '产', roles: ['super-admin', 'enterprise', 'regulator'] },
    ],
  },
  {
    label: '流通销售',
    items: [
      { id: 'cold-chain', label: '冷链物流管理', icon: '冷', roles: ['super-admin', 'enterprise', 'regulator'] },
      { id: 'sales', label: '销售终端管理', icon: '售', roles: ['super-admin', 'enterprise', 'regulator'] },
    ],
  },
  {
    label: '可信追溯',
    items: [
      { id: 'trace-code', label: '溯源码管理', icon: '码', roles: ['super-admin', 'enterprise'] },
      { id: 'chain-proof', label: '区块链存证记录', icon: '链', roles: ['super-admin', 'enterprise', 'regulator'] },
    ],
  },
  {
    label: '资质监管',
    items: [
      { id: 'qualification', label: '企业资质管理', icon: '企', roles: ['super-admin', 'enterprise', 'regulator'] },
      { id: 'audit', label: '人工资质审核', icon: '审', roles: ['super-admin', 'regulator'] },
      { id: 'supervision', label: '监管全链追溯', icon: '监', roles: ['super-admin', 'regulator'] },
    ],
  },
  {
    label: '平台治理',
    items: [
      { id: 'operation-log', label: '操作日志审计', icon: '志', roles: ['super-admin', 'regulator'] },
      { id: 'alerts', label: '异常预警推送', icon: '警', roles: ['super-admin', 'regulator'] },
      { id: 'report', label: '自定义报表导出', icon: '报', roles: ['super-admin', 'enterprise', 'regulator'] },
    ],
  },
]
