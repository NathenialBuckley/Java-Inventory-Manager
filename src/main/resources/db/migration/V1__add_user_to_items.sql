-- Add user_id column to items table
-- This migration adds user isolation to the inventory system

-- Add the column as nullable first (for existing data)
ALTER TABLE items ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE items ADD CONSTRAINT fk_items_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- For existing items without a user, you'll need to assign them to a user
-- Option 1: Assign to first user (if exists)
-- UPDATE items SET user_id = (SELECT id FROM users LIMIT 1) WHERE user_id IS NULL;

-- Option 2: Delete orphaned items (use with caution!)
-- DELETE FROM items WHERE user_id IS NULL;

-- After handling existing data, make the column NOT NULL
-- ALTER TABLE items ALTER COLUMN user_id SET NOT NULL;
