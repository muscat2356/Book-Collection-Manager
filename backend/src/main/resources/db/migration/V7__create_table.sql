-- ----------------------------------------
-- 1. 大中小カテゴリテーブル + seed
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS category_large (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT uq_category_large_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS category_medium (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  large_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_category_medium_large_id FOREIGN KEY (large_id)
    REFERENCES category_large (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT uq_category_medium_large_name UNIQUE (large_id, name)
);

CREATE TABLE IF NOT EXISTS category_small (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  medium_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_category_small_medium_id FOREIGN KEY (medium_id)
    REFERENCES category_medium (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT uq_category_small_medium_name UNIQUE (medium_id, name)
);

CREATE INDEX IF NOT EXISTS idx_category_medium_large_id ON category_medium (large_id);
CREATE INDEX IF NOT EXISTS idx_category_small_medium_id ON category_small (medium_id);

INSERT INTO category_large (name, sort_order) VALUES
  ('技術', 1),
  ('ビジネス', 2)
ON CONFLICT (name) DO NOTHING;

INSERT INTO category_medium (large_id, name, sort_order)
SELECT l.id, v.name, v.sort_order
FROM category_large l
JOIN (VALUES
  ('技術', 'プログラミング', 1),
  ('技術', 'インフラ', 2),
  ('技術', 'データベース', 3),
  ('ビジネス', 'アジャイル', 1)
) AS v(large_name, name, sort_order)
  ON l.name = v.large_name
ON CONFLICT (large_id, name) DO NOTHING;

INSERT INTO category_small (medium_id, name, sort_order)
SELECT m.id, v.name, v.sort_order
FROM category_medium m
JOIN category_large l ON l.id = m.large_id
JOIN (VALUES
  ('技術', 'プログラミング', 'Java', 1),
  ('技術', 'プログラミング', 'TypeScript', 2),
  ('技術', 'プログラミング', 'Python', 3),
  ('技術', 'インフラ', 'コンテナ', 1),
  ('技術', 'インフラ', 'Linux', 2),
  ('技術', 'データベース', 'SQL', 1),
  ('ビジネス', 'アジャイル', 'チーム開発', 1)
) AS v(large_name, medium_name, name, sort_order)
  ON l.name = v.large_name AND m.name = v.medium_name
ON CONFLICT (medium_id, name) DO NOTHING;

-- ----------------------------------------
-- 2. books に小カテゴリ FK（NULL 可）
-- ----------------------------------------
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS category_small_id BIGINT;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_books_category_small_id'
  ) THEN
    ALTER TABLE books
      ADD CONSTRAINT fk_books_category_small_id FOREIGN KEY (category_small_id)
        REFERENCES category_small (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_books_category_small_id ON books (category_small_id);

-- サンプル: 一部書誌に小カテゴリを紐付け（任意）
UPDATE books b
SET category_small_id = s.id
FROM category_small s
JOIN category_medium m ON m.id = s.medium_id
JOIN category_large l ON l.id = m.large_id
WHERE l.name = '技術' AND m.name = 'プログラミング' AND s.name = 'Java'
  AND b.title IN ('Effective Java', '独習Java', 'スッキリわかるJava入門');

UPDATE books b
SET category_small_id = s.id
FROM category_small s
JOIN category_medium m ON m.id = s.medium_id
JOIN category_large l ON l.id = m.large_id
WHERE l.name = '技術' AND m.name = 'プログラミング' AND s.name = 'Python'
  AND b.title IN ('入門Python3', 'Pythonではじめる機械学習', 'ゼロから作るDeep Learning');

UPDATE books b
SET category_small_id = s.id
FROM category_small s
JOIN category_medium m ON m.id = s.medium_id
JOIN category_large l ON l.id = m.large_id
WHERE l.name = '技術' AND m.name = 'インフラ' AND s.name = 'コンテナ'
  AND b.title IN ('Docker/Kubernetes実践コンテナ開発入門', 'Kubernetes完全ガイド');

UPDATE books b
SET category_small_id = s.id
FROM category_small s
JOIN category_medium m ON m.id = s.medium_id
JOIN category_large l ON l.id = m.large_id
WHERE l.name = '技術' AND m.name = 'データベース' AND s.name = 'SQL'
  AND b.title IN ('SQLアンチパターン', '達人に学ぶDB設計徹底指南書', 'SQL第2版 ゼロからはじめるデータベース操作');

UPDATE books b
SET category_small_id = s.id
FROM category_small s
JOIN category_medium m ON m.id = s.medium_id
JOIN category_large l ON l.id = m.large_id
WHERE l.name = 'ビジネス' AND m.name = 'アジャイル' AND s.name = 'チーム開発'
  AND b.title IN ('アジャイルサムライ', 'カイゼン・ジャーニー', 'チーム開発実践入門');

-- ----------------------------------------
-- 新規 INSERT 例（publisher NOT NULL・category_small_i