# レビュー指摘 — リファクタリング設計差分

レビュー指摘（出版社・大中小カテゴリ・同一書誌は一人一冊まで）に基づく **設計変更の差分** です。  
**docs 反映済み**（実装は未着手または一部のみ）。契約の正は [../api/openapi.yaml](../api/openapi.yaml) / [../api/openapi-notes.md](../api/openapi-notes.md) / [../database/er-diagram.md](../database/er-diagram.md) を併読してください。

---

## 1. 変更サマリ

| 指摘 | 決定内容 | docs | 実装 |
|------|----------|------|------|
| 出版社 | `books.publisher VARCHAR(255) NOT NULL` | 反映済 | 未 |
| 大中小カテゴリ | 3 表 + `books.category_small_id`（小のみ、NULL 可） | 反映済 | 未 |
| 同一書誌は一人一冊 | 同一利用者が同一 `books.id` を同時に 2 冊以上借りられない | 反映済 | 未 |
| docs 整理 | カテゴリ別ディレクトリ + 本ファイル | 反映済 | — |

**影響箇所・Back/Front 対応の詳細は [§4](#4-影響箇所と対応機能別)**。実装順は [§5](#5-実装順序推奨)。

---

## 2. DB 差分

### 2.1 `books` テーブル

| 項目 | 変更前 | 変更後 |
|------|--------|--------|
| `publisher` | なし | `VARCHAR(255) NOT NULL` |
| `category_small_id` | なし | `BIGINT NULL` → FK `category_small(id)` RESTRICT |

### 2.2 カテゴリ（新規）

| テーブル | 主な列 | 備考 |
|----------|--------|------|
| `category_large` | id, name, sort_order | name UNIQUE |
| `category_medium` | id, large_id, name, sort_order | 同一 large 内 name UNIQUE |
| `category_small` | id, medium_id, name, sort_order | 同一 medium 内 name UNIQUE |

書誌は **小カテゴリのみ** 紐付け。大・中への直接 FK は持たない。

### 2.3 参考 SQL

- 既存シードへの出版社埋め・NOT NULL 化・カテゴリ seed: [../database/sql/seed-publisher-categories.sql](../database/sql/seed-publisher-categories.sql)
- 到達スキーマ: [../database/er-diagram.md](../database/er-diagram.md)

### 2.4 Flyway（実装時の想定）

| ファイル（新規） | 内容 |
|------------------|------|
| `V6__add_publisher_and_categories.sql` | publisher 追加 → UPDATE → NOT NULL、カテゴリ 3 表、category_small_id |
| または分割 | V6 publisher / V7 categories |

既存 `V2__seed_sample_data.sql` は **書き換えない**（参考 SQL で backfill）。

---

## 3. API 差分

### 3.1 書誌（Books）

**Request（POST/PUT）追加フィールド**

```json
{
  "title": "リーダブルコード",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "publisher": "オライリー・ジャパン",
  "categorySmallId": 100,
  "initialCopyCount": 3
}
```

| フィールド | 必須 | 備考 |
|------------|------|------|
| `publisher` | **必須** | NOT NULL |
| `categorySmallId` | 任意 | 小のみ。null で未分類 |

**Response 追加**

```json
{
  "publisher": "オライリー・ジャパン",
  "category": {
    "smallId": 100,
    "smallName": "Java",
    "mediumId": 10,
    "mediumName": "プログラミング",
    "largeId": 1,
    "largeName": "技術"
  }
}
```

未設定時 `category: null`。

### 3.2 カテゴリ（新規）

| Method | Path | 権限 | 説明 |
|--------|------|------|------|
| GET | `/api/categories/tree` | 社員 | 大→中→小ネスト（登録・編集のカスケード用） |

カテゴリ CRUD は初期は **Flyway seed のみ**。管理画面は後続可。

### 3.3 貸出（Loans）— 同一書誌は一人一冊まで

**ルール**

1. 同一 `userId` について、同一 `books.id` の `BORROWED` が既にあれば追加貸出不可
2. 1 リクエスト内で同一書誌の所蔵を 2 件以上選べない
3. **異なる書誌** の所蔵をまとめて貸出することは可

**Request 例（変更なし・中身の制約が追加）**

```json
{
  "userId": 10,
  "bookCopyIds": [12, 25]
}
```

（bookId=1 の所蔵 12 と bookId=2 の所蔵 25 — OK）

**409 追加**

```json
{
  "error": "BOOK_ALREADY_LOANED_BY_USER",
  "message": "同じ書誌は一人一冊までです"
}
```

**201 例（同一書誌 2 冊は不可になったため、別書誌 2 冊に変更）**

```json
{
  "loans": [
    { "id": 42, "bookCopyId": 12, "bookId": 1, "bookTitle": "リーダブルコード", "userId": 10, "status": "BORROWED" },
    { "id": 43, "bookCopyId": 25, "bookId": 2, "bookTitle": "達人プログラマー", "userId": 10, "status": "BORROWED" }
  ]
}
```

---

## 4. 影響箇所と対応（機能別）

各指摘について **どのファイルが触るか** と **Back / Front で何をするか** をまとめた。実装順の推奨は §5。

### 4.1 出版社（`books.publisher NOT NULL`）

#### 影響箇所

| 層 | パス | 現状 | 変更内容 |
|----|------|------|----------|
| DB | `backend/.../db/migration/V6__*.sql`（新規） | 列なし | `publisher` 追加 → UPDATE → NOT NULL |
| DB | `backend/.../db/migration/V2__seed_sample_data.sql` | 書き換え禁止 | 触らない（参考 SQL で backfill） |
| Domain | `backend/.../domain/Book.java` | title/author/isbn のみ | `publisher` 列追加（`@Column(nullable=false)`） |
| DTO | `backend/.../dto/request/BookRequest.java` | 4 フィールド | `publisher` + `@NotBlank` |
| DTO | `backend/.../dto/response/BookResponse.java` | 所蔵集計のみ | `publisher` 追加 |
| Service | `backend/.../service/BookService.java` | create/update/toResponse | publisher の保存・マッピング |
| Controller | `backend/.../controller/BookController.java` | 変更なし想定 | `@Valid` 経由で 400 バリデーション |
| 型 | `frontend/src/types/Book.ts` | publisher なし | `publisher: string` |
| API | `frontend/src/api/books.ts` | Create/Update 型に publisher なし | リクエスト・レスポンス型に追加 |
| ページ | `frontend/src/pages/BookCreatePage.tsx` | 4 入力項目 | 出版社入力（必須） |
| ページ | `frontend/src/pages/BookEditPage.tsx` | 同上 | 出版社入力（必須） |
| 表示 | `frontend/src/components/BookCard.tsx` | 著者・ISBN のみ | 出版社表示（任意） |
| 表示 | `frontend/src/components/BookDetail.tsx` | 同上 | 出版社表示 |
| 表示 | `frontend/src/pages/BookDetailPage.tsx` | 同上 | 詳細に出版社 |
| Mock | `frontend/src/data/mockBooks.ts` | publisher なし | 開発用 mock に publisher 追加 |
| docs | `docs/api/openapi.yaml` 等 | 反映済 | 実装後に例と一致確認 |

#### Backend 対応

1. Flyway `V6` で [seed-publisher-categories.sql](../database/sql/seed-publisher-categories.sql) の publisher 部分を適用
2. `Book` Entity に `publisher` を追加
3. `BookRequest` に `publisher` + Bean Validation（`UserRequest` と同様の `@NotBlank`）
4. `BookService.createBook` / `updateBook` / `toResponse` で publisher を読み書き
5. 既存データ移行後に NOT NULL 制約が効くことをローカル DB で確認

#### Frontend 対応

1. `Book` 型・`CreateBookRequest` / `UpdateBookRequest` に `publisher` を追加
2. `BookCreatePage` / `BookEditPage` に必須入力欄を追加（HTML `required` + 送信前チェック）
3. `BookCard` / `BookDetail` / 一覧・詳細ページに出版社を表示
4. 400 `VALIDATION_ERROR` は既存の `toErrorMessage` でメッセージ表示（追加ハンドリング最小で可）

---

### 4.2 大中小カテゴリ（3 表 + `category_small_id`）

#### 影響箇所

| 層 | パス | 現状 | 変更内容 |
|----|------|------|----------|
| DB | `backend/.../db/migration/V6__*.sql`（または V7） | テーブルなし | `category_large/medium/small` + `books.category_small_id` |
| Domain | `backend/.../domain/CategoryLarge.java` 等（**新規 3 件**） | なし | JPA Entity |
| Domain | `backend/.../domain/Book.java` | FK なし | `@ManyToOne CategorySmall`（任意） |
| Repository | `backend/.../repository/Category*Repository.java`（**新規**） | なし | ツリー取得用 |
| Repository | `backend/.../repository/BookRepository.java` | 変更最小 | 必要なら `@EntityGraph` で category join |
| DTO | `backend/.../dto/request/BookRequest.java` | なし | `categorySmallId`（任意、`Long`） |
| DTO | `backend/.../dto/response/BookResponse.java` | なし | `category` ネストオブジェクト or `categorySmallId` |
| DTO | `backend/.../dto/response/CategoryTreeResponse.java` 等（**新規**） | なし | 大→中→小ネスト |
| Service | `backend/.../service/BookService.java` | なし | 小カテゴリ FK 設定・レスポンス組み立て |
| Service | `backend/.../service/CategoryService.java`（**新規**） | なし | `GET /api/categories/tree` 用 |
| Controller | `backend/.../controller/CategoryController.java`（**新規**） | なし | `GET /api/categories/tree`（社員） |
| Controller | `backend/.../controller/BookController.java` | 既存 CRUD | Request/Response 拡張のみ |
| Security | `backend/.../controller/*` | `@PreAuthorize` パターンあり | CategoryController に社員ロール付与 |
| 型 | `frontend/src/types/Book.ts` | なし | `categorySmallId?`, `category?` |
| 型 | `frontend/src/types/Category.ts`（**新規**） | なし | ツリー用型 |
| API | `frontend/src/api/books.ts` | publisher 同様 | request/response 拡張 |
| API | `frontend/src/api/categories.ts`（**新規**） | なし | `fetchCategoryTree()` |
| ページ | `BookCreatePage.tsx` / `BookEditPage.tsx` | なし | 大→中→小カスケード `<select>` |
| 表示 | `BookCard.tsx` / `BookDetail.tsx` 等 | なし | 小カテゴリ名（または「大 > 中 > 小」）表示 |
| Mock | `frontend/src/data/mockBooks.ts` | なし | 任意で category 追加 |
| docs | `docs/api/openapi.yaml` | 反映済 | `CategoryTree` スキーマと実装一致確認 |

#### Backend 対応

1. Flyway で 3 カテゴリ表 + seed + `books.category_small_id`（[seed-publisher-categories.sql](../database/sql/seed-publisher-categories.sql) 参照）
2. Entity / Repository / `CategoryService` / `CategoryController` を新規追加
3. `BookRequest.categorySmallId` — 存在しない ID は 400 または 409（プロジェクト慣例に合わせる）
4. `BookResponse` に `category: { smallId, smallName, mediumId, ... }` を組み立て（未設定は `null`）
5. `GET /api/categories/tree` — 登録・編集フォーム用。CRUD は Flyway seed のみ（管理画面は後続）

#### Frontend 対応

1. `Category.ts` + `api/categories.ts` でツリー取得
2. 登録・編集フォームに **連動 select 3 段**（大変更 → 中・小リセット、中変更 → 小リセット）
3. 送信時は `categorySmallId` のみ API に渡す（大・中は UI 補助）
4. 一覧・詳細は `category.smallName` またはパンくず風表示（未分類は「—」等）
5. カテゴリ未取得時はフォームを disabled + エラー表示

---

### 4.3 同一書誌は一人一冊まで（`BOOK_ALREADY_LOANED_BY_USER`）

#### 影響箇所

| 層 | パス | 現状 | 変更内容 |
|----|------|------|----------|
| DB | スキーマ変更なし | — | アプリ層チェック（将来 UNIQUE インデックスは任意） |
| Service | `backend/.../service/LoansService.java` | 在庫・ユーザー状態のみ | **同一 bookId 重複チェック**（リクエスト内 + 既存 BORROWED） |
| Repository | `backend/.../repository/LoanRepository.java` | `findByUserId` のみ | `existsByUserIdAndBookIdAndStatus` 等（**新規クエリ**） |
| Repository | `backend/.../repository/BookCopyRepository.java` | ロック取得あり | bookId 取得は `BookCopy.getBook()` で可（変更不要想定） |
| Exception | `backend/.../exception/exception/BusinessException.java` | 既存 | `BOOK_ALREADY_LOANED_BY_USER` を throw |
| Handler | `backend/.../exception/handler/GlobalExceptionHandler.java` | `BusinessException` → 409 | 変更不要（既存ハンドラで 409） |
| DTO | `backend/.../dto/response/LoansPostListResponse.java` 等 | 変更なし | 409 ボディは `{ error, message }` のみ（`failedBookCopyIds` なし） |
| Context | `frontend/src/loans/LoanContext.tsx` | 同一所蔵 ID のみ dedupe | **同一 bookId の 2 件目を addCopy で拒否** |
| ページ | `frontend/src/pages/CheckoutBookDetailPage.tsx` | 複数所蔵を checkbox 選択可 | 同一書誌は 1 所蔵のみ。2 件目選択時はガード |
| ページ | `frontend/src/pages/CheckoutConfirmPage.tsx` | 409 で `failedBookCopyIds` 除去 | `BOOK_ALREADY_LOANED_BY_USER` は **全選択クリアしない**（`COPY_NOT_AVAILABLE` と分岐） |
| コンポ | `frontend/src/components/CheckoutSelectionSummary.tsx` | 件数表示のみ | 同一書誌警告文（任意） |
| API | `frontend/src/api/loans.ts` | 409 は `COPY_NOT_AVAILABLE` 想定 | `error` コード分岐（`BOOK_ALREADY_LOANED_BY_USER`） |
| docs | `docs/design/refactor-holdings-and-checkout.md` | 反映済 | F-04 フローと整合 |

#### Backend 対応

1. `createLoans` 内、在庫チェック**前**に以下を実施:
   - リクエストの `bookCopyIds` から `bookId` を解決し、**同一 bookId が 2 件以上**なら 409
   - 各 `bookId` について `userId` + `BORROWED` の既存 loan 有無を確認 → あれば 409
2. `LoanRepository` に例: `boolean existsByUser_IdAndBookCopy_Book_IdAndStatus(...)` を追加
3. `throw new BusinessException("BOOK_ALREADY_LOANED_BY_USER", "同じ書誌は一人一冊までです")`
4. トランザクション内・行ロック順序は既存 `findByIdsForUpdate` と整合（デッドロックに注意）
5. 返却後は同一書誌を再貸出可能（`RETURNED` はカウント外）

#### Frontend 対応

1. **`LoanContext.addCopy`**: 既に同じ `bookId` が `selectedBookCopies` にあれば追加しない（または先に remove して 1 件に置換 — UX はチームで決定）
2. **`CheckoutBookDetailPage`**: checkbox で 2 件目を選ぼうとしたとき UI で阻止 + 説明文（「同じ書誌は 1 冊まで」）
3. **`CheckoutConfirmPage`**: 409 かつ `error === 'BOOK_ALREADY_LOANED_BY_USER'` のとき
   - `failedBookCopyIds` が無いので **`COPY_NOT_AVAILABLE` とは分岐**（全件 remove しない）
   - 利用者の既存貸出と衝突した場合は「選び直し」導線を表示
4. **`loans.ts` `toLoanError`**: `data.error` で分岐し、ユーザー向けメッセージを固定化
5. **異なる書誌**の複数選択・一括 POST は現状どおり維持

---

## 5. 実装順序（推奨）

| 順 | 担当 | 内容 | 依存 |
|----|------|------|------|
| 1 | Back | Flyway V6（publisher + カテゴリ + backfill） | — |
| 2 | Back | Entity / Repository / Book API 拡張 | 1 |
| 3 | Back | CategoryController `GET /api/categories/tree` | 1 |
| 4 | Front | 型・API・書誌 CRUD フォーム（publisher + カスケード） | 2, 3 |
| 5 | Back | LoansService 一人一冊チェック | —（2 と並行可） |
| 6 | Front | Checkout UI ガード + 409 分岐 | 5 |
| 7 | 両方 | OpenAPI / ER / 本 doc の「実装」列を更新 | 各完了時 |

---

## 6. Backend 実装チェックリスト（未着手）

### 6.1 Flyway / Entity

- [ ] `V6__add_publisher_and_categories.sql`（[参考 SQL](../database/sql/seed-publisher-categories.sql)）
- [ ] `Book.publisher`（NOT NULL）
- [ ] `Book` ↔ `CategorySmall`（`category_small_id`、任意）
- [ ] `CategoryLarge` / `CategoryMedium` / `CategorySmall` + Repository

### 6.2 Book / Category API

- [ ] `BookRequest` / `BookResponse` — `publisher`, `categorySmallId`, `category`
- [ ] `BookService.createBook` / `updateBook` / `toResponse`
- [ ] `CategoryService` + `CategoryController` — `GET /api/categories/tree`

### 6.3 Loans API

- [ ] `LoanRepository` — 同一 user + book + BORROWED 存在確認
- [ ] `LoansService.createLoans` — リクエスト内 bookId 重複 + 既存 BORROWED チェック
- [ ] `BusinessException("BOOK_ALREADY_LOANED_BY_USER", ...)` → 409

---

## 7. Frontend 実装チェックリスト（未着手）

### 7.1 書誌（F-02 / F-02a/b）

- [ ] `types/Book.ts` — `publisher`, `category`, `categorySmallId`
- [ ] `types/Category.ts` + `api/categories.ts`（新規）
- [ ] `api/books.ts` — Create/Update/Response 型
- [ ] `BookCreatePage` / `BookEditPage` — 出版社 + カスケード select
- [ ] `BookCard` / `BookDetail` / 詳細ページ — 表示追加

### 7.2 Checkout（F-04）

- [ ] `LoanContext.addCopy` — 同一 `bookId` ガード
- [ ] `CheckoutBookDetailPage` — 1 書誌 1 所蔵 UI
- [ ] `CheckoutConfirmPage` + `api/loans.ts` — `BOOK_ALREADY_LOANED_BY_USER` 409 分岐

---

## 8. docs ディレクトリ再構成（本 PR）

| 旧パス | 新パス |
|--------|--------|
| `docs/refactor-holdings-and-checkout.md` | `docs/design/refactor-holdings-and-checkout.md` |
| `docs/screen-transition.md` | `docs/design/screen-transition.md` |
| `docs/sequence-diagram.md` | `docs/design/sequence-diagram.md` |
| `docs/frontend-gap-analysis.md` | `docs/design/frontend-gap-analysis.md` |
| `docs/refact-plane.md` | `docs/design/refact-plane.md` |
| `docs/er-diagram.md` | `docs/database/er-diagram.md` |
| `docs/sql/` | `docs/database/sql/` |
| `docs/openapi.yaml` | `docs/api/openapi.yaml` |
| `docs/openapi-notes.md` | `docs/api/openapi-notes.md` |
| `docs/book-copies-soft-delete-changes.md` | `docs/refactoring/book-copies-soft-delete-changes.md` |
| `docs/future-considerations.md` | `docs/future/future-considerations.md` |
| `docs/front-api-learning.md` 等 | `docs/learning/` |
| — | `docs/README.md`（索引） |
| — | `docs/refactoring/review-refactoring-diff.md`（本ファイル） |

---

## 9. 関連ドキュメント

- [../design/refactor-holdings-and-checkout.md](../design/refactor-holdings-and-checkout.md) — 所蔵分離・checkout
- [../database/er-diagram.md](../database/er-diagram.md) — ER・制約
- [../api/openapi.yaml](../api/openapi.yaml) — OpenAPI
- [../api/openapi-notes.md](../api/openapi-notes.md) — API 補足
- [book-copies-soft-delete-changes.md](./book-copies-soft-delete-changes.md) — 所蔵論理削除（別件）
- [../future/future-considerations.md](../future/future-considerations.md) — 将来検討
