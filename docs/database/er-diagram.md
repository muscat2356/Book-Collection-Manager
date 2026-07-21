# ER 図

このドキュメントは、LibraShare の MVP-A における確定済み DB スキーマの ER 図です。認証・ロール管理の正は Keycloak とし、アプリ DB の `users` は貸出対象の利用者（`general_user`）のみを参照情報として保持します。社員・管理社員は `users` に登録せず Keycloak で管理します。

書誌と所蔵の分離・一括貸出の確定方針は [refactor-holdings-and-checkout.md](../design/refactor-holdings-and-checkout.md) を参照してください。参照元は [README.md](../../README.md) の「DB スキーマ（MVP-A）」です。

大中小カテゴリ・`publisher`・「同一書誌は一人一冊まで」はレビュー指摘に基づく確定方針です。マイグレーション例は [sql/seed-publisher-categories.sql](./sql/seed-publisher-categories.sql) を参照してください。

## エンティティ関連図

```mermaid
erDiagram
    CATEGORY_LARGE ||--o{ CATEGORY_MEDIUM : "含む"
    CATEGORY_MEDIUM ||--o{ CATEGORY_SMALL : "含む"
    CATEGORY_SMALL ||--o{ BOOKS : "分類する"
    BOOKS ||--o{ BOOK_COPIES : "所蔵する"
    BOOK_COPIES ||--o{ LOANS : "貸出される"
    USERS ||--o{ LOANS : "借りる"

    CATEGORY_LARGE {
        bigint id PK
        varchar name
        int sort_order
    }
    CATEGORY_MEDIUM {
        bigint id PK
        bigint large_id FK "CATEGORY_LARGE.id"
        varchar name
        int sort_order
    }
    CATEGORY_SMALL {
        bigint id PK
        bigint medium_id FK "CATEGORY_MEDIUM.id"
        varchar name
        int sort_order
    }
    BOOKS {
        bigint id PK
        varchar title
        varchar author
        varchar isbn
        varchar publisher "出版社。NOT NULL"
        bigint category_small_id FK "CATEGORY_SMALL.id。NULL 可"
        boolean deleted "論理削除フラグ。true で削除済み"
        timestamp created_at
    }
    BOOK_COPIES {
        bigint id PK
        bigint book_id FK "BOOKS.id"
        varchar status "AVAILABLE / LOANED"
        boolean deleted "論理削除フラグ。true で削除済み"
    }
    USERS {
        bigint id PK
        varchar keycloak_sub "Keycloak の sub と対応。一意識別子"
        varchar display_name "画面表示名"
        varchar email "Keycloak ログイン識別子にも利用"
        boolean is_active "論理削除フラグ。false で無効化"
        timestamp updated_at
    }
    LOANS {
        bigint id PK
        bigint book_copy_id FK "BOOK_COPIES.id"
        bigint user_id FK "USERS.id"
        timestamp borrowed_at "貸出日時"
        timestamp returned_at "返却実績日時。未返却は NULL"
        varchar status "BORROWED / RETURNED"
    }
```

## リレーション

| 関係 | カーディナリティ | 説明 |
|------|------------------|------|
| `CATEGORY_LARGE` — `CATEGORY_MEDIUM` | 1 対 0..N | 大カテゴリは複数の中カテゴリを持つ |
| `CATEGORY_MEDIUM` — `CATEGORY_SMALL` | 1 対 0..N | 中カテゴリは複数の小カテゴリを持つ |
| `CATEGORY_SMALL` — `BOOKS` | 1 対 0..N | 書誌は小カテゴリのみに紐づく（未設定可） |
| `BOOKS` — `BOOK_COPIES` | 1 対 0..N | 1 書誌は複数の所蔵（物理冊）を持つ |
| `BOOK_COPIES` — `LOANS` | 1 対 0..N | 1 所蔵は複数の貸出履歴を持つ |
| `USERS` — `LOANS` | 1 対 0..N | 1 利用者は複数の貸出履歴を持つ |

`loans` は `book_copy_id` / `user_id` を外部キーに持ち、「誰がいつどの所蔵を借りたか」を記録する履歴テーブルです。

貸出可能冊数は `books` に保持せず、`COUNT(book_copies WHERE book_id=? AND deleted=false AND status='AVAILABLE')` で集計します。所蔵総数（`totalCount`）・`holdings` も `deleted=false` のみを対象とします。`barcode` / `location` は MVP-A では持たない。

### 貸出業務ルール（同一書誌は一人一冊まで）

- **同一書誌（`books.id`）について、同一利用者は同時に 1 冊まで**（`loans.status=BORROWED` が既にあれば追加貸出不可）
- 1 リクエスト内でも、同一書誌の所蔵を 2 冊以上選べない
- 異なる書誌の所蔵をまとめて貸出すことは可
- 違反時は **409**（例: `BOOK_ALREADY_LOANED_BY_USER`）

## 制約（MVP-A の決定事項）

### 外部キー（参照整合性）

- `category_medium.large_id` → `category_large.id`
- `category_small.medium_id` → `category_medium.id`
- `books.category_small_id` → `category_small.id`
- `book_copies.book_id` → `books.id`
- `loans.book_copy_id` → `book_copies.id`
- `loans.user_id` → `users.id`

参照アクションは **物理削除をしにくくし、履歴を壊さない** 方針とし、次を採用します。

- **ON DELETE**: `RESTRICT`（または `NO ACTION`）
- **ON UPDATE**: `RESTRICT`（または `NO ACTION`）

補足:

- `books` は物理削除ではなく **論理削除（`deleted=true`）** を正とします。貸出履歴を壊さないため行は残します。通常の一覧・詳細は `deleted=false` のみを対象とします。書誌削除時、紐づく未削除所蔵に `LOANED` があれば削除不可（業務 409）。
- `book_copies` も物理削除ではなく **論理削除（`deleted=true`）** を正とします。貸出履歴（`loans`）を壊さないため行は残します。通常の集計・holdings・貸出対象は `deleted=false` のみです。所蔵削除時、`status=LOANED` なら削除不可（業務 409）。`AVAILABLE` なら過去の loans があっても論理削除可です。
- `users` は物理削除ではなく **論理削除（`is_active=false`）** を正とします（変更なし）。
- カテゴリは子カテゴリまたは紐づく書誌がある間は物理削除不可（RESTRICT）。カテゴリ論理削除は後続検討可。

### CHECK（整合性）

- `book_copies.status IN ('AVAILABLE', 'LOANED')`
- `loans.status IN ('BORROWED', 'RETURNED')`
- `loans.status='BORROWED'` のとき `returned_at IS NULL`
- `loans.status='RETURNED'` のとき `returned_at IS NOT NULL`
- `returned_at IS NULL OR returned_at >= borrowed_at`

### UNIQUE（推奨）

- `users.keycloak_sub` は UNIQUE
- `users.email` は UNIQUE（Keycloak のログイン識別子にも利用する想定のため）
- `category_large.name` は UNIQUE
- 同一 `large_id` 内の `category_medium.name` は UNIQUE
- 同一 `medium_id` 内の `category_small.name` は UNIQUE

### 索引（決定）

- `book_copies(book_id)`
- `loans(book_copy_id)`
- `loans(user_id)`
- `books(category_small_id)`
- `category_medium(large_id)`
- `category_small(medium_id)`

## テーブル作成 SQL（PostgreSQL・到達スキーマ）

実装時は既存スキーマから Flyway で移行する。以下は移行完了後の到達形です。出版社・カテゴリの追記手順とシード例は [sql/seed-publisher-categories.sql](./sql/seed-publisher-categories.sql) を参照。

```sql
-- 大中小カテゴリ
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

-- books（書誌。stock_count は持たない。publisher は NOT NULL）
CREATE TABLE IF NOT EXISTS books (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  isbn VARCHAR(32),
  publisher VARCHAR(255) NOT NULL,
  category_small_id BIGINT,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_books_category_small_id FOREIGN KEY (category_small_id)
    REFERENCES category_small (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_books_category_small_id ON books (category_small_id);

-- book_copies（所蔵 1 冊）
CREATE TABLE IF NOT EXISTS book_copies (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  book_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_book_copies_book_id FOREIGN KEY (book_id)
    REFERENCES books (id)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT chk_book_copies_status CHECK (status IN ('AVAILABLE', 'LOANED'))
);

CREATE INDEX IF NOT EXISTS idx_book_copies_book_id ON book_copies (book_id);

-- users（貸出対象の利用者 general_user のみ）
CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  keycloak_sub VARCHAR(36) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_users_keycloak_sub UNIQUE (keycloak_sub),
  CONSTRAINT uq_users_email UNIQUE (email)
);

-- loans
CREATE TABLE IF NOT EXISTS loans (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  book_copy_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  borrowed_at TIMESTAMPTZ NOT NULL,
  returned_at TIMESTAMPTZ,
  status VARCHAR(16) NOT NULL,
  CONSTRAINT fk_loans_book_copy_id FOREIGN KEY (book_copy_id)
    REFERENCES book_copies (id)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT fk_loans_user_id FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT chk_loans_status CHECK (status IN ('BORROWED', 'RETURNED')),
  CONSTRAINT chk_loans_returned_at_status CHECK (
    (status = 'BORROWED' AND returned_at IS NULL)
    OR
    (status = 'RETURNED' AND returned_at IS NOT NULL)
  ),
  CONSTRAINT chk_loans_returned_at_after_borrowed_at CHECK (
    returned_at IS NULL OR returned_at >= borrowed_at
  )
);

CREATE INDEX IF NOT EXISTS idx_loans_book_copy_id ON loans (book_copy_id);
CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans (user_id);
```

## 補足

- `users` は貸出対象の利用者（`general_user`）のみを保持する参照テーブルです。認証・パスワード・ロールの正は Keycloak です。
- 付与ロールは常に `general_user` のため `users` に `role` カラムは持ちません。
- `loans.status` は `BORROWED` / `RETURNED`、`book_copies.status` は `AVAILABLE` / `LOANED` です（語彙を混同しない）。所蔵の削除状態は `status` ではなく `deleted` フラグで表します。
- `books.publisher` は必須（`NOT NULL`）。既存シードへの値埋めは [sql/seed-publisher-categories.sql](./sql/seed-publisher-categories.sql) を使う。
- 書誌のカテゴリ紐付けは **小のみ**（`category_small_id`）。大・中への直接 FK は持たない。初期は NULL 可。
- `loans.returned_at` は返却実績日時です。返却予定日 `due_at` は MVP-A のスコープ外です。
- 延滞管理などの拡張は [future-considerations.md](../future/future-considerations.md) を参照してください。
