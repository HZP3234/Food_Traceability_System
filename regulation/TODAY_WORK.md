# 今日完成工作 (2026-06-23)

**负责人**: JYZ  
**模块**: regulation（监管端）

---

## 一、项目环境搭建

| 事项 | 内容 |
|------|------|
| 项目仓库 | `https://github.com/HZP3234/Food_Traceability_System.git` |
| 数据库 | MySQL 8.0 → `fts` 库 |
| 开发模块 | `regulation/`（监管端） |
| 技术栈 | Spring Boot 4.1.0 + MyBatis Plus 3.5.9 + MySQL + Spring Security + JWT + Swagger |

## 二、需求规约学习

学习需求文档《02.第03组-需求规约-食品溯源系统.docx》，明确监管端负责以下三个功能模块：

1. **企业资质管理**（需求 3.2.9）
2. **监管全链追溯**（需求 3.2.10）
3. **操作日志审计**（需求 3.2.11）

## 三、包结构搭建

```
regulation/src/main/java/com/foodtraceability/regulation/
├── common/              # 公共类
│   ├── Result.java               # 统一响应封装
│   ├── BusinessException.java    # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理
├── config/              # 配置类
│   ├── MyBatisPlusConfig.java    # MyBatis Plus + 分页插件
│   └── SecurityConfig.java       # Spring Security 配置
├── controller/          # 控制器
│   ├── EnterpriseController.java # 企业资质管理
│   ├── TraceController.java      # 溯源管理
│   └── AuditLogController.java   # 审计日志
├── entity/              # 实体类（已对齐数据库表字段）
│   ├── Enterprise.java           # t_enterprise
│   ├── QualificationFile.java    # t_qualification
│   ├── TraceCode.java            # t_trace_code
│   └── AuditLog.java             # t_audit_log
├── mapper/              # 数据访问层
│   ├── EnterpriseMapper.java
│   ├── QualificationFileMapper.java
│   ├── TraceCodeMapper.java
│   └── AuditLogMapper.java
├── security/            # 安全工具
│   └── JwtUtil.java             # JWT 生成/解析
└── service/             # 业务逻辑层
    ├── EnterpriseService.java
    ├── TraceCodeService.java
    ├── AuditLogService.java
    └── impl/
        ├── EnterpriseServiceImpl.java
        ├── TraceCodeServiceImpl.java
        └── AuditLogServiceImpl.java
```

## 四、完成的 API 接口

### 4.1 企业资质管理 — `/api/enterprise`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/enterprise` | 分页查询企业列表（支持企业名称、企业类型筛选） |
| GET | `/api/enterprise/{enterpriseId}` | 查看企业详情 |
| GET | `/api/enterprise/risk/{level}` | 按风险等级筛选企业 |
| POST | `/api/enterprise` | 新增企业 |
| PUT | `/api/enterprise/{enterpriseId}` | 修改企业信息 |
| DELETE | `/api/enterprise/{enterpriseId}` | 逻辑删除企业 |
| POST | `/api/enterprise/check-status` | 手动触发资质状态检查 |

### 4.2 监管全链追溯 — `/api/trace`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/trace/code/{traceCode}` | 按溯源码精确查询 |
| GET | `/api/trace/batch/{batchNo}` | 按批次号查询溯源码列表 |
| GET | `/api/trace/enterprise/{enterpriseUuid}` | 按企业查询溯源码列表 |
| GET | `/api/trace/verify/{traceCode}` | 校验溯源码内容 Hash 完整性 |
| PUT | `/api/trace/disable/{traceCode}` | 禁用溯源码 |
| PUT | `/api/trace/void/{traceCode}` | 作废溯源码 |

### 4.3 操作日志审计 — `/api/audit`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/audit` | 分页查询审计日志（支持操作人、操作类型、时间范围筛选） |
| GET | `/api/audit/{logId}` | 查看日志详情（含操作前后数据对比） |
| POST | `/api/audit` | 手工写入审计日志（自动计算 SHA-256 Hash 链） |
| GET | `/api/audit/verify-chain` | 校验日志 Hash 链完整性（防篡改检测） |
| POST | `/api/audit/archive` | 手动触发日志归档（12 个月以前的日志） |

## 五、数据库映射

数据库 `fts` 已有完整表结构和测试数据，Java 实体已与以下表对齐：

| 实体 | 数据库表 | 数据量 |
|------|----------|--------|
| Enterprise | t_enterprise | 12 条 |
| QualificationFile | t_qualification | 多条 |
| TraceCode | t_trace_code | 多条 |
| AuditLog | t_audit_log | 15 条 |

## 六、服务地址

- 应用端口: `8081`
- Swagger 文档: http://localhost:8081/swagger-ui.html
