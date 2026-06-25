// 通用响应
export interface Result<T> {
  code: number
  message: string
  data: T
}

// 分页
export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 溯源查询
export interface TraceabilityQueryDTO {
  productBatchNo: string
  userId?: string
}

export interface TraceabilityNode {
  id: number
  nodeName: string
  nodeDescription: string
  nodeTime: string
  location: string
  operator: string
  sortOrder: number
}

export interface TraceabilityVO {
  productBatchNo: string
  productName: string
  manufacturer: string
  productionDate: string
  productionLine: string
  checkResult: number
  txHash: string
  nodes: TraceabilityNode[]
}

// 扫码记录
export interface ScanRecordDTO {
  productBatchNo: string
  scanLocation?: string
  userId?: string
}

export interface ScanRecord {
  id: number
  productBatchNo: string
  scanIp: string
  scanLocation: string
  userId: string
  scanTime: string
  createTime: string
}

// 投诉
export interface Complaint {
  id: number
  complaintNo: string
  productBatchNo: string
  productName: string
  consumerName: string
  consumerPhone: string
  consumerId?: number
  complaintType: number
  complaintTitle: string
  complaintContent: string
  imageUrls: string
  status: number
  feedbackContent: string
  feedbackTime: string
  feedbackBy: string
  createTime: string
}

export interface ComplaintSubmitDTO {
  productBatchNo: string
  productName: string
  consumerName: string
  consumerPhone: string
  consumerId?: number
  complaintType: number
  complaintTitle: string
  complaintContent: string
  imageUrls?: string
}

export interface ComplaintQueryDTO {
  complaintNo?: string
  productBatchNo?: string
  productName?: string
  consumerPhone?: string
  consumerId?: number
  complaintType?: number
  status?: number
  pageNum?: number
  pageSize?: number
}

export type ComplaintTypeKey = 1 | 2 | 3 | 4 | 5

export interface UserInfo {
  consumerId: number
  consumerUuid: string
  nickName: string
  realName: string | null
  phone: string
  gender: string
  region: string | null
  lastScanTime: string | null
  totalScans: number
  complaintCount: number
  status: number
  createTime: string
}

export interface UserInfoUpdateDTO {
  phone: string
  nickName?: string
  realName?: string
  gender?: string
  region?: string
}
