import { post, get } from './index'
import type { TraceabilityQueryDTO, TraceabilityVO, ScanRecordDTO, ScanRecord } from '@/types'

// 按产品批次号查询溯源信息
export function queryTraceability(data: TraceabilityQueryDTO) {
  return post<TraceabilityVO>('/api/traceability/query', data as unknown as Record<string, any>)
}

// 按溯源码查询溯源信息
export function queryByTraceCode(traceCode: string, userId?: string) {
  return get<TraceabilityVO>('/api/traceability/query-by-code', { traceCode, userId })
}

// 记录扫码
export function recordScan(data: ScanRecordDTO) {
  return post<void>('/api/traceability/scan', data as unknown as Record<string, any>)
}

// 获取最近扫码记录
export function getRecentScans(limit = 20) {
  return get<ScanRecord[]>('/api/traceability/scans', { limit })
}

// 获取用户扫码历史
export function getUserScans(userId: string, limit = 50) {
  return get<ScanRecord[]>('/api/traceability/user-scans', { userId, limit })
}
