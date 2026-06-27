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
  productBatchNo?: string
  userId?: string
  traceCode?: string
}

export interface ScanRecordDTO {
  productBatchNo: string
  traceCode?: string
  scanLocation?: string
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
  traceCode: string
  productBatchNo: string
  productName: string
  manufacturer: string
  supplierName: string
  rawBatchNo: string
  productionDate: string
  productionLine: string
  checkResult: number
  txHash: string
  nodes: TraceabilityNode[]
}

// 投诉
export interface Complaint {
  complaintRecordId: number
  complaintNo: string
  traceCode: string
  batchNumber: string
  enterpriseUuid: string
  enterpriseName: string
  consumerUuid: string
  complainantName: string
  phone: string
  isAnonymous: number
  complaintType: number
  description: string
  photoUrls: string
  priority: number
  status: number
  handlerId: string
  handlerName: string
  handlingConclusion: string
  handlingProofUrls: string
  submitTime: string
  acceptTime: string
  closeTime: string
  createTime: string
  updateTime: string
}

export interface ComplaintSubmitDTO {
  traceCode?: string
  batchNumber: string
  enterpriseUuid?: string
  enterpriseName: string
  consumerUuid?: string
  complainantName: string
  phone: string
  isAnonymous?: number
  complaintType: number
  description: string
  photoUrls?: string
}

export interface ComplaintQueryDTO {
  complaintNo?: string
  batchNumber?: string
  enterpriseName?: string
  phone?: string
  consumerUuid?: string
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
