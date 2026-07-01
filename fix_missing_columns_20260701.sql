-- =============================================
-- Fix: add missing columns that Java entities expect
-- but are absent in the database tables
-- Date: 2026-07-01
-- Idempotent: safe to re-run
-- =============================================

DROP PROCEDURE IF EXISTS add_col_if_missing;

DELIMITER $$
CREATE PROCEDURE add_col_if_missing(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def  TEXT
)
BEGIN
    DECLARE col_count INT DEFAULT 0;
    SELECT COUNT(*) INTO col_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'fts'
      AND TABLE_NAME   = tbl_name
      AND COLUMN_NAME  = col_name;

    IF col_count = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 1. t_sales_order: terminal_id, terminal_name
--    Java entity: SalesOrder.terminalId / terminalName
CALL add_col_if_missing('t_sales_order', 'terminal_id',
    'VARCHAR(64) DEFAULT NULL COMMENT "terminal ID" AFTER seller_enterprise_name');
CALL add_col_if_missing('t_sales_order', 'terminal_name',
    'VARCHAR(100) DEFAULT NULL COMMENT "terminal name" AFTER terminal_id');

-- 2. t_sales_terminal: enterprise_id
--    Java entity: SalesTerminal.enterpriseId
CALL add_col_if_missing('t_sales_terminal', 'enterprise_id',
    'VARCHAR(64) DEFAULT NULL COMMENT "enterprise ID" AFTER terminal_name');

DROP PROCEDURE IF EXISTS add_col_if_missing;
