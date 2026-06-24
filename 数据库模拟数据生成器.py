#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Food Traceability System - Mock Data Generator (Clean Version)
Truncate all tables first, then generate fresh mock data.
"""

import pymysql
import uuid
import random
import hashlib
from datetime import datetime, timedelta, date

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

def gen_uuid():
    return uuid.uuid4().hex[:32]

def gen_hash(seed_str):
    return hashlib.sha256(seed_str.encode()).hexdigest()

def now_str(fmt='%Y-%m-%d %H:%M:%S'):
    return datetime.now().strftime(fmt)

def random_date_s(days_back_max, days_back_min=0):
    days = random.randint(days_back_min, days_back_max)
    return (datetime.now() - timedelta(days=days)).strftime('%Y-%m-%d')

def random_datetime_s(days_back_max, days_back_min=0):
    days = random.randint(days_back_min, days_back_max)
    seconds = random.randint(0, 86400)
    return (datetime.now() - timedelta(days=days, seconds=seconds)).strftime('%Y-%m-%d %H:%M:%S')

def bulk_insert(table, columns, rows):
    if not rows:
        print(f"  ⚠ {table}: no data")
        return
    placeholders = ', '.join(['%s'] * len(columns))
    cols = ', '.join(columns)
    sql = f"INSERT INTO {table} ({cols}) VALUES ({placeholders})"
    try:
        cursor.executemany(sql, rows)
        conn.commit()
        print(f"  OK {table}: {len(rows)} rows")
    except Exception as e:
        print(f"  FAIL {table}: {e}")

# ============================================================
# 1. Truncate all tables
# ============================================================
print("Truncating all tables...")
tables = [
    't_regulatory_trace', 't_rectify_task', 't_audit_log', 't_scan_log',
    't_complaint_handle_log', 't_complaint_record', 't_consumer_info',
    't_sales_supplement', 't_sales_storage', 't_sales_stock', 't_sales_terminal',
    't_cc_receipt', 't_cc_temp_humidity', 't_cc_transport_node', 't_cc_transport', 't_cc_vehicle',
    't_quality_inspection', 't_prod_material_input', 't_prod_env_record',
    't_trace_code',
    't_prod_batch', 't_process_batch', 't_tech_template',
    't_purchase_order', 't_raw_pending', 't_raw_detail', 't_raw_batch',
    't_warehouse', 't_qualification', 't_enterprise',
]
cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
for t in tables:
    cursor.execute(f"TRUNCATE TABLE {t}")
    print(f"  Truncated {t}")
cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
conn.commit()

# ============================================================
# 2. Data definitions
# ============================================================
ENTERPRISES = [
    ('ENT_001', '绿源食品集团有限公司', 2, '91330100MA2B0XXXX1', '浙江省杭州市余杭区仓前街道文一西路1500号', '0571-8812XXXX', '张明远', 1, 1),
    ('ENT_002', '金穗农产品供应有限公司', 1, '91320500MA2B0XXXX2', '江苏省苏州市吴江区盛泽镇西环路88号', '0512-6632XXXX', '李建国', 2, 1),
    ('ENT_003', '速达冷链物流有限公司', 3, '91370200MA2B0XXXX3', '山东省青岛市城阳区流亭街道天河路99号', '0532-8787XXXX', '王海涛', 1, 1),
    ('ENT_004', '惠民连锁超市有限公司', 4, '91440100MA2B0XXXX4', '广东省广州市天河区天河路385号', '020-8555XXXX', '陈晓娟', 1, 1),
    ('ENT_005', '恒丰畜牧养殖专业合作社', 1, '93410600MA2B0XXXX5', '河南省郑州市中牟县大孟镇畜牧园区2号', '0371-6222XXXX', '赵大伟', 2, 1),
    ('ENT_006', '鲜美达食品加工有限公司', 2, '91370200MA2B0XXXX6', '山东省潍坊市寒亭区北海路678号', '0536-7272XXXX', '刘文斌', 1, 1),
]

PRODUCTS = [
    ('PROD_001', '有机西红柿', '蔬菜', '500g/袋'),
    ('PROD_002', '鲜牛肉(冷鲜)', '肉类', '2.5kg/袋'),
    ('PROD_003', '原味酸奶', '乳制品', '200ml/瓶'),
    ('PROD_004', '精选猪五花肉', '肉类', '1kg/盒'),
    ('PROD_005', '有机生菜', '蔬菜', '300g/袋'),
    ('PROD_006', '冷冻鸡胸肉', '肉类', '1kg/袋'),
    ('PROD_007', '纯牛奶(巴氏杀菌)', '乳制品', '250ml/瓶'),
    ('PROD_008', '鲜鸡蛋(散养)', '禽蛋', '15枚/盒'),
]

WAREHOUSES = [
    ('WH_001', '1号冷库', 1, '浙江省杭州市余杭区仓前街道', '500吨', '周建国', '-18℃ ~ -15℃', '75% ~ 85%'),
    ('WH_002', '2号常温库', 2, '浙江省杭州市余杭区仓前街道', '1000吨', '吴志强', '15℃ ~ 25℃', '45% ~ 65%'),
    ('WH_003', '3号冷藏库', 3, '浙江省杭州市余杭区仓前街道', '300吨', '郑国华', '0℃ ~ 4℃', '85% ~ 95%'),
    ('WH_004', '4号保鲜库', 3, '山东省潍坊市寒亭区北海路', '200吨', '孙永康', '2℃ ~ 8℃', '80% ~ 90%'),
]

VEHICLES = [
    ('鲁BA1234', '福田奥铃CTS冷藏车', '王刚', '13800138001', 'ENT_003', '速达冷链物流有限公司', '冷藏', '-5℃ ~ 5℃', 1),
    ('鲁BB5678', '解放J6F冷藏车', '刘强', '13800138002', 'ENT_003', '速达冷链物流有限公司', '冷冻', '-25℃ ~ -15℃', 1),
    ('浙AC9012', '东风天锦冷藏车', '陈飞', '13800138003', 'ENT_003', '速达冷链物流有限公司', '冷藏', '0℃ ~ 8℃', 1),
    ('豫AD3456', '江淮帅铃冷藏车', '张磊', '13800138004', 'ENT_003', '速达冷链物流有限公司', '恒温', '15℃ ~ 20℃', 1),
]

TEMPLATES = [
    ('蔬菜清洗分拣标准工艺', 'V2.1', '有机西红柿,有机生菜', '5℃', '30min', '常压', '2℃', '8℃', 'N/A', '6.5-7.0', 'N/A', 3),
    ('肉品分割包装标准工艺', 'V1.8', '鲜牛肉(冷鲜),精选猪五花肉', '4℃', '45min', '常压', '0℃', '4℃', 'N/A', '5.8-6.2', 'N/A', 3),
    ('乳制品巴氏杀菌工艺', 'V3.0', '原味酸奶,纯牛奶(巴氏杀菌)', '72℃', '15s', '0.3MPa', '4℃', '6℃', '120rpm', '4.2-4.6', '1500cP', 4),
    ('冷冻肉品速冻工艺', 'V1.5', '冷冻鸡胸肉', '-35℃', '4h', '常压', '-18℃', '-15℃', 'N/A', '6.0-6.5', 'N/A', 2),
]

TERMINALS = [
    ('STORE_001', '惠民超市(仓前店)', 1, '华东区', '杭州市余杭区仓前街道文一西路', 'EMP_001', '陈晓娟'),
    ('STORE_002', '惠民超市(西湖店)', 1, '华东区', '杭州市西湖区文三路', 'EMP_002', '林小燕'),
    ('STORE_003', '惠民超市(天河店)', 1, '华南区', '广州市天河区体育西路', 'EMP_003', '黄伟明'),
    ('STORE_004', '惠民超市(市南店)', 2, '华北区', '青岛市市南区香港中路', 'EMP_004', '杨丽华'),
]

CONSUMERS = [
    ('CON_001', '吃货小王', '王小明', '13900139001', '男', '浙江省杭州市'),
    ('CON_002', '美食达人Lucy', '刘露西', '13900139002', '女', '广东省广州市'),
    ('CON_003', '家常菜爱好者', '李芳', '13900139003', '女', '山东省青岛市'),
    ('CON_004', '阳光男孩', '赵阳', '13900139004', '男', '江苏省苏州市'),
    ('CON_005', '健康生活家', '陈美丽', '13900139005', '女', '浙江省杭州市'),
    ('CON_006', '老张的美食日记', '张伟', '13900139006', '男', '河南省郑州市'),
    ('CON_007', '开心果果', '周婷', '13900139007', '女', '山东省潍坊市'),
    ('CON_008', '小吃货一枚', '吴思远', '13900139008', '男', '浙江省杭州市'),
]

COMPLAINT_TYPES = [
    (1, '产品质量问题', '产品存在异味/变色/变质等质量问题'),
    (2, '食品安全问题', '食用后出现腹泻/呕吐等食品安全问题'),
    (3, '包装问题', '包装破损/漏气/标签不清等包装问题'),
    (4, '虚假宣传', '产品描述与实际不符'),
    (5, '其他问题', '其他类型问题'),
]

QUAL_TYPES = [
    (1, '营业执照', '市场监督管理局'),
    (2, '食品生产许可证', '食品药品监督管理局'),
    (3, '食品经营许可证', '食品药品监督管理局'),
    (4, 'ISO22000食品安全管理体系认证', 'SGS认证机构'),
]

OPERATORS = ['张师傅', '李师傅', '王师傅', '赵师傅', '刘师傅', '陈师傅']
LINES = ['1号生产线', '2号生产线', '3号生产线']

# ============================================================
# 3. INSERT DATA
# ============================================================
print("\n" + "=" * 60)
print("Inserting mock data...")
print("=" * 60)

# ---- 3.1 Enterprise ----
print("\n[1] Enterprise, Qualification, Warehouse")
rows = [(e[0], e[1], e[2], e[3], e[4], e[5], e[6], e[7], e[8], None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0) for e in ENTERPRISES]
bulk_insert('t_enterprise', ['enterprise_uuid','enterprise_name','enterprise_type','cert_no','address','contact_phone','contact_person','risk_level','status','remark','create_by','create_time','update_by','update_time','is_deleted'], rows)

# ---- 3.2 Qualification ----
qual_rows = []
for ent in ENTERPRISES:
    for qt, qn, qa in QUAL_TYPES:
        vu = gen_uuid()
        vf = date(2024, 1, 1) + timedelta(days=random.randint(0, 365))
        vt = vf + timedelta(days=365 * random.randint(1, 3))
        vs = 1 if vt > date.today() else (2 if vt < date.today() + timedelta(days=30) else 1)
        qual_rows.append((vu, ent[0], qt, f'{qn[:2]}-{random.randint(100000,999999)}', qa, vf, vt, f'/uploads/qual/{vu}.pdf', gen_hash(f'{vu}{vf}'), vs, 1, now_str(), now_str(), 'admin', 'admin', 0))
bulk_insert('t_qualification', ['qualification_uuid','enterprise_uuid','qualification_type','qualification_no','issue_authority','valid_from','valid_to','file_path','file_hash','qualification_state','audit_state','create_time','update_time','create_by','update_by','is_deleted'], qual_rows)

# ---- 3.3 Warehouse ----
wh_rows = [(w[0], w[1], w[2], w[3], w[4], w[5], w[6], w[7], 1, None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0) for w in WAREHOUSES]
bulk_insert('t_warehouse', ['warehouse_uuid','warehouse_name','warehouse_type','address','capacity','manager','temp_range','humidity_range','warehouse_status','remark','create_by','create_time','update_by','update_time','is_deleted'], wh_rows)

# ---- 3.4 Raw Batch & Detail ----
print("[2] Raw Batch, Detail, Pending, Purchase Order")
raw_batch_nos = []
raw_data = [
    ('有机西红柿', '蔬菜', 500.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_001'),
    ('有机西红柿', '蔬菜', 300.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_001'),
    ('鲜牛肉(冷鲜)', '肉类', 200.00, 'ENT_005', '恒丰畜牧养殖专业合作社', 'WH_003'),
    ('鲜牛肉(冷鲜)', '肉类', 150.00, 'ENT_005', '恒丰畜牧养殖专业合作社', 'WH_003'),
    ('精选猪五花肉', '肉类', 300.00, 'ENT_005', '恒丰畜牧养殖专业合作社', 'WH_003'),
    ('有机生菜', '蔬菜', 200.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_001'),
    ('冷冻鸡胸肉', '肉类', 400.00, 'ENT_005', '恒丰畜牧养殖专业合作社', 'WH_001'),
    ('纯牛奶原料', '乳制品', 1000.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_002'),
    ('有机西红柿', '蔬菜', 600.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_001'),
    ('精选猪五花肉', '肉类', 250.00, 'ENT_005', '恒丰畜牧养殖专业合作社', 'WH_003'),
    ('鲜鸡蛋', '禽蛋', 10000.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_002'),
    ('有机生菜', '蔬菜', 350.00, 'ENT_002', '金穗农产品供应有限公司', 'WH_001'),
]

raw_rows = []
detail_rows = []
origins = ['浙江省杭州市', '江苏省苏州市', '山东省潍坊市', '河南省郑州市', '广东省广州市']
farm_types = ['家庭农场', '合作社', '养殖基地', '大型种植园', '现代化牧场']
feed_types = ['有机饲料', '谷物混合饲料', '天然牧草', '配合饲料']
breeds = ['本地品种', '引进品种', '杂交优质品种']

for i, (pn, pc, amt, sid, sname, wh) in enumerate(raw_data):
    dt = random_datetime_s(90)
    batch_no = f"RB{datetime.strptime(dt, '%Y-%m-%d %H:%M:%S').strftime('%Y%m%d')}{i+1:04d}"
    raw_batch_nos.append(batch_no)
    detail_id = str(i + 1)
    raw_rows.append((batch_no, pn, pc, amt, 'kg' if pc != '禽蛋' else '枚', sid, sname, wh,
                     random.choice([0, 1, 2]), dt[:10], dt[:10], random.choice([1, 1, 1, 2]),
                     random.choice([1, 1, 1, 2, 3]), detail_id, 1,
                     gen_hash(f"raw_{batch_no}"), None, 'SYSTEM', dt, 'SYSTEM', dt, 0))
    sd = random_datetime_s(90)
    detail_rows.append((batch_no, random.choice(origins), random.choice(farm_types),
                        random.choice(feed_types), random.randint(1, 4), f'INSP-{random.randint(10000,99999)}',
                        random.choice(breeds), f'规模: {random.randint(50,5000)}亩/头', sd[:10],
                        random.choice(['鲁BA1234', '浙AC9012', '豫AD3456', None]),
                        f'{random.randint(0,25)}℃', random.choice([0, 1, 2]), sd[:10],
                        '系统导入', sd, None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0))

bulk_insert('t_raw_batch', ['batch_no','product_name','product_category','amount','unit','supplier_id','supplier_name','warehouse','storage_method','shelf_life','purchase_date','check_result','batch_status','detail_id','detail_status','data_hash','remark','create_by','create_time','update_by','update_time','is_deleted'], raw_rows)
bulk_insert('t_raw_detail', ['batch_no','origin','farm_type','feed_type','cert_type','inspection_no','breed','scale_desc','collect_date','plate_no','transport_temp','storage_method','shelf_life','uploader','upload_time','remark','create_by','create_time','update_by','update_time','is_deleted'], detail_rows)

# ---- 3.5 Raw Pending ----
pending_rows = []
for i in range(6):
    dt = random_datetime_s(30)
    matched = random.choice([True, True, False])
    pending_rows.append((
        f"PEND{datetime.strptime(dt, '%Y-%m-%d %H:%M:%S').strftime('%Y%m%d')}{i+1:03d}",
        random.choice(['有机西红柿', '鲜鸡蛋', '有机生菜']),
        random.choice(['蔬菜', '禽蛋', '蔬菜']),
        '金穗农产品供应有限公司',
        random.randint(50, 500),
        str(random.randint(1, 12)),
        random.choice(raw_batch_nos) if matched else '',
        random.choice([1, 2]),
        dt,
        (datetime.strptime(dt, '%Y-%m-%d %H:%M:%S') + timedelta(hours=random.randint(1, 48))).strftime('%Y-%m-%d %H:%M:%S') if matched else None,
        None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0
    ))
bulk_insert('t_raw_pending', ['pending_code','product_name','product_category','supplier_name','amount','raw_detail_id','matched_batch_no','pending_status','upload_time','match_time','remark','create_by','create_time','update_by','update_time','is_deleted'], pending_rows)

# ---- 3.6 Purchase Order ----
order_rows = []
for i in range(8):
    dt = random_date_s(120)
    order_no = f"PO{dt.replace('-', '')}{i+1:03d}"
    sup = random.choice([ENTERPRISES[1], ENTERPRISES[4]])
    order_rows.append((order_no, sup[0], sup[1], random.choice(['蔬菜', '肉类', '乳制品', '禽蛋']),
                       random.choice([1, 2]), random.choice([w[1] for w in WAREHOUSES]),
                       dt, f'/uploads/orders/{order_no}.pdf', random.choice([1, 2, 3]),
                       None, 'SYSTEM', dt + ' 08:00:00', 'SYSTEM', dt + ' 08:00:00', 0))
bulk_insert('t_purchase_order', ['order_no','supplier_id','supplier_name','product_category','import_method','warehouse_name','purchase_date','file_path','order_status','remark','create_by','create_time','update_by','update_time','is_deleted'], order_rows)

# ---- 3.7 Tech Template ----
print("[3] Tech Template, Process Batch, Prod Batch")
tmpl_rows = [(t[0], t[1], t[2], t[3], t[4], t[5], t[6], t[7], t[8], t[9], t[10], t[11], 1, None, 'admin', now_str(), 'admin', now_str(), 0) for t in TEMPLATES]
bulk_insert('t_tech_template', ['template_name','version','applicable_product','target_temp','duration','pressure','cool_temp','fill_temp','stir_speed','ph_value','viscosity','clean_level','template_status','remark','create_by','create_time','update_by','update_time','is_deleted'], tmpl_rows)

# ---- 3.8 Process Batch ----
process_batch_nos = []
process_rows = []
prod_names = ['有机西红柿', '鲜牛肉(冷鲜)', '原味酸奶', '冷冻鸡胸肉', '精选猪五花肉', '有机生菜', '纯牛奶(巴氏杀菌)', '鲜鸡蛋(散养)']
for i in range(10):
    dt = random_datetime_s(60)
    batch_no = f"PCB{datetime.strptime(dt, '%Y-%m-%d %H:%M:%S').strftime('%Y%m%d')}{i+1:04d}"
    process_batch_nos.append(batch_no)
    product = prod_names[i % len(prod_names)]
    tmpl = TEMPLATES[i % len(TEMPLATES)]
    process_rows.append((batch_no, product, tmpl[0], random.choice(raw_batch_nos),
                         random.randint(100, 2000), dt[:10], random.choice(OPERATORS),
                         random.choice(LINES), random.randint(1, 3),
                         f'{random.randint(2,75)}℃', f'{random.randint(10,240)}min',
                         f'{random.uniform(0.1,0.5):.1f}MPa', f'{random.randint(-5,8)}℃',
                         f'{random.randint(2,15)}℃', f'{random.uniform(4.0,7.5):.1f}',
                         f'{random.randint(500,2000)}cP',
                         random.choice([1, 1, 1, 2, 3]),
                         gen_hash(f"proc_{batch_no}"), gen_hash(f"chain_proc_{batch_no}"),
                         None, 'SYSTEM', dt, 'SYSTEM', dt, 0))
bulk_insert('t_process_batch', ['batch_no','product_name','template_name','raw_batch_no','planned_amount','process_date','operator','production_line','shift','actual_temp','actual_duration','actual_pressure','actual_cool_temp','actual_fill_temp','actual_ph','actual_viscosity','batch_status','data_hash','chain_hash','remark','create_by','create_time','update_by','update_time','is_deleted'], process_rows)

# ---- 3.9 Prod Batch ----
# Use a counter to ensure unique batch_no for prod batches
prod_batch_nos = []
prod_rows = []
# Use fixed date offsets to avoid collisions
for i in range(10):
    dt_offset = 60 - i * 5  # spread dates apart
    dt = random_datetime_s(dt_offset)
    batch_no = f"PB{datetime.strptime(dt, '%Y-%m-%d %H:%M:%S').strftime('%Y%m%d')}{i+1:04d}"
    prod_batch_nos.append(batch_no)
    product = prod_names[i % len(prod_names)]
    planned = random.randint(100, 2000)
    actual = int(planned * random.uniform(0.95, 1.05))
    prod_rows.append((batch_no, product, process_batch_nos[i],
                      random.choice(LINES), planned, actual, dt[:10],
                      random.choice([1, 1, 1, 2]),
                      random.choice([0, 1, 1]),
                      random.choice([1, 1, 2, 3]),
                      gen_hash(f"prod_{batch_no}"), gen_hash(f"chain_prod_{batch_no}"),
                      None, 'SYSTEM', dt, 'SYSTEM', dt, 0))
bulk_insert('t_prod_batch', ['batch_no','product_name','process_batch_no','production_line','planned_amount','actual_amount','production_date','check_result','code_status','batch_status','data_hash','chain_hash','remark','create_by','create_time','update_by','update_time','is_deleted'], prod_rows)

# ---- 3.10 Trace Code ----
print("[4] Trace Code, Env Record, Material Input, Quality Inspection")
trace_codes = []
trace_rows = []
for i, pb in enumerate(prod_batch_nos):
    prod = PRODUCTS[i % len(PRODUCTS)]
    ent = ENTERPRISES[i % 2]  # alternate between 2 processors
    count = random.randint(3, 8)
    for j in range(count):
        ts = datetime.now().strftime('%Y%m%d%H%M%S%f')[:17]
        tc = f"TC{ts}{random.randint(1000,9999)}"
        trace_codes.append(tc)
        status = random.choice([1, 1, 1, 2, 3])
        dt = random_datetime_s(30)
        trace_rows.append((
            gen_uuid(), tc, random.choice([1, 1, 1, 2]), random.choice([1, 1, 1, 2, 3]),
            prod[0], prod[1], ent[0], ent[1], status,
            gen_hash(f"content_{tc}"),
            f'PROOF_{random.randint(100000,999999):08X}' if random.random() > 0.3 else None,
            f"0x{gen_hash(f'tx_{tc}')[:40]}" if random.random() > 0.3 else None,
            count, pb, None, None,
            (datetime.now() + timedelta(days=random.randint(180, 365))).strftime('%Y-%m-%d %H:%M:%S') if random.random() > 0.5 else None,
            dt, dt, 'admin', 'admin', 0
        ))
bulk_insert('t_trace_code', ['trace_code_uuid','trace_code','code_type','package_level','product_id','product_name','enterprise_uuid','enterprise_name','trace_code_status','content_hash','proof_id','tx_hash','generate_count','batch_no','disable_reason','void_reason','expire_time','create_time','update_time','create_by','update_by','is_deleted'], trace_rows)

# ---- 3.11 Env Record ----
env_rows = []
for i in range(20):
    dt = random_datetime_s(60)
    env_rows.append((random.choice(LINES), dt, random.choice(['张质检', '李质检', '王质检']),
                     f'{random.randint(18,26)}℃', f'{random.randint(40,70)}%',
                     f'CLASS-{random.choice(["A","B","C"])}', f'{random.randint(5,15)}Pa',
                     f'{random.randint(100,500)}/m³', f'{random.randint(0,50)}CFU/m³',
                     random.choice([0, 0, 0, 0, 1]), None, 'SYSTEM', dt, 'SYSTEM', dt, 0))
bulk_insert('t_prod_env_record', ['production_line','collect_time','collector','workshop_temp','workshop_humidity','clean_level','pressure_diff','particles','bacteria','is_abnormal','remark','create_by','create_time','update_by','update_time','is_deleted'], env_rows)

# ---- 3.12 Material Input ----
input_rows = []
for i in range(15):
    input_rows.append((random.choice(raw_batch_nos) + f'_{i}', random.choice(prod_names),
                       random.randint(10, 500), random.choice(['已投入', '已投入', '已投入', '待投入']),
                       'SYSTEM', now_str(), 'SYSTEM', now_str(), 0))
bulk_insert('t_prod_material_input', ['raw_batch_no','material_name','input_amount','input_status','create_by','create_time','update_by','update_time','is_deleted'], input_rows)

# ---- 3.13 Quality Inspection ----
inspection_rows = []
standards = ['GB 2762-2022', 'GB 2763-2021', 'GB 29921-2021', 'GB 31650-2019', 'GB 7101-2022']
inspectors_list = ['张质检员', '李质检员', '王质检员', '赵质检员']
for i in range(20):
    dt = random_datetime_s(60)
    biz_type = (i % 3) + 1
    if biz_type == 1:
        biz_no = raw_batch_nos[i % len(raw_batch_nos)]
    elif biz_type == 2:
        biz_no = prod_batch_nos[i % len(prod_batch_nos)]
    else:
        biz_no = process_batch_nos[i % len(process_batch_nos)]
    inspection_no = f"INSP{datetime.strptime(dt, '%Y-%m-%d %H:%M:%S').strftime('%Y%m%d')}{i+1:04d}"
    result = random.choice([1, 1, 1, 1, 2, 3])
    inspection_rows.append((inspection_no, biz_type, biz_no, random.randint(1, 4), result,
                            random.choice(inspectors_list), random.choice(standards), dt[:10],
                            random.choice([1, 1, 1, 2]), random.choice([1, 1, 1, 2]),
                            '密封完好' if random.random() > 0.2 else '密封有轻微破损',
                            f'检测完成，结果{"合格" if result==1 else "不合格" if result==2 else "待定"}',
                            None, 'SYSTEM', dt, 'SYSTEM', dt, 0))
bulk_insert('t_quality_inspection', ['inspection_no','biz_type','biz_batch_no','inspection_type','inspection_result','inspector','standard','inspection_date','sensory_check','microbe_check','seal_check','detail_result','remark','create_by','create_time','update_by','update_time','is_deleted'], inspection_rows)

# ---- 3.14 Cold Chain ----
print("[5] Cold Chain: Vehicle, Transport, Node, Temp/Humidity, Receipt")
veh_rows = [(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0) for v in VEHICLES]
bulk_insert('t_cc_vehicle', ['plate_no','vehicle_model','driver_name','driver_phone','owner_id','owner_name','cold_type','temp_range','vehicle_status','remark','create_by','create_time','update_by','update_time','is_deleted'], veh_rows)

transport_order_nos = []
for i in range(6):
    dt = random_datetime_s(30)
    base_dt = datetime.strptime(dt, '%Y-%m-%d %H:%M:%S')
    order_no = f"CTO{base_dt.strftime('%Y%m%d%H%M')}{i+1:03d}"
    transport_order_nos.append(order_no)
    vehicle = VEHICLES[i % len(VEHICLES)]
    prod_name = prod_names[i % len(prod_names)]
    batch = prod_batch_nos[i % len(prod_batch_nos)]
    depart_time = dt
    estimated = base_dt + timedelta(hours=random.randint(2, 48))
    actual = estimated + timedelta(hours=random.randint(-2, 4)) if random.random() > 0.3 else None
    row = ((order_no, vehicle[0], vehicle[2], vehicle[3], prod_name, batch,
            ENTERPRISES[i % 3][0], ENTERPRISES[i % 3][1],
            ENTERPRISES[3 + (i % 2)][0], ENTERPRISES[3 + (i % 2)][1],
            f'{random.randint(-5,8)}℃', depart_time, estimated.strftime('%Y-%m-%d %H:%M:%S'),
            actual.strftime('%Y-%m-%d %H:%M:%S') if actual else None,
            random.choice([1, 2, 3]), f'{random.choice([5,10,15,30])}min',
            round(random.uniform(0, 8), 1), round(random.uniform(-5, 2), 1),
            round(random.uniform(80, 95), 1), round(random.uniform(30, 60), 1),
            'SMS+APP推送', random.choice([1, 1, 1, 2, 3]),
            gen_hash(f"transport_{order_no}"), gen_hash(f"chain_transport_{order_no}"),
            None, 'SYSTEM', dt, 'SYSTEM', dt, 0),)
    bulk_insert('t_cc_transport', ['order_no','plate_no','driver_name','driver_phone','product_name','prod_batch_no','departure_id','departure_name','destination_id','destination_name','loading_temp','depart_time','estimated_arrival','actual_arrival','transport_method','collect_interval','temp_upper','temp_lower','humid_upper','humid_lower','alert_method','transport_status','data_hash','chain_hash','remark','create_by','create_time','update_by','update_time','is_deleted'], row)

# Transport Nodes
node_types = [(1, '装货'), (2, '发车'), (3, '途中检查'), (4, '到达中转'), (5, '到达目的地'), (6, '卸货')]
node_locations = ['杭州仓前物流园', '苏州盛泽中转站', '青岛流亭物流中心', '广州天河配送中心', '郑州中牟集散中心']
node_rows = []
for order_no in transport_order_nos:
    n = random.randint(3, 6)
    for j in range(n):
        dt = random_datetime_s(30)
        node_rows.append((order_no, node_types[j % len(node_types)][0], dt,
                          random.choice(node_locations), random.choice(['王刚', '刘强', '陈飞', '张磊']),
                          1 if j < n - 1 else 2, f'{node_types[j % len(node_types)][1]}完成',
                          'SYSTEM', dt, 'SYSTEM', dt, 0))
bulk_insert('t_cc_transport_node', ['order_no','node_type','node_time','location','operator','node_status','remark','create_by','create_time','update_by','update_time','is_deleted'], node_rows)

# Temp/Humidity
th_rows = []
for order_no in transport_order_nos:
    dt_base = datetime.strptime(random_datetime_s(7), '%Y-%m-%d %H:%M:%S')
    for j in range(random.randint(8, 15)):
        t = round(random.uniform(-3, 6), 1)
        h = round(random.uniform(50, 90), 1)
        is_abn = 1 if (t > 8 or t < -5) else 0
        th_rows.append((order_no, t, h, (dt_base + timedelta(hours=j)).strftime('%Y-%m-%d %H:%M:%S'),
                        f'运输途中-{j+1}号采集点', is_abn,
                        f'温度{t}℃超出范围[-5,8]℃' if is_abn else None,
                        'SYSTEM', now_str(), 'SYSTEM', now_str(), 0))
bulk_insert('t_cc_temp_humidity', ['order_no','temperature','humidity','record_time','location_desc','is_abnormal','abnormal_reason','create_by','create_time','update_by','update_time','is_deleted'], th_rows)

# Receipt
receipt_rows = []
for order_no in transport_order_nos:
    dt = random_datetime_s(7)
    receipt_rows.append((order_no, random.choice(['陈晓娟', '林小燕', '黄伟明', '杨丽华']),
                         dt, f'{random.randint(-2,6)}℃', 1, random.choice([1, 1, 1, 2]),
                         '数量差异: +2件' if random.random() > 0.8 else None, None,
                         'SYSTEM', dt, 'SYSTEM', dt, 0))
bulk_insert('t_cc_receipt', ['order_no','signer','sign_time','sign_temp','is_package_intact','qty_match','qty_mismatch_desc','remark','create_by','create_time','update_by','update_time','is_deleted'], receipt_rows)

# ---- 3.15 Sales ----
print("[6] Sales Terminal, Stock, Storage, Supplement")
term_rows = [(t[0], t[1], t[2], t[3], t[4], t[5], t[6], 1, None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0) for t in TERMINALS]
bulk_insert('t_sales_terminal', ['terminal_code','terminal_name','terminal_type','area','address','operator_id','operator_name','terminal_status','remark','create_by','create_time','update_by','update_time','is_deleted'], term_rows)

stock_rows = []
for i in range(15):
    dt = random_datetime_s(30)
    terminal = TERMINALS[i % len(TERMINALS)]
    batch = prod_batch_nos[i % len(prod_batch_nos)]
    stock_rows.append((
        f"STK{datetime.strptime(dt, '%Y-%m-%d %H:%M:%S').strftime('%Y%m%d')}{i+1:03d}",
        terminal[0], terminal[1], f'PB{i+1:03d}', batch, prod_names[i % len(prod_names)],
        random.randint(10, 200), dt,
        (datetime.strptime(dt, '%Y-%m-%d %H:%M:%S') + timedelta(days=random.randint(1, 14))).strftime('%Y-%m-%d %H:%M:%S'),
        random.choice([1, 1, 2]), None, 'SYSTEM', dt, 'SYSTEM', dt, 0
    ))
bulk_insert('t_sales_stock', ['stock_code','terminal_id','terminal_name','prod_batch_id','prod_batch_no','product_name','quantity','received_time','last_check_time','stock_status','remark','create_by','create_time','update_by','update_time','is_deleted'], stock_rows)

storage_rows = []
for i, t in enumerate(TERMINALS):
    for j in range(random.randint(2, 3)):
        storage_rows.append((
            t[0], random.choice(['超市', '便利店', '专卖店']), random.choice(['蔬菜', '肉类', '乳制品', '禽蛋']),
            f'{random.choice([2,4,8,25])}℃', f'{random.randint(30,90)}%',
            random.choice([0, 1, 2]), random.choice([0, 1]),
            f'{random.randint(7,365)}天', f'LOC-{t[0]}-{j+1:02d}', t[6],
            random.choice([0, 1]), None, 'SYSTEM', now_str(), 'SYSTEM', now_str(), 0
        ))
bulk_insert('t_sales_storage', ['terminal_code','terminal_type','product_type','temperature','humidity','storage_method','light_condition','shelf_life','location_code','operator','is_auto_filled','remark','create_by','create_time','update_by','update_time','is_deleted'], storage_rows)

supp_rows = []
for i in range(8):
    dt = random_datetime_s(30)
    base_dt = datetime.strptime(dt, '%Y-%m-%d %H:%M:%S')
    t = TERMINALS[i % len(TERMINALS)]
    supp_rows.append((
        f"SUP{base_dt.strftime('%Y%m%d')}{i+1:03d}",
        f"TB{base_dt.strftime('%Y%m%d')}{i+1:03d}",
        prod_batch_nos[i % len(prod_batch_nos)],
        t[0], t[1],
        random.choice(['惠民连锁超市有限公司', '鲜美达食品加工有限公司']),
        f"SB{base_dt.strftime('%Y%m%d')}{i+1:03d}",
        f'{random.choice([2,4,8,25])}℃', f'{random.randint(40,80)}%',
        random.choice([0, 1, 2]), random.choice([0, 1]),
        f'{random.randint(30,180)}天', f'LOC-SUP-{i+1:03d}',
        random.randint(20, 300), dt, t[6],
        random.choice([1, 1, 2]),
        gen_hash(f"supp_{i}_{dt}"), gen_hash(f"chain_supp_{i}_{dt}"),
        None, 'SYSTEM', dt, 'SYSTEM', dt, 0
    ))
bulk_insert('t_sales_supplement', ['supplement_code','trace_batch_id','trace_batch_no','terminal_code','terminal_name','sales_company','sales_batch_no','temperature','humidity','storage_method','light_condition','shelf_life','location_code','quantity','inbound_time','operator','supplement_status','data_hash','chain_hash','remark','create_by','create_time','update_by','update_time','is_deleted'], supp_rows)

# ---- 3.16 Consumer, Complaint, Scan ----
print("[7] Consumer, Complaint, Handle Log, Scan Log")
consumer_rows = [(c[0], c[1], c[2], c[3], c[4], c[5], random_datetime_s(30), random.randint(3, 50), random.randint(0, 3), 1, now_str(), now_str(), 'SYSTEM', 'SYSTEM', 0) for c in CONSUMERS]
bulk_insert('t_consumer_info', ['consumer_uuid','nick_name','real_name','phone','gender','region','last_scan_time','total_scans','complaint_count','status','create_time','update_time','create_by','update_by','is_deleted'], consumer_rows)

complaint_nos = []
complaint_rows = []
for i in range(12):
    dt = random_datetime_s(30)
    base_dt = datetime.strptime(dt, '%Y-%m-%d %H:%M:%S')
    complaint_no = f"CP{base_dt.strftime('%Y%m%d')}{i+1:04d}"
    complaint_nos.append(complaint_no)
    consumer = CONSUMERS[i % len(CONSUMERS)]
    ct = COMPLAINT_TYPES[i % len(COMPLAINT_TYPES)]
    status = random.choice([1, 1, 2, 3])
    processor_ent = ENTERPRISES[i % 2]  # processor
    complaint_rows.append((
        complaint_no,
        random.choice(trace_codes) if trace_codes else '',
        random.choice(prod_batch_nos),
        processor_ent[0], processor_ent[1],
        consumer[0], consumer[2], consumer[3],
        random.choice([0, 0, 1]),
        ct[0], f'{ct[1]}: {ct[2]}',
        f'/uploads/complaints/photos_{complaint_no}.jpg',
        random.choice([1, 2, 3]), status,
        'EMP_001' if status >= 2 else None,
        '客服小李' if status >= 2 else None,
        f'已核实，{ct[1]}，正在处理中' if status == 2 else ('已处理完毕，退款+赔偿' if status == 3 else None),
        f'/uploads/handling/evidence_{complaint_no}.pdf' if status == 3 else None,
        dt,
        (base_dt + timedelta(hours=random.randint(1, 24))).strftime('%Y-%m-%d %H:%M:%S') if status >= 2 else None,
        (base_dt + timedelta(days=random.randint(1, 7))).strftime('%Y-%m-%d %H:%M:%S') if status == 3 else None,
        dt, dt, 'SYSTEM', 'SYSTEM', 0
    ))
bulk_insert('t_complaint_record', ['complaint_no','trace_code','batch_number','enterprise_uuid','enterprise_name','consumer_uuid','complainant_name','phone','is_anonymous','complaint_type','description','photo_urls','priority','status','handler_id','handler_name','handling_conclusion','handling_proof_urls','submit_time','accept_time','close_time','create_time','update_time','create_by','update_by','is_deleted'], complaint_rows)

# Handle logs
handle_rows = []
for cn in complaint_nos:
    dt = random_datetime_s(14)
    base_dt = datetime.strptime(dt, '%Y-%m-%d %H:%M:%S')
    for j in range(random.randint(1, 3)):
        action = random.choice(['SUBMIT', 'ACCEPT', 'INVESTIGATE', 'RESPOND', 'CLOSE'])
        handle_rows.append((cn, action, random.choice([0, 1, 2]), random.choice([1, 2, 3]),
                            f'{action}操作', random.randint(1, 10),
                            random.choice(['客服小李', '客服小王', '管理员']),
                            (base_dt + timedelta(hours=j)).strftime('%Y-%m-%d %H:%M:%S'),
                            (base_dt + timedelta(hours=j)).strftime('%Y-%m-%d %H:%M:%S'),
                            random.choice([1, 2]), 'SYSTEM', 'SYSTEM', 0))
bulk_insert('t_complaint_handle_log', ['complaint_no','action','before_status','after_status','remark','operator_id','operator_name','create_time','update_time','operator_role','create_by','update_by','is_deleted'], handle_rows)

# Scan logs
scan_rows = []
for i in range(30):
    consumer = CONSUMERS[i % len(CONSUMERS)]
    dt = random_datetime_s(30)
    scan_rows.append((
        consumer[0],
        random.choice(trace_codes) if trace_codes else f'TC-TEST-{i}',
        consumer[5][:7] if random.random() > 0.3 else random.choice(['浙江省', '广东省', '山东省']),
        random.choice(['SUCCESS', 'SUCCESS', 'SUCCESS', 'FAIL']),
        None,
        random.choice([1, 1, 1, 0]), random.choice([0, 0, 0, 1]),
        dt, dt, 'SYSTEM', 'SYSTEM', 0
    ))
bulk_insert('t_scan_log', ['consumer_uuid','trace_code','region','query_result','fail_reason','chain_verify_result','risk_level','create_time','update_time','create_by','update_by','is_deleted'], scan_rows)

# ---- 3.17 Audit Log ----
print("[8] Audit Log, Rectify Task, Regulatory Trace")
audit_rows = []
actions = [(1, '创建'), (2, '更新'), (3, '删除'), (4, '查询'), (5, '导出')]
targets = ['t_trace_code', 't_raw_batch', 't_prod_batch', 't_enterprise', 't_qualification',
           't_cc_transport', 't_complaint_record', 't_process_batch', 't_sales_stock']
op_list = [('OP001', '张明远'), ('OP002', '李建国'), ('OP003', 'admin')]
for i in range(25):
    dt = random_datetime_s(30)
    action = actions[i % len(actions)]
    op = op_list[i % len(op_list)]
    audit_rows.append((
        gen_uuid(), op[0], op[1], dt, action[0],
        f'{targets[i % len(targets)]}-{random.randint(1, 100)}',
        f'{action[1]} {targets[i % len(targets)]} 记录',
        '{"old": "..."}' if action[0] in [2, 3] else None,
        '{"new": "..."}' if action[0] in [1, 2] else None,
        random.choice([1, 1, 1, 1, 0]), None,
        gen_hash(f"audit_{i}_{dt}"),
        random.choice([0, 0, 0, 0, 1]),
        '非工作时间操作' if random.random() > 0.9 else None,
        dt, dt, 'SYSTEM', 'SYSTEM', 0
    ))
bulk_insert('t_audit_log', ['log_uuid','operator_id','operator_name','operation_time','action_type','target_id','target_desc','before_data','after_data','operation_result','failure_reason','log_hash','is_abnormal','abnormal_desc','create_time','update_time','create_by','update_by','is_deleted'], audit_rows)

# Rectify tasks
rectify_rows = []
for i in range(3):
    ent = ENTERPRISES[i]
    dt = random_datetime_s(60)
    base_dt = datetime.strptime(dt, '%Y-%m-%d %H:%M:%S')
    deadline = (base_dt + timedelta(days=random.randint(7, 30))).date()
    status = random.choice([0, 1, 2])
    rectify_rows.append((
        gen_uuid(), ent[0], qual_rows[i * 4][0] if i * 4 < len(qual_rows) else qual_rows[0][0],
        random.choice(['资质证书即将过期', '生产环境不达标', '产品抽检不合格']),
        '请在规定期限内完成整改并提交整改报告及相关证明材料',
        deadline, status,
        '已完成整改，附整改报告' if status >= 1 else None,
        '/uploads/rectify/report.pdf' if status >= 1 else None,
        '整改验收通过' if status == 2 else ('整改内容不完整，请补充' if status == 1 else None),
        'OP001' if status >= 1 else None,
        (base_dt + timedelta(days=random.randint(1, 7))).strftime('%Y-%m-%d %H:%M:%S') if status >= 1 else None,
        'OP001', dt, dt, dt, 'SYSTEM', 'SYSTEM', 0
    ))
bulk_insert('t_rectify_task', ['rectify_uuid','enterprise_uuid','qualification_uuid','rectify_reason','rectify_requirement','deadline','rectify_state','submit_content','submit_attachment','review_opinion','reviewer_id','review_time','issuer_id','issue_time','create_time','update_time','create_by','update_by','is_deleted'], rectify_rows)

# Regulatory trace
reg_rows = []
for i in range(10):
    dt = random_datetime_s(30)
    reg_rows.append((
        gen_uuid(), random.randint(1, 3),
        random.choice(prod_batch_nos + trace_codes),
        random.choice([e[1] for e in ENTERPRISES[:2]]),
        random.choice([1, 1, 1, 0]), random.choice([0, 1, 2]),
        '产品质量风险预警' if random.random() > 0.7 else None,
        f'RECALL-{random.randint(1000, 9999)}' if random.random() > 0.9 else None,
        'OP001', '张明远',
        f'追溯查询完成，涉及{random.randint(1, 3)}家企业，产品批次可追溯',
        dt, dt, dt, 'SYSTEM', 'SYSTEM', 0
    ))
bulk_insert('t_regulatory_trace', ['trace_record_uuid','query_type','query_keyword','related_enterprises','hash_verify_result','risk_level','risk_reason','recall_task_id','handler_id','handler_name','handle_conclusion','handle_time','create_time','update_time','create_by','update_by','is_deleted'], reg_rows)

# ============================================================
# Done
# ============================================================
conn.close()

print("\n" + "=" * 60)
print("DONE! All mock data generated successfully.")
print("=" * 60)
