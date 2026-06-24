import { post, get, put } from './index'
import type { Complaint, ComplaintSubmitDTO, ComplaintQueryDTO, Page } from '@/types'

// 提交投诉
export function submitComplaint(data: ComplaintSubmitDTO) {
  return post<Complaint>('/api/complaint/submit', data as unknown as Record<string, any>)
}

// 分页查询投诉
export function queryComplaintPage(data: ComplaintQueryDTO) {
  return post<Page<Complaint>>('/api/complaint/page', data as unknown as Record<string, any>)
}

// 投诉详情
export function getComplaintDetail(id: number) {
  return get<Complaint>(`/api/complaint/detail/${id}`)
}

// 投诉反馈（实际由企业端处理）
export function feedbackComplaint(data: { complaintId: number; feedbackContent: string; feedbackBy: string }) {
  return put<Complaint>('/api/complaint/feedback', data as unknown as Record<string, any>)
}
