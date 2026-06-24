/**
 * API 服务层 —— 封装对后端的所有请求。
 * 后端使用 @RequestMapping（同时接受 GET/POST），参数为 form 风格。
 */
// 开发环境走 Vite 代理，生产环境同域部署，都使用相对路径

/** 获取当前存储的 Token */
function getToken(): string | null {
  return sessionStorage.getItem('fts-admin-token')
}

/** 构建带 Authorization 的 headers（如果有 token） */
function authHeaders(extra?: Record<string, string>): Record<string, string> {
  const headers: Record<string, string> = { ...extra }
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

/** 检查后端业务异常：GlobalExceptionHandler 返回 success=false 但 HTTP 200 */
function checkBizError(json: any) {
  if (json && typeof json === 'object' && json.success === false) {
    throw new Error(json.errorMessage || json.errorCode || '服务器业务异常')
  }
}

/** 通用 GET 请求（参数拼在 query string，自动带 Token） */
async function get<T = any>(path: string, params?: Record<string, any>): Promise<T> {
  const url = new URL(path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, String(v))
    })
  }
  const res = await fetch(url.toString(), { headers: authHeaders() })
  if (!res.ok) throw new Error(`请求失败 ${res.status}`)
  const text = await res.text()
  let json: any
  try { json = JSON.parse(text) } catch { return text as any }
  checkBizError(json)
  return json as T
}

/** 通用 POST 请求（form-encoded body，自动带 Token） */
async function post<T = any>(path: string, data?: Record<string, any>): Promise<T> {
  const body = new URLSearchParams()
  if (data) {
    Object.entries(data).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') body.set(k, String(v))
    })
  }
  const res = await fetch(path, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' }),
    body: body.toString(),
  })
  if (!res.ok) throw new Error(`请求失败 ${res.status}`)
  const text = await res.text()
  let json: any
  try { json = JSON.parse(text) } catch { return text as any }
  checkBizError(json)
  return json as T
}

/** 通用 POST 请求（JSON body，regulation 后端使用 @RequestBody） */
async function postJson<T = any>(path: string, data?: Record<string, any>): Promise<T> {
  const res = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: data ? JSON.stringify(data) : undefined,
  })
  if (!res.ok) throw new Error(`请求失败 ${res.status}`)
  const text = await res.text()
  let json: any
  try { json = JSON.parse(text) } catch { return text as any }
  checkBizError(json)
  return json as T
}

/** 通用带 Token 的 GET 请求 */
async function getWithAuth<T = any>(path: string, token: string, params?: Record<string, any>): Promise<T> {
  const url = new URL(path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, String(v))
    })
  }
  const res = await fetch(url.toString(), {
    headers: { 'Authorization': `Bearer ${token}` },
  })
  if (!res.ok) throw new Error(`请求失败 ${res.status}`)
  const text = await res.text()
  let json: any
  try { json = JSON.parse(text) } catch { return text as any }
  checkBizError(json)
  return json as T
}

/** 通用 PUT 请求（JSON body，自动带 Token） */
async function put<T = any>(path: string, data?: Record<string, any>): Promise<T> {
  const res = await fetch(path, {
    method: 'PUT',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: data ? JSON.stringify(data) : undefined,
  })
  if (!res.ok) throw new Error(`请求失败 ${res.status}`)
  const text = await res.text()
  let json: any
  try { json = JSON.parse(text) } catch { return text as any }
  checkBizError(json)
  return json as T
}

/** 通用 DELETE 请求（自动带 Token） */
async function del<T = any>(path: string): Promise<T> {
  const res = await fetch(path, { method: 'DELETE', headers: authHeaders() })
  if (!res.ok) throw new Error(`请求失败 ${res.status}`)
  const text = await res.text()
  let json: any
  try { json = JSON.parse(text) } catch { return text as any }
  checkBizError(json)
  return json as T
}

// ==================== Auth (认证) ====================
export const authApi = {
  /** 登录 — 返回 { code, message, data: { token, username, roleType, realName, enterpriseUuid } } */
  login: (username: string, password: string) =>
    postJson('/api/auth/login', { username, password }),
  /** 注册 */
  register: (data: Record<string, any>) =>
    postJson('/api/auth/register', data),
  /** 获取当前用户信息 */
  me: (token: string) =>
    getWithAuth('/api/auth/me', token),
}

// ==================== Raw (原料) ====================
export const rawApi = {
  queryByBatchNo: (batchNo: string) => get('/Raw/queryByBatchNo', { batchNo }),
  list: (params: Record<string, any>) => get('/Raw/listRaw', params),
  listBySupplierId: (supplierId: string) => get('/Raw/listBySupplierId', { supplierId }),
  create: (data: Record<string, any>) => post('/Raw/createRaw', data),
  update: (data: Record<string, any>) => post('/Raw/updateRaw', data),
  delete: (rawBatchId: number) => post('/Raw/deleteRaw', { rawBatchId }),
  queryDetail: (batchNo: string) => get('/Raw/queryDetail', { batchNo }),
  uploadDetail: (batchNo: string, detail: Record<string, any>) => post('/Raw/uploadDetail', { batchNo, ...detail }),
  proactiveUpload: (detail: Record<string, any>, pending: Record<string, any>) =>
    post('/Raw/proactiveUpload', { ...detail, ...pending }),
  matchBatch: (pendingCode: string, targetBatchNo: string) => post('/Raw/matchBatch', { pendingCode, targetBatchNo }),
  listPending: (supplierName?: string, pendingStatus?: number) =>
    get('/Raw/listPending', { supplierName, pendingStatus }),
  qualityCheck: (batchNo: string, checkResult: number) => post('/Raw/qualityCheck', { batchNo, checkResult }),
}

// ==================== Production (生产/加工) ====================
export const productionApi = {
  // Tech Template
  queryTemplate: (templateName: string) => get('/Production/queryTemplate', { templateName }),
  listTemplateByProduct: (applicableProduct: string) => get('/Production/listTemplateByProduct', { applicableProduct }),
  listTemplate: (params: Record<string, any>) => get('/Production/listTemplate', params),
  createTemplate: (data: Record<string, any>) => post('/Production/createTemplate', data),
  updateTemplate: (data: Record<string, any>) => post('/Production/updateTemplate', data),
  deleteTemplate: (templateId: number) => post('/Production/deleteTemplate', { templateId }),

  // Process Batch (加工批次)
  queryProcessBatch: (batchNo: string) => get('/Production/queryProcessBatch', { batchNo }),
  listProcessByRaw: (rawBatchNo: string) => get('/Production/listProcessByRaw', { rawBatchNo }),
  listProcessBatch: (params: Record<string, any>) => get('/Production/listProcessBatch', params),
  createProcessBatch: (data: Record<string, any>) => post('/Production/createProcessBatch', data),
  updateProcessBatch: (data: Record<string, any>) => post('/Production/updateProcessBatch', data),
  completeProcessBatch: (processBatchId: number) => post('/Production/completeProcessBatch', { processBatchId }),
  deleteProcessBatch: (processBatchId: number) => post('/Production/deleteProcessBatch', { processBatchId }),

  // Prod Batch (生产批次)
  queryProdBatch: (batchNo: string) => get('/Production/queryProdBatch', { batchNo }),
  listProdByProcess: (processBatchNo: string) => get('/Production/listProdByProcess', { processBatchNo }),
  listProdBatch: (params: Record<string, any>) => get('/Production/listProdBatch', params),
  createProdBatch: (data: Record<string, any>) => post('/Production/createProdBatch', data),
  updateProdBatch: (data: Record<string, any>) => post('/Production/updateProdBatch', data),
  completeProdBatch: (prodBatchId: number) => post('/Production/completeProdBatch', { prodBatchId }),
  startProdBatch: (prodBatchId: number) => post('/Production/startProdBatch', { prodBatchId }),
  bindCode: (prodBatchId: number) => post('/Production/bindCode', { prodBatchId }),
  deleteProdBatch: (prodBatchId: number) => post('/Production/deleteProdBatch', { prodBatchId }),

  // Material Input
  queryMaterialInput: (rawBatchNo: string) => get('/Production/queryMaterialInput', { rawBatchNo }),
  listMaterialInput: (materialName?: string) => get('/Production/listMaterialInput', { materialName }),
  recordMaterialInput: (data: Record<string, any>) => post('/Production/recordMaterialInput', data),
  updateMaterialInput: (data: Record<string, any>) => post('/Production/updateMaterialInput', data),

  // Env Record
  listEnvRecord: (productionLine: string) => get('/Production/listEnvRecord', { productionLine }),
  listEnvAbnormal: (isAbnormal?: number) => get('/Production/listEnvAbnormal', { isAbnormal }),
  recordEnv: (data: Record<string, any>) => post('/Production/recordEnv', data),
  updateEnvRecord: (data: Record<string, any>) => post('/Production/updateEnvRecord', data),

  // Quality Inspection
  queryInspection: (inspectionNo: string) => get('/Production/queryInspection', { inspectionNo }),
  listInspectionByBiz: (bizType: number, bizBatchNo: string) =>
    get('/Production/listInspectionByBiz', { bizType, bizBatchNo }),
  listInspectionByType: (bizType: number) => get('/Production/listInspectionByType', { bizType }),
  listInspection: (params: Record<string, any>) => get('/Production/listInspection', params),
  createInspection: (data: Record<string, any>) => post('/Production/createInspection', data),
  updateInspection: (data: Record<string, any>) => post('/Production/updateInspection', data),
  qualityCheckProd: (prodBatchNo: string, checkResult: number) =>
    post('/Production/qualityCheckProd', { prodBatchNo, checkResult }),
  qualityCheckProcess: (processBatchNo: string, checkResult: number) =>
    post('/Production/qualityCheckProcess', { processBatchNo, checkResult }),
  traceProcessChain: (prodBatchNo: string) => get('/Production/traceProcessChain', { prodBatchNo }),
}

// ==================== TraceCode (溯源码) ====================
export const traceCodeApi = {
  list: (params: Record<string, any>) => get('/TraceCode/list', params),
  detail: (traceCode: string) => get('/TraceCode/queryDetail', { traceCode }),
  batchGenerate: (data: Record<string, any>) => post('/TraceCode/batchGenerate', data),
  updateStatus: (data: Record<string, any>) => post('/TraceCode/updateStatus', data),
  verifyHash: (traceCode: string) => get('/TraceCode/verifyHash', { traceCode }),
}

// ==================== ColdChain (冷链物流) ====================
export const coldChainApi = {
  // Warehouse
  queryWarehouse: (warehouseName: string) => get('/ColdChain/queryWarehouse', { warehouseName }),
  queryWarehouseByUuid: (warehouseUuid: string) => get('/ColdChain/queryWarehouseByUuid', { warehouseUuid }),
  listWarehouse: (params: Record<string, any>) => get('/ColdChain/listWarehouse', params),
  createWarehouse: (data: Record<string, any>) => post('/ColdChain/createWarehouse', data),
  updateWarehouse: (data: Record<string, any>) => post('/ColdChain/updateWarehouse', data),
  deleteWarehouse: (warehouseId: number) => post('/ColdChain/deleteWarehouse', { warehouseId }),

  // Vehicle
  queryVehicle: (plateNo: string) => get('/ColdChain/queryVehicle', { plateNo }),
  listVehicleByOwner: (ownerId: string) => get('/ColdChain/listVehicleByOwner', { ownerId }),
  listVehicle: (params: Record<string, any>) => get('/ColdChain/listVehicle', params),
  createVehicle: (data: Record<string, any>) => post('/ColdChain/createVehicle', data),
  updateVehicle: (data: Record<string, any>) => post('/ColdChain/updateVehicle', data),
  updateVehicleStatus: (vehicleId: number, vehicleStatus: number) =>
    post('/ColdChain/updateVehicleStatus', { vehicleId, vehicleStatus }),
  deleteVehicle: (vehicleId: number) => post('/ColdChain/deleteVehicle', { vehicleId }),

  // Transport
  queryTransport: (orderNo: string) => get('/ColdChain/queryTransport', { orderNo }),
  listTransportByBatch: (prodBatchNo: string) => get('/ColdChain/listTransportByBatch', { prodBatchNo }),
  listTransportByPlate: (plateNo: string) => get('/ColdChain/listTransportByPlate', { plateNo }),
  listTransport: (params: Record<string, any>) => get('/ColdChain/listTransport', params),
  createTransport: (data: Record<string, any>) => post('/ColdChain/createTransport', data),
  updateTransport: (data: Record<string, any>) => post('/ColdChain/updateTransport', data),
  departTransport: (transportId: number) => post('/ColdChain/departTransport', { transportId }),
  arriveTransport: (transportId: number) => post('/ColdChain/arriveTransport', { transportId }),
  alertTransport: (transportId: number) => post('/ColdChain/alertTransport', { transportId }),
  closeTransport: (transportId: number) => post('/ColdChain/closeTransport', { transportId }),
  deleteTransport: (transportId: number) => post('/ColdChain/deleteTransport', { transportId }),

  // Temp Humidity
  listTempHumidity: (orderNo: string) => get('/ColdChain/listTempHumidity', { orderNo }),
  listTempHumidityAbnormal: (isAbnormal?: number) => get('/ColdChain/listTempHumidityAbnormal', { isAbnormal }),
  recordTempHumidity: (data: Record<string, any>) => post('/ColdChain/recordTempHumidity', data),

  // Transport Node
  listTransportNode: (orderNo: string) => get('/ColdChain/listTransportNode', { orderNo }),
  listNodeByType: (nodeType: number) => get('/ColdChain/listNodeByType', { nodeType }),
  recordNode: (data: Record<string, any>) => post('/ColdChain/recordNode', data),
  updateNode: (data: Record<string, any>) => post('/ColdChain/updateNode', data),

  // Receipt
  queryReceipt: (orderNo: string) => get('/ColdChain/queryReceipt', { orderNo }),
  signReceipt: (data: Record<string, any>) => post('/ColdChain/signReceipt', data),
  updateReceipt: (data: Record<string, any>) => post('/ColdChain/updateReceipt', data),

  // Trace
  traceColdChain: (orderNo: string) => get('/ColdChain/traceColdChain', { orderNo }),
  traceByProdBatch: (prodBatchNo: string) => get('/ColdChain/traceByProdBatch', { prodBatchNo }),
}

// ==================== Sales (销售) ====================
export const salesApi = {
  // Terminal
  queryTerminal: (terminalCode: string) => get('/Sales/queryTerminal', { terminalCode }),
  listTerminal: (params: Record<string, any>) => get('/Sales/listTerminal', params),
  listByOperator: (operatorId: string) => get('/Sales/listByOperator', { operatorId }),
  createTerminal: (data: Record<string, any>) => post('/Sales/createTerminal', data),
  updateTerminal: (data: Record<string, any>) => post('/Sales/updateTerminal', data),
  deleteTerminal: (terminalId: number) => post('/Sales/deleteTerminal', { terminalId }),

  // Stock
  listStock: (terminalId: string) => get('/Sales/listStock', { terminalId }),
  listStockByBatch: (prodBatchNo: string) => get('/Sales/listStockByBatch', { prodBatchNo }),
  stockIn: (data: Record<string, any>) => post('/Sales/stockIn', data),
  updateStock: (data: Record<string, any>) => post('/Sales/updateStock', data),

  // Storage
  queryStorage: (terminalCode: string) => get('/Sales/queryStorage', { terminalCode }),
  updateStorage: (data: Record<string, any>) => post('/Sales/updateStorage', data),

  // Supplement
  listSupplement: (traceBatchNo: string) => get('/Sales/listSupplement', { traceBatchNo }),
  listSupplementByTerminal: (terminalCode: string) => get('/Sales/listSupplementByTerminal', { terminalCode }),
  supplementInfo: (data: Record<string, any>) => post('/Sales/supplementInfo', data),
  updateSupplement: (data: Record<string, any>) => post('/Sales/updateSupplement', data),

  // Anti Fraud
  antiFraudCheck: (area: string) => get('/Sales/antiFraudCheck', { area }),
  runAntiFraud: () => post('/Sales/runAntiFraud'),
}

// ==================== Audit (审计日志，regulation 后端 :8081) ====================
export const auditApi = {
  list: (params: Record<string, any>) => get('/api/audit', params),
  detail: (logId: number) => get(`/api/audit/${logId}`),
  verifyChain: () => get('/api/audit/verify-chain'),
  archive: () => post('/api/audit/archive'),
  write: (data: Record<string, any>) => postJson('/api/audit', data),
}

// ==================== Enterprise (企业资质，regulation 后端 :8081) ====================
export const enterpriseApi = {
  list: (params: Record<string, any>) => get('/api/enterprise', params),
  detail: (enterpriseId: number) => get(`/api/enterprise/${enterpriseId}`),
  listByRisk: (level: number) => get(`/api/enterprise/risk/${level}`),
  create: (data: Record<string, any>) => postJson('/api/enterprise', data),
  update: (enterpriseId: number, data: Record<string, any>) => put(`/api/enterprise/${enterpriseId}`, data),
  delete: (enterpriseId: number) => del(`/api/enterprise/${enterpriseId}`),
  checkStatus: () => post('/api/enterprise/check-status'),
}

// ==================== Trace (监管全链追溯，regulation 后端 :8081) ====================
export const traceApi = {
  getByCode: (traceCode: string) => get(`/api/trace/code/${encodeURIComponent(traceCode)}`),
  listByBatch: (batchNo: string) => get(`/api/trace/batch/${encodeURIComponent(batchNo)}`),
  listByEnterprise: (enterpriseUuid: string) => get(`/api/trace/enterprise/${encodeURIComponent(enterpriseUuid)}`),
  verifyHash: (traceCode: string) => get(`/api/trace/verify/${encodeURIComponent(traceCode)}`),
  disable: (traceCode: string, reason: string) => put(`/api/trace/disable/${encodeURIComponent(traceCode)}?reason=${encodeURIComponent(reason)}`),
  void: (traceCode: string, reason: string) => put(`/api/trace/void/${encodeURIComponent(traceCode)}?reason=${encodeURIComponent(reason)}`),
}
