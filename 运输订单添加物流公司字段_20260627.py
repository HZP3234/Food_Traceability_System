#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
食品追溯系统 - 运输订单添加 logistics_company 字段
为 t_cc_transport 表添加物流公司标识列，用于待匹配运输的企业级过滤
"""

import pymysql5

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

print("=" * 60)
print("运输订单添加 logistics_company 字段")
print("=" * 60)

try:
    cursor.execute("""
        ALTER TABLE t_cc_transport
        ADD COLUMN logistics_company VARCHAR(128) NOT NULL DEFAULT ''
        COMMENT '物流公司企业UUID（用于待匹配运输过滤）'
        AFTER transport_status
    """)
    conn.commit()
    print("OK: logistics_company 列已添加")
except pymysql.err.OperationalError as e:
    if "Duplicate column name" in str(e):
        print("SKIP: logistics_company 列已存在，无需重复添加")
    else:
        raise

# 验证
cursor.execute("SELECT COUNT(*) FROM t_cc_transport")
total = cursor.fetchone()[0]
print(f"\n验证: t_cc_transport 共 {total} 行")

print("\n" + "=" * 60)
print("迁移完成！")
print("=" * 60)

conn.close()
