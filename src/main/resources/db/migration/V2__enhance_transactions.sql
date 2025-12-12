-- Add new fields to transactions table for enhanced tracking
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'COMPLETED';
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS inventory_before INTEGER;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS inventory_after INTEGER;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS notes VARCHAR(1000);

-- Update existing transactions to use enum values if type is still varchar
-- This handles migration from string "BUY"/"SELL" to enum values
UPDATE transactions SET status = 'COMPLETED' WHERE status IS NULL;
