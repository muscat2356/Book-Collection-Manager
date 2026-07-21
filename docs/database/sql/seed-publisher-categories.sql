-- ============================================
-- 出版社・大中小カテゴリ追記用 SQL（ドキュメント用サンプル）
--
-- 用途:
--   - 既存 DB / V2 シードへの publisher 埋め込み（NOT NULL 化の前準備）
--   - カテゴリマスタの作成と seed
--   - books.category_small_id の追加（任意紐付け）
--
-- 実装時は Flyway の新規 V* に分割して適用する想定。
-- 本ファイルは設計の正（到達形）の参考 SQL です。既存 V2 を直接書き換えない。
-- 参照: docs/database/er-diagram.md
-- ============================================

-- ----------------------------------------
-- 1. 出版社カラム追加 → テストデータ埋め → NOT NULL
-- ----------------------------------------
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS publisher VARCHAR(255);

-- V2 シードの書誌（title で特定）に出版社を付与
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'リーダブルコード';
UPDATE books SET publisher = 'アスキー' WHERE title = '達人プログラマー';
UPDATE books SET publisher = 'Pearson' WHERE title = 'Clean Code';
UPDATE books SET publisher = 'Pearson' WHERE title = 'Clean Architecture';
UPDATE books SET publisher = 'ピアソン・エデュケーション' WHERE title = 'リファクタリング';
UPDATE books SET publisher = '翔泳社' WHERE title = 'エリック・エヴァンスのドメイン駆動設計';
UPDATE books SET publisher = 'オーム社' WHERE title = 'テスト駆動開発';
UPDATE books SET publisher = 'ピアソン・エデュケーション' WHERE title = 'Effective Java';
UPDATE books SET publisher = 'ソフトバンククリエイティブ' WHERE title = 'Java言語で学ぶデザインパターン入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'プログラミング言語Go';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = '入門Python3';
UPDATE books SET publisher = '翔泳社' WHERE title = '独習Java';
UPDATE books SET publisher = 'インプレス' WHERE title = 'スッキリわかるJava入門';
UPDATE books SET publisher = '翔泳社' WHERE title = 'Spring徹底入門';
UPDATE books SET publisher = '秀和システム' WHERE title = 'Spring Boot 3 プログラミング入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'SQLアンチパターン';
UPDATE books SET publisher = '翔泳社' WHERE title = '達人に学ぶDB設計徹底指南書';
UPDATE books SET publisher = '翔泳社' WHERE title = 'SQL第2版 ゼロからはじめるデータベース操作';
UPDATE books SET publisher = '技術評論社' WHERE title = 'Webを支える技術';
UPDATE books SET publisher = 'オーム社' WHERE title = 'マスタリングTCP/IP入門編';
UPDATE books SET publisher = '日経BP' WHERE title = 'ネットワークはなぜつながるのか';
UPDATE books SET publisher = '技術評論社' WHERE title = 'プロになるためのWeb技術入門';
UPDATE books SET publisher = 'ソフトバンククリエイティブ' WHERE title = '体系的に学ぶ安全なWebアプリケーションの作り方';
UPDATE books SET publisher = '翔泳社' WHERE title = 'Docker/Kubernetes実践コンテナ開発入門';
UPDATE books SET publisher = '翔泳社' WHERE title = 'Kubernetes完全ガイド';
UPDATE books SET publisher = '翔泳社' WHERE title = 'インフラエンジニアの教科書';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = '入門 監視';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'SREサイトリライアビリティエンジニアリング';
UPDATE books SET publisher = 'LPI-Japan' WHERE title = 'Linux標準教科書';
UPDATE books SET publisher = 'SBクリエイティブ' WHERE title = '新しいLinuxの教科書';
UPDATE books SET publisher = '技術評論社' WHERE title = 'プログラマのためのGit';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = '実用Git';
UPDATE books SET publisher = 'オーム社' WHERE title = 'アジャイルサムライ';
UPDATE books SET publisher = '翔泳社' WHERE title = 'カイゼン・ジャーニー';
UPDATE books SET publisher = '技術評論社' WHERE title = 'チーム開発実践入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'オブジェクト指向設計実践ガイド';
UPDATE books SET publisher = '秀和システム' WHERE title = 'プリンシプル オブ プログラミング';
UPDATE books SET publisher = '技術評論社' WHERE title = '良いコード/悪いコードで学ぶ設計入門';
UPDATE books SET publisher = '翔泳社' WHERE title = 'ドメイン駆動設計入門';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'マイクロサービスアーキテクチャ';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'データ指向アプリケーションデザイン';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'コンピュータシステムの理論と実装';
UPDATE books SET publisher = '共立出版' WHERE title = 'プログラミングのための線形代数';
UPDATE books SET publisher = '翔泳社' WHERE title = 'アルゴリズム図鑑';
UPDATE books SET publisher = '講談社' WHERE title = '問題解決力を鍛える アルゴリズムとデータ構造';
UPDATE books SET publisher = '翔泳社' WHERE title = 'なっとく!アルゴリズム';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'ゼロから作るDeep Learning';
UPDATE books SET publisher = 'オライリー・ジャパン' WHERE title = 'Pythonではじめる機械学習';
UPDATE books SET publisher = '技術評論社' WHERE title = '達人が教えるWebパフォーマンスチューニング';
UPDATE books SET publisher = 'オーム社' WHERE title = 'ハッカーと画家';

-- 万一未設定が残っていた場合のフォールバック（NOT NULL 化前の安全策）
UPDATE books SET publisher = '不明' WHERE publisher IS NULL;

ALTER TABLE books
  ALTER COLUMN publisher SET NOT NULL;

-- ----------------------------------------
-- 2. 大中小カテゴリテーブル + seed
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
-- 3. books に小カテゴリ FK（NULL 可）
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
-- 新規 INSERT 例（publisher NOT NULL・category_small_id 任意）
-- ----------------------------------------
-- INSERT INTO books (title, author, isbn, publisher, category_small_id)
-- VALUES ('サンプル書誌', '著者名', '9780000000000', 'サンプル出版', NULL);
