#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
资质表添加 audit_remark 字段 — 2025-06-26
=========================================
为 t_qualification 表新增 audit_remark 列（审核意见/备注），
与 QualificationFile 实体和 Mapper XML 对齐。
"""

import pymysql

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
    cur = conn.cursor()
    try:
        cur.execute(sql)
        conn.commit()
        if desc:
            print(f"  OK {desc}")
    except pymysql.err.OperationalError as e:
        conn.rollback()
        if "Duplicate column name" in str(e):
            print(f"  SKIP {desc}: column already exists")
        else:
            print(f"  ERROR {desc}: {e}")
            raise
    except Exception as e:
        conn.rollback()
        print(f"  ERROR {desc}: {e}")
        raise
    finally:
        cur.close()


def main():
    print("=" * 60)
    print("资质表添加 audit_remark 字段 (2025-06-26)")
    print("=" * 60)

    conn = get_conn()
    try:
        # 新增 audit_remark 列：审核意见/备注，VARCHAR(500) 可为空
        run_sql(conn,
            "ALTER TABLE t_qualification ADD COLUMN audit_remark VARCHAR(500) NULL COMMENT '审核意见/备注' AFTER audit_state",
            "ADD COLUMN audit_remark VARCHAR(500) AFTER audit_state"
        )

        print("\n" + "=" * 60)
        print("audit_remark 字段添加完成!")
        print("=" * 60)
    finally:
        conn.close()


if __name__ == '__main__':
    main()
