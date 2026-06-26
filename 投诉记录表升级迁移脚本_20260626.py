#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
投诉记录表 t_complaint_record 结构升级
将 customers 模块创建的旧表结构升级为 regulation 模块所需的结构
"""

import pymysql

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

print("=== t_complaint_record 表结构升级 ===")

# 1. 检查表是否存在
cursor.execute("SHOW TABLES LIKE 't_complaint_record'")
exists = cursor.fetchone()
if not exists:
    print("表 t_complaint_record 不存在，创建新表...")
    cursor.execute("""
        CREATE TABLE t_complaint_record (
            complaint_record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
            complaint_no VARCHAR(32) NOT NULL,
            trace_code VARCHAR(64),
            batch_number VARCHAR(64),
            enterprise_uuid VARCHAR(64),
            enterprise_name VARCHAR(128),
            consumer_uuid VARCHAR(64),
            complainant_name VARCHAR(64),
            phone VARCHAR(20),
            is_anonymous TINYINT DEFAULT 0,
            complaint_type TINYINT NOT NULL,
            description TEXT,
            photo_urls VARCHAR(1024),
            priority TINYINT DEFAULT 1,
            status TINYINT DEFAULT 1,
            handler_id VARCHAR(64),
            handler_name VARCHAR(64),
            handling_conclusion TEXT,
            handling_proof_urls VARCHAR(1024),
            submit_time DATETIME,
            accept_time DATETIME,
            close_time DATETIME,
            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            create_by VARCHAR(64),
            update_by VARCHAR(64),
            is_deleted TINYINT DEFAULT 0,
            INDEX idx_complaint_no (complaint_no),
            INDEX idx_enterprise_uuid (enterprise_uuid),
            INDEX idx_consumer_uuid (consumer_uuid),
            INDEX idx_status (status),
            INDEX idx_batch_number (batch_number)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)
    conn.commit()
    print("  表创建成功")
else:
    # 2. 表存在，检查并添加缺失的列
    cursor.execute("SHOW COLUMNS FROM t_complaint_record")
    existing_cols = {row[0] for row in cursor.fetchall()}
    print(f"  现有列: {existing_cols}")

    # 需要添加的列定义 (列名, SQL定义)
    needed_columns = [
        ('complaint_record_id', 'BIGINT AUTO_INCREMENT'),
        ('trace_code', 'VARCHAR(64)'),
        ('batch_number', 'VARCHAR(64)'),
        ('enterprise_uuid', 'VARCHAR(64)'),
        ('enterprise_name', 'VARCHAR(128)'),
        ('consumer_uuid', 'VARCHAR(64)'),
        ('complainant_name', 'VARCHAR(64)'),
        ('is_anonymous', 'TINYINT DEFAULT 0'),
        ('photo_urls', 'VARCHAR(1024)'),
        ('priority', 'TINYINT DEFAULT 1'),
        ('handler_id', 'VARCHAR(64)'),
        ('handler_name', 'VARCHAR(64)'),
        ('handling_conclusion', 'TEXT'),
        ('handling_proof_urls', 'VARCHAR(1024)'),
        ('submit_time', 'DATETIME'),
        ('accept_time', 'DATETIME'),
        ('close_time', 'DATETIME'),
        ('create_by', 'VARCHAR(64)'),
        ('update_by', 'VARCHAR(64)'),
        ('is_deleted', 'TINYINT DEFAULT 0'),
    ]

    # 重命名旧列映射
    rename_map = {
        'id': 'complaint_record_id',
        'product_batch_no': 'batch_number',
        'consumer_name': 'complainant_name',
        'consumer_phone': 'phone',
        'image_urls': 'photo_urls',
        'complaint_content': 'description',
        'deleted': 'is_deleted',
    }

    for old_name, new_name in rename_map.items():
        if old_name in existing_cols and new_name not in existing_cols:
            try:
                cursor.execute(f"ALTER TABLE t_complaint_record CHANGE COLUMN `{old_name}` `{new_name}` VARCHAR(1024)")
                existing_cols.discard(old_name)
                existing_cols.add(new_name)
                print(f"  重命名: {old_name} -> {new_name}")
            except Exception as e:
                print(f"  重命名 {old_name} -> {new_name} 失败: {e}")

    for col_name, col_def in needed_columns:
        if col_name not in existing_cols:
            try:
                # 特殊处理: complaint_record_id 作为主键
                if col_name == 'complaint_record_id':
                    # 先检查是否有其他主键
                    cursor.execute("SHOW KEYS FROM t_complaint_record WHERE Key_name = 'PRIMARY'")
                    pk = cursor.fetchone()
                    if not pk:
                        cursor.execute(f"ALTER TABLE t_complaint_record ADD COLUMN `{col_name}` {col_def} FIRST")
                        cursor.execute(f"ALTER TABLE t_complaint_record ADD PRIMARY KEY (`{col_name}`)")
                        print(f"  添加: {col_name} (主键)")
                    else:
                        print(f"  跳过: {col_name} (已有主键 {pk[4]})")
                elif col_name == 'phone' and 'phone' not in existing_cols:
                    cursor.execute(f"ALTER TABLE t_complaint_record ADD COLUMN `phone` VARCHAR(20)")
                    print(f"  添加: phone")
                elif col_name == 'description' and 'description' not in existing_cols:
                    cursor.execute(f"ALTER TABLE t_complaint_record ADD COLUMN `description` TEXT")
                    print(f"  添加: description")
                else:
                    cursor.execute(f"ALTER TABLE t_complaint_record ADD COLUMN `{col_name}` {col_def}")
                    print(f"  添加: {col_name}")
            except Exception as e:
                print(f"  添加 {col_name} 失败: {e}")

    # 删除 customers 专用但 regulation 不需要的列 (保留它们也没关系)
    # product_name, complaint_title, feedback_content, feedback_time, feedback_by 可以保留不影响

    # 修复 NULL is_deleted 值
    cursor.execute("UPDATE t_complaint_record SET is_deleted = 0 WHERE is_deleted IS NULL")
    affected = cursor.rowcount
    if affected > 0:
        print(f"  修复: {affected} 条记录的 is_deleted 从 NULL 改为 0")

    conn.commit()
    print("  表结构升级完成")

# 3. 验证最终结构
print("\n=== 最终表结构 ===")
cursor.execute("DESCRIBE t_complaint_record")
for row in cursor.fetchall():
    print(f"  {row[0]:30s} {row[1]:20s} {row[2]:10s} {row[3]:10s}")

cursor.close()
conn.close()
print("\n完成！请运行 '数据库模拟数据生成器.py' 来填充测试数据。")
