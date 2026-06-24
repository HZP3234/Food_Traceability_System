#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
食品追溯系统 - 系统用户表生成器
创建 t_sys_user 表并插入初始用户，密码使用 bcrypt 加密
"""

import pymysql
import uuid
import bcrypt

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'fts',
    'charset': 'utf8mb4'
}

conn = pymysql.connect(**DB_CONFIG)
cursor = conn.cursor()

# ============================================================
# 1. 建表
# ============================================================
print("创建 t_sys_user 表...")
cursor.execute("""
    CREATE TABLE IF NOT EXISTS t_sys_user (
        user_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
        user_uuid       VARCHAR(64)     NOT NULL                  COMMENT '用户UUID（全局唯一）',
        username        VARCHAR(64)     NOT NULL                  COMMENT '登录用户名',
        password_hash   VARCHAR(255)    NOT NULL                  COMMENT 'bcrypt 加密密码',
        role_type       VARCHAR(20)     NOT NULL DEFAULT 'ENTERPRISE'
                                       COMMENT '角色类型: ADMIN/REGULATOR/ENTERPRISE',
        enterprise_uuid VARCHAR(40)     DEFAULT NULL              COMMENT '关联企业UUID（企业用户时必填）',
        real_name       VARCHAR(50)     NOT NULL                  COMMENT '真实姓名',
        phone           VARCHAR(20)     DEFAULT NULL              COMMENT '手机号',
        email           VARCHAR(100)    DEFAULT NULL              COMMENT '邮箱',
        status          TINYINT(1)      NOT NULL DEFAULT 1        COMMENT '状态: 1-启用 0-禁用',
        last_login_time DATETIME        DEFAULT NULL              COMMENT '最后登录时间',
        create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        create_by       VARCHAR(40)     NOT NULL DEFAULT 'SYSTEM' COMMENT '创建人',
        update_by       VARCHAR(40)     NOT NULL DEFAULT 'SYSTEM' COMMENT '更新人',
        is_deleted      TINYINT(1)      NOT NULL DEFAULT 0        COMMENT '逻辑删除: 0-未删除 1-已删除',
        PRIMARY KEY (user_id),
        UNIQUE KEY uk_user_uuid (user_uuid),
        UNIQUE KEY uk_username  (username)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
      COMMENT='系统用户账号表';
""")
conn.commit()
print("  OK t_sys_user 表创建成功")

# ============================================================
# 2. 插入初始用户
# ============================================================
print("插入初始用户数据...")

common_password = "123456"
common_hash = bcrypt.hashpw(common_password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

users = [
    # (user_uuid, username, role_type, enterprise_uuid, real_name, phone, email)
    ("ADMIN_001", "admin",        "ADMIN",       None,        "系统管理员", "13900000000", "admin@fts.com"),
    ("REG_001",   "regulator1",   "REGULATOR",   None,        "张明",       "13900000001", "zhangming@fts.com"),
    ("REG_002",   "regulator2",   "REGULATOR",   None,        "李华",       "13900000002", "lihua@fts.com"),
    ("ENT_USER_001", "maker1",    "ENTERPRISE",  "ENT_001",   "张明远",     "0571-8812XXXX", "zhangmy@lvye.com"),
    ("ENT_USER_002", "maker2",    "ENTERPRISE",  "ENT_001",   "周建国",     "0571-8812YYYY", "zhoujg@lvye.com"),
    ("ENT_USER_003", "supplier1", "ENTERPRISE",  "ENT_002",   "李建国",     "0512-6632XXXX", "lijg@jinsui.com"),
    ("ENT_USER_004", "logistics1","ENTERPRISE",  "ENT_003",   "王海涛",     "0532-8787XXXX", "wanght@suda.com"),
    ("ENT_USER_005", "seller1",   "ENTERPRISE",  "ENT_004",   "陈晓娟",     "020-8555XXXX", "chenxj@huimin.com"),
]

from datetime import datetime
now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

insert_sql = """
    INSERT IGNORE INTO t_sys_user
        (user_uuid, username, password_hash, role_type, enterprise_uuid,
         real_name, phone, email, status, create_time, update_time, create_by, update_by)
    VALUES
        (%s, %s, %s, %s, %s, %s, %s, %s, 1, %s, %s, 'SYSTEM', 'SYSTEM')
"""

inserted = 0
for (uuid_val, username, role, ent_uuid, name, phone, email) in users:
    cursor.execute(insert_sql, (uuid_val, username, common_hash, role, ent_uuid, name, phone, email, now, now))
    if cursor.rowcount > 0:
        inserted += 1
        print(f"  OK 创建用户: {username} ({role}) -> {name}")
    else:
        print(f"  SKIP 用户已存在: {username}")

conn.commit()
conn.close()

print("\n" + "=" * 60)
print(f"完成！共创建 {inserted} 个用户，默认密码: {common_password}")
print("=" * 60)
