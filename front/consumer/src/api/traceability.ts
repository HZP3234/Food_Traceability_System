import { post } from './index'
import type { TraceabilityQueryDTO, TraceabilityVO, ScanRecordDTO } from '@/types'

// 按产品批次号查询溯源信息
export function queryTraceability(data: TraceabilityQueryDTO) {
  return post<TraceabilityVO>('/api/traceability/query', data as unknown as Record<string, any>)
}

// 记录扫码
export function recordScan(data: ScanRecordDTO) {
  return post<void>('/api/traceability/scan', data as unknown as Record<string, any>)
}
