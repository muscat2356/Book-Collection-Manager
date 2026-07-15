-- V4: 書誌/所蔵分離（Phase 1）
-- 前提: loans は空（既存貸出がある場合は手移行が必要）

-- 0. ガード（空でないなら失敗させて黙って壊さない）
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM loans LIMIT 1) THEN
    RAISE EXCEPTION
      'V4 aborted: loans に既存行があります。book_copy_id へのデータ移行を先に設計してください';
  END IF;
END $$;

-- 1. 所蔵テーブル
CREATE TABLE IF NOT EXISTS book_copies (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  book_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL,
  CONSTRAINT fk_book_copies_book_id
    FOREIGN KEY (book_id) REFERENCES books (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT chk_book_copies_status
    CHECK (status IN ('AVAILABLE', 'LOANED'))
);

CREATE INDEX IF NOT EXISTS idx_book_copies_book_id ON book_copies (book_id);

-- 2. 旧 stock_count 件ぶん AVAILABLE を投入
INSERT INTO book_copies (book_id, status)
SELECT b.id, 'AVAILABLE'
FROM books b
CROSS JOIN LATERAL generate_series(1, b.stock_count) AS g(n)
WHERE b.stock_count > 0;

-- 3. loans: book_id → book_copy_id
ALTER TABLE loans ADD COLUMN IF NOT EXISTS book_copy_id BIGINT;

ALTER TABLE loans DROP CONSTRAINT IF EXISTS fk_loans_book_id;
DROP INDEX IF EXISTS idx_loans_book_id;
ALTER TABLE loans DROP COLUMN IF EXISTS book_id;

-- 空テーブル前提なのでここで NOT NULL + FK を張る
ALTER TABLE loans ALTER COLUMN book_copy_id SET NOT NULL;

ALTER TABLE loans
  ADD CONSTRAINT fk_loans_book_copy_id
    FOREIGN KEY (book_copy_id) REFERENCES book_copies (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_loans_book_copy_id ON loans (book_copy_id);

-- 4. stock_count 廃止
ALTER TABLE books DROP CONSTRAINT IF EXISTS chk_books_stock_count_non_negative;
ALTER TABLE books DROP COLUMN IF EXISTS stock_count;
