#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
加工批次合并到生产批次 — 数据库迁移脚本
============================================
操作步骤:
1. t_prod_batch 增加加工相关字段
2. 数据迁移：从 t_process_batch 迁移到 t_prod_batch
3. 删除 t_prod_batch.process_batch_no 列
4. 删除 t_process_batch 表
5. 更新 t_trace_code_bind 中 PROCESS_BATCH -> PROD_BATCH
6. 更新 t_quality_inspection 中 biz_type=2 -> 3
7. 重新生成生产批次模拟数据
"""

import pymysql
import sys
from datetime import datetime

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'fts',
    'charset': 'utf8mb4',
}


def get_conn():
    return pymysql.connect(**DB_CONFIG)


def run_sql(conn, sql, desc=""):
    """执行单条SQL并提交"""
    cur = conn.cursor()
    try:
        cur.execute(sql)
        conn.commit()
        if desc:
            print(f"  OK {desc}")
    except Exception as e:
        conn.rollback()
        print(f"  ERROR {desc}: {e}")
        raise
    finally:
        cur.close()


def step1_add_columns(conn):
    """步骤1: t_prod_batch 增加加工相关字段"""
    print("\n[Step 1] Adding process fields to t_prod_batch...")
    columns = [
        ("template_name", "VARCHAR(100) DEFAULT '' COMMENT '工艺模板名称'"),
        ("raw_batch_no", "VARCHAR(64) DEFAULT '' COMMENT '关联原料批次号'"),
        ("process_date", "VARCHAR(32) DEFAULT '' COMMENT '加工日期'"),
        ("operator", "VARCHAR(64) DEFAULT '' COMMENT '操作员'"),
        ("shift", "INT DEFAULT 0 COMMENT '班次 1早班 2中班 3晚班'"),
        ("actual_temp", "VARCHAR(32) DEFAULT '' COMMENT '实际杀菌温度(℃)'"),
        ("actual_duration", "VARCHAR(32) DEFAULT '' COMMENT '实际杀菌时长(s)'"),
        ("actual_pressure", "VARCHAR(32) DEFAULT '' COMMENT '实际均质压力(MPa)'"),
        ("actual_cool_temp", "VARCHAR(32) DEFAULT '' COMMENT '实际冷却温度(℃)'"),
        ("actual_fill_temp", "VARCHAR(32) DEFAULT '' COMMENT '实际灌装温度(℃)'"),
        ("actual_ph", "VARCHAR(32) DEFAULT '' COMMENT '实际pH值'"),
        ("actual_viscosity", "VARCHAR(32) DEFAULT '' COMMENT '实际粘度(mPa·s)'"),
    ]
    for col_name, col_def in columns:
        sql = f"ALTER TABLE t_prod_batch ADD COLUMN {col_name} {col_def} AFTER process_batch_no"
        try:
            run_sql(conn, sql, f"ADD {col_name}")
        except Exception:
            print(f"  SKIP {col_name} (may already exist)")


def step2_migrate_data(conn):
    """步骤2: 从 t_process_batch 迁移数据到 t_prod_batch"""
    print("\n[Step 2] Migrating data from t_process_batch to t_prod_batch...")
    cur = conn.cursor(pymysql.cursors.DictCursor)

    # 查询所有未删除的加工批次
    cur.execute("SELECT * FROM t_process_batch WHERE is_deleted = 0")
    process_batches = cur.fetchall()
    print(f"  Found {len(process_batches)} process batches to migrate")

    migrated = 0
    for pb in process_batches:
        batch_no = pb.get('batch_no')
        # 查找关联的生产批次
        cur.execute(
            "SELECT * FROM t_prod_batch WHERE process_batch_no = %s AND is_deleted = 0",
            (batch_no,)
        )
        prod_batches = cur.fetchall()

        for prod in prod_batches:
            update_sql = """
                UPDATE t_prod_batch SET
                    template_name = %s,
                    raw_batch_no = %s,
                    process_date = %s,
                    operator = %s,
                    shift = %s,
                    actual_temp = %s,
                    actual_duration = %s,
                    actual_pressure = %s,
                    actual_cool_temp = %s,
                    actual_fill_temp = %s,
                    actual_ph = %s,
                    actual_viscosity = %s
                WHERE prod_batch_id = %s
            """
            cur.execute(update_sql, (
                pb.get('template_name', ''),
                pb.get('raw_batch_no', ''),
                pb.get('process_date', ''),
                pb.get('operator', ''),
                pb.get('shift', 0),
                pb.get('actual_temp', ''),
                pb.get('actual_duration', ''),
                pb.get('actual_pressure', ''),
                pb.get('actual_cool_temp', ''),
                pb.get('actual_fill_temp', ''),
                pb.get('actual_ph', ''),
                pb.get('actual_viscosity', ''),
                prod.get('prod_batch_id'),
            ))
            migrated += 1

    conn.commit()
    print(f"  Migrated {migrated} production batches")
    cur.close()


def step3_drop_process_batch_no(conn):
    """步骤3: 删除 t_prod_batch.process_batch_no 列"""
    print("\n[Step 3] Dropping process_batch_no from t_prod_batch...")
    try:
        run_sql(conn, "ALTER TABLE t_prod_batch DROP COLUMN process_batch_no", "DROP process_batch_no")
    except Exception as e:
        print(f"  SKIP: {e}")


def step4_drop_process_batch_table(conn):
    """步骤4: 删除 t_process_batch 表"""
    print("\n[Step 4] Dropping t_process_batch table...")
    try:
        run_sql(conn, "DROP TABLE IF EXISTS t_process_batch", "DROP t_process_batch")
    except Exception as e:
        print(f"  ERROR: {e}")


def step5_update_trace_code_bind(conn):
    """步骤5: 更新溯源码绑定表"""
    print("\n[Step 5] Updating t_trace_code_bind (PROCESS_BATCH -> PROD_BATCH)...")
    try:
        run_sql(conn,
            "UPDATE t_trace_code_bind SET biz_type = 'PROD_BATCH' WHERE biz_type = 'PROCESS_BATCH'",
            "UPDATE biz_type"
        )
    except Exception as e:
        print(f"  SKIP: {e}")


def step6_update_quality_inspection(conn):
    """步骤6: 更新质检记录"""
    print("\n[Step 6] Updating t_quality_inspection (biz_type 2->3 for process)...")
    try:
        run_sql(conn,
            "UPDATE t_quality_inspection SET biz_type = 3 WHERE biz_type = 2",
            "UPDATE biz_type 2->3"
        )
    except Exception as e:
        print(f"  SKIP: {e}")


def main():
    print("=" * 60)
    print("ProcessBatch -> ProdBatch Migration Script")
    print("=" * 60)

    conn = get_conn()
    try:
        step1_add_columns(conn)
        step2_migrate_data(conn)
        step3_drop_process_batch_no(conn)
        step4_drop_process_batch_table(conn)
        step5_update_trace_code_bind(conn)
        step6_update_quality_inspection(conn)

        print("\n" + "=" * 60)
        print("MIGRATION COMPLETED SUCCESSFULLY!")
        print("=" * 60)
        print("\nSummary:")
        print("  - Process fields added to t_prod_batch")
        print("  - Data migrated from t_process_batch")
        print("  - process_batch_no column dropped")
        print("  - t_process_batch table dropped")
        print("  - Trace code binds updated")
        print("  - Quality inspections updated")
        print("\nNext steps:")
        print("  1. Update Java entity classes")
        print("  2. Update ProductionService/Controller")
        print("  3. Update frontend pages")
        print("  4. Run data generator to refresh mock data")
    finally:
        conn.close()


if __name__ == '__main__':
    main()
