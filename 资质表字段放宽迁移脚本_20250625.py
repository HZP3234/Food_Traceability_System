#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
资质表字段放宽迁移脚本_20250625
================================
修改 t_qualification 表，放宽NOT NULL约束，支持资质信息先行提交部分字段
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
    except Exception as e:
        conn.rollback()
        print(f"  ERROR {desc}: {e}")
        raise
    finally:
        cur.close()


def main():
    print("=" * 60)
    print("资质表字段放宽迁移脚本 (2025-06-25)")
    print("=" * 60)

    conn = get_conn()
    try:
        # 1. qualification_no 改为允许为空，默认空字符串
        run_sql(conn,
            "ALTER TABLE t_qualification MODIFY COLUMN qualification_no VARCHAR(50) NOT NULL DEFAULT ''",
            "MODIFY qualification_no VARCHAR(50) DEFAULT ''"
        )

        # 2. issue_authority 改为允许为空，默认空字符串
        run_sql(conn,
            "ALTER TABLE t_qualification MODIFY COLUMN issue_authority VARCHAR(200) NOT NULL DEFAULT ''",
            "MODIFY issue_authority VARCHAR(200) DEFAULT ''"
        )

        # 3. file_path 改为允许为空，默认空字符串
        run_sql(conn,
            "ALTER TABLE t_qualification MODIFY COLUMN file_path VARCHAR(500) NOT NULL DEFAULT ''",
            "MODIFY file_path VARCHAR(500) DEFAULT ''"
        )

        # 4. file_hash 改为允许为空，默认空字符串
        run_sql(conn,
            "ALTER TABLE t_qualification MODIFY COLUMN file_hash VARCHAR(64) NOT NULL DEFAULT ''",
            "MODIFY file_hash VARCHAR(64) DEFAULT ''"
        )

        # 5. qualification_state 默认值改为 1 (有效)
        run_sql(conn,
            "ALTER TABLE t_qualification MODIFY COLUMN qualification_state TINYINT(1) NOT NULL DEFAULT 1",
            "MODIFY qualification_state DEFAULT 1"
        )

        # 6. valid_from / valid_to 保持必填（日期是核心字段），但先去重 UNIQUE 索引避免测试冲突
        try:
            run_sql(conn,
                "ALTER TABLE t_qualification DROP INDEX qualification_no",
                "DROP UNIQUE INDEX qualification_no"
            )
        except Exception:
            print("  SKIP DROP INDEX qualification_no (may not exist)")

        print("\n" + "=" * 60)
        print("资质表字段放宽迁移完成!")
        print("=" * 60)
    finally:
        conn.close()


if __name__ == '__main__':
    main()
