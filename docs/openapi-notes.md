# OpenAPI 補足メモ

設計フェーズ（Phase 3 Day 7）で OpenAPI 仕様書に反映する草案。

---

## ロール方針

LibraShare は社内バックオフィス向けの蔵書管理として扱う。全ユーザーの認証・ロール管理は Keycloak を正とし、アプリ DB の `users` には貸出対象の利用者（`general_user`）のみを参照情報として保持する。社員・管理社員は `users` に登録せず、Keycloak 管理コンソールで管理する。

| ロール | 説明 | 管理場所 |
|--------|------|----------|
| `general_user` | 貸出対象の利用者。バックオフィス画面の操作は対象外 | アプリ（`/api/users`）+ Keycloak |
| `general_employee` | 一般社員。蔵書参照、利用者管理、貸出/返却、貸出中一覧を利用できる | Keycloak 管理コンソール |
| `admin_employee` | 管理社員。一般社員の機能に加えて、蔵書追加・更新・削除を利用できる | Keycloak 管理コンソール |

---

## エラーレスポンス共通仕様

アプリが返す業務・バリデーションエラーのボディは次で統一する。実装は `GlobalExceptionHandler`（`@RestControllerAdvice`）を想定する。

```json
{
  "error": "USER_HAS_ACTIVE_LOANS",
  "message": "貸出中の書籍があるため削除できません"
}
```

| フィールド | 型 | 役割 |
|------------|-----|------|
| `error` | string | 機械可読コード（例: `VALIDATION_ERROR`） |
| `message` | string | 人間向け文言（画面表示用） |

MVP-A ではフィールド単位のエラー配列は返さない。入力バリデーション失敗時は先頭 1 件の `message` のみとする。

### HTTP ステータス方針

| 区分 | ステータス | ボディ | 備考 |
|------|------------|--------|------|
| 入力バリデーション（`@Valid` / Bean Validation） | **400** | `{ error, message }` | 例: `error: "VALIDATION_ERROR"` |
| 業務衝突（在庫不足・貸出中で削除不可・二重返却 等） | **409** | `{ error, message }` | Conflict |
| 未認証 | **401** | **固定**（Spring Security 既定） | `{ error, message }` に揃えない |
| 権限不足 | **403** | **固定**（Spring Security 既定） | フロントは `"権限がありません"` を表示 |
| リソースなし | **404** | 空ボディ可 | 蔵書詳細など。フロントは `null` 扱いで「見つかりません」UI |

### バックエンド実装の分担

| 層 | 内容 | 失敗時 |
|----|------|--------|
| Request DTO + `@Valid` | 必須・形式（`@NotBlank` / `@Min` 等） | `MethodArgumentNotValidException` → Handler → **400** |
| Service | 業務ルール（在庫・状態衝突） | 業務例外 → Handler → **409** |

### フロントの扱い

- 画面表示は `response.data.message` を使う（既存 `toErrorMessage`）
- `error` コードは将来の分岐用。MVP-A では未使用でも可
- **403** は API ボディを読まず固定文言 `"権限がありません"`
- **401** もボディ非統一のまま（Security 既定）

### 代表的な `error` コード例

| error | HTTP | 場面 |
|-------|------|------|
| `VALIDATION_ERROR` | 400 | 入力バリデーション失敗 |
| `USER_HAS_ACTIVE_LOANS` | 409 | 利用者削除時に貸出中あり（変更なし） |
| `INSUFFICIENT_STOCK` | 409 | 貸出時に在庫不足 |
| `LOAN_ALREADY_RETURNED` | 409 | 返却済み貸出の再返却 等 |

---

## 利用者管理 API（F-06）

一般社員以上が利用できる API。**対象は貸出対象の利用者（`general_user`）のみ**で、社員・管理社員は扱わない（Keycloak 管理コンソールで管理）。これにより一般社員が社員・管理社員の情報を編集できてしまう権限昇格を構造的に防ぐ。

Keycloak Admin API と連携して Keycloak 側に利用者を作成/更新し（付与ロールは常に `general_user`）、アプリ DB の `users` に `keycloakSub` と表示情報を保存する。削除は物理削除ではなく **論理削除 + Keycloak 無効化** とする（貸出履歴を壊さないため）。

アプリ DB は独自の `username` を保持せず、表示は `displayName`、利用者の一意識別は `keycloakSub` で行う。付与ロールは常に `general_user` のため `users` に `role` カラムは持たず、API のボディでもロールは受け取らない。Keycloak 側のログイン識別子（`username`）には `email` を用いる想定。

### 利用者登録時のパスワード（MVP-A）

- リクエストボディは **氏名（`displayName`）とメール（`email`）のみ**。フロントからパスワードを送らない
- バックエンドが初回パスワードを生成し、Keycloak Admin API でユーザー作成時に設定する
- Keycloak のパスワードは **temporary フラグをオン** にする（初回ログイン時に変更を要求する想定）
- 生成したパスワードは API レスポンス・画面には載せない
- 利用者本人への初回パスワード通知は **MVP-A 対象外**。`general_user` のログイン実装時に検討する（[future-considerations.md](./future-considerations.md)）

### GET /api/users

**権限**: `general_employee` / `admin_employee`

```json
// Response 200 （デフォルトは is_active=true のみ。?includeInactive=true で論理削除済みも含む）
[
  {
    "id": 10,
    "keycloakSub": "8d5f5c6e-1111-2222-3333-123456789abc",
    "displayName": "山田 太郎",
    "email": "yamada@example.com",
    "isActive": true
  }
]
```

### GET /api/users/{id}

**権限**: `general_employee` / `admin_employee`

```json
// Response 200
{
  "id": 10,
  "keycloakSub": "8d5f5c6e-1111-2222-3333-123456789abc",
  "displayName": "山田 太郎",
  "email": "yamada@example.com",
  "isActive": true
}
```

### POST /api/users

**権限**: `general_employee` / `admin_employee`

```json
// Request
{
  "displayName": "山田 太郎",
  "email": "yamada@example.com"
}

// Response 201
{
  "id": 10,
  "keycloakSub": "8d5f5c6e-1111-2222-3333-123456789abc",
  "displayName": "山田 太郎",
  "email": "yamada@example.com",
  "isActive": true
}
```

処理内容:

1. `email` の重複などを検証する（衝突時は 409 等）
2. 初回パスワードをサーバー側で生成する
3. Keycloak に利用者を作成し、`general_user` ロールを付与する。パスワードは temporary フラグをオンで設定する
4. アプリ DB の `users` に `keycloakSub`・表示情報を保存する
5. 201 で利用者情報を返す（パスワードは含めない）

### PUT /api/users/{id}

**権限**: `general_employee` / `admin_employee`

```json
// Request
{
  "displayName": "山田 太郎",
  "email": "yamada.taro@example.com"
}

// Response 200
{
  "id": 10,
  "keycloakSub": "8d5f5c6e-1111-2222-3333-123456789abc",
  "displayName": "山田 太郎",
  "email": "yamada.taro@example.com",
  "isActive": true
}
```

### DELETE /api/users/{id}

**権限**: `general_employee` / `admin_employee`

論理削除。物理削除は行わない。

```json
// Response 204
{}

// Response 409（貸出中の loans があり削除不可）
{
  "error": "USER_HAS_ACTIVE_LOANS",
  "message": "貸出中の書籍があるため削除できません"
}
```

処理内容:

1. 対象利用者に `status=BORROWED` の `loans` があれば `409 Conflict` を返し、削除しない
2. 貸出中がなければ Keycloak 側の利用者を無効化（`enabled=false`）する
3. アプリ DB の `users` を論理削除する（`is_active=false`）
4. `loans` の過去履歴はそのまま残す

無効化済み利用者は通常の利用者一覧には表示しないが、貸出履歴や `?includeInactive=true` では参照できる。

---

## 書誌 API（F-02 / F-02a / F-02b / F-02c）

`books` は書誌（種類）、`book_copies` は所蔵（1 冊）です。在庫数は `stockCount` ではなく所蔵の集計（`totalCount` / `availableCount`）で表します。詳細は [refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md) を参照。

一覧・詳細の通常取得は `deleted=false` の書誌のみを対象とする。

### GET /api/books

**権限**: `general_employee` / `admin_employee`

```json
// Response 200
[
  {
    "id": 1,
    "title": "リーダブルコード",
    "author": "Boswell",
    "isbn": "978-4-87311-565-8",
    "totalCount": 3,
    "availableCount": 2
  }
]
```

### GET /api/books/{id}

**権限**: `general_employee` / `admin_employee`

```json
// Response 200
{
  "id": 1,
  "title": "リーダブルコード",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "totalCount": 3,
  "availableCount": 2,
  "holdings": [
    { "id": 12, "status": "AVAILABLE" },
    { "id": 14, "status": "LOANED" },
    { "id": 15, "status": "AVAILABLE" }
  ]
}
```

### POST /api/books

**権限**: `admin_employee`

`initialCopyCount` 件の `AVAILABLE` 所蔵を同時に作成する。

```json
// Request
{
  "title": "リーダブルコード",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "initialCopyCount": 3
}

// Response 201
{
  "id": 1,
  "title": "リーダブルコード",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "totalCount": 3,
  "availableCount": 3,
  "holdings": [
    { "id": 12, "status": "AVAILABLE" },
    { "id": 13, "status": "AVAILABLE" },
    { "id": 14, "status": "AVAILABLE" }
  ]
}
```

### PUT /api/books/{id}

**権限**: `admin_employee`

書誌情報のみ更新する。冊数は受け付けない（所蔵の追加は次節）。

```json
// Request
{
  "title": "リーダブルコード 改訂版",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8"
}

// Response 200
{
  "id": 1,
  "title": "リーダブルコード 改訂版",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "totalCount": 3,
  "availableCount": 2,
  "holdings": [
    { "id": 12, "status": "AVAILABLE" },
    { "id": 14, "status": "LOANED" },
    { "id": 15, "status": "AVAILABLE" }
  ]
}
```

### POST /api/books/{id}/copies

**権限**: `admin_employee`

所蔵を 1 冊追加する。

```json
// Request
{}

// Response 201
{
  "id": 16,
  "status": "AVAILABLE"
}
```

### DELETE /api/books/{id}

**権限**: `admin_employee`

論理削除。物理削除は行わない。アプリ DB の `books.deleted` を `true` にする。

```json
// Response 204
{}

// Response 404（対象書誌が存在しない）
（ボディなし）

// Response 409（貸出中の所蔵があり削除不可）
{
  "error": "BOOK_HAS_LOANED_COPIES",
  "message": "貸出中の所蔵があるため削除できません"
}
```

処理内容:

1. 対象書誌がなければ `404`
2. 紐づく所蔵に `status=LOANED` があれば `409`
3. なければ `deleted=true` に更新し `204`

---

## F-11 書籍一覧ページング

### エンドポイント

```
GET /api/books?page={page}&size={size}
```

| パラメータ | 型 | 必須 | デフォルト | 説明 |
|------------|-----|------|------------|------|
| `page` | integer | 否 | `0` | ページ番号（0 始まり） |
| `size` | integer | 否 | `20` | 1 ページあたりの件数 |
| `q` | string | 否 | — | 検索キーワード（F-03 と併用、MVP-B） |

### レスポンス（Spring Page 互換イメージ）

```json
{
  "content": [
    {
      "id": 1,
      "title": "リーダブルコード",
      "author": "Boswell",
      "isbn": "978-4-87311-565-8",
      "totalCount": 3,
      "availableCount": 2
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

| フィールド | 型 | 説明 |
|------------|-----|------|
| `content` | Book[] | 当該ページの書誌一覧（`totalCount` / `availableCount`） |
| `page` | integer | 現在のページ番号 |
| `size` | integer | ページサイズ |
| `totalElements` | long | 全件数 |
| `totalPages` | integer | 総ページ数 |

### MVP-A との関係

- **MVP-A**: `page` / `size` 未指定時は全件返却（既存クライアント互換）
- **MVP-B**: ページングパラメータ対応 + フロントに前へ/次へ UI
- **権限**: `general_employee` / `admin_employee`

### フロント実装イメージ（Member B）

```ts
// api/books.ts
export async function fetchBooks(page = 0, size = 20): Promise<Page<Book>> {
  const { data } = await apiClient.get<Page<Book>>('/api/books', {
    params: { page, size },
  });
  return data;
}
```

```tsx
// BookListPage.tsx
const [page, setPage] = useState(0);
// 「前へ」「次へ」ボタンで setPage を更新
```

### 検索との併用（F-03 + F-11）

```
GET /api/books?q=react&page=0&size=20
```

検索結果に対してページングを適用する。F-03 と F-11 は同時実装すると効率的。

---

## 貸出 API（F-04 / F-05）

MVP-A で実装。貸出入口は SPA の `/loans/checkout`。詳細は [refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md)。

**権限**: `general_employee` / `admin_employee`

### POST /api/loans

複数所蔵を 1 リクエストで一括貸出する（全件成功 / 全件ロールバック）。

```json
// Request
{
  "userId": 10,
  "bookCopyIds": [12, 15]
}

// Response 201
{
  "loans": [
    {
      "id": 42,
      "bookCopyId": 12,
      "bookId": 1,
      "bookTitle": "リーダブルコード",
      "userId": 10,
      "borrowedAt": "2026-06-30T10:00:00Z",
      "returnedAt": null,
      "status": "BORROWED"
    },
    {
      "id": 43,
      "bookCopyId": 15,
      "bookId": 1,
      "bookTitle": "リーダブルコード",
      "userId": 10,
      "borrowedAt": "2026-06-30T10:00:00Z",
      "returnedAt": null,
      "status": "BORROWED"
    }
  ]
}

// Response 409
{
  "error": "COPY_NOT_AVAILABLE",
  "message": "貸出できない所蔵が含まれています",
  "failedBookCopyIds": [15]
}
```

処理内容:

1. 利用者の存在・有効を確認する
2. 各 `bookCopyId` を行ロックし `AVAILABLE` であることを確認する
3. 1 件でも不可なら 409 で全体ロールバック
4. 各 copy を `LOANED`、`loans` を `BORROWED` で作成する

サーバによる自動割当は行わない。クライアントが指定した `bookCopyIds` のみを検証する。

### PUT /api/loans/{id}/return

```json
// Response 200
{
  "id": 42,
  "bookCopyId": 12,
  "bookId": 1,
  "bookTitle": "リーダブルコード",
  "userId": 10,
  "borrowedAt": "2026-06-30T10:00:00Z",
  "returnedAt": "2026-07-07T15:30:00Z",
  "status": "RETURNED"
}
```

当該所蔵を `AVAILABLE` に戻す。二重返却など状態衝突は **409**（例: `LOAN_ALREADY_RETURNED`）。

### GET /api/loans/active

貸出中（`status=BORROWED`）を利用者・書誌情報つきで返す。

```json
// Response 200
[
  {
    "id": 42,
    "bookCopyId": 12,
    "book": {
      "id": 1,
      "title": "リーダブルコード",
      "author": "Boswell"
    },
    "user": {
      "id": 10,
      "displayName": "山田 太郎"
    },
    "borrowedAt": "2026-06-30T10:00:00Z",
    "returnedAt": null,
    "status": "BORROWED"
  }
]
```

`GET /api/loans/me` のような利用者自身のマイ貸出は、バックオフィス用途の MVP-A では対象外とする。

---

## 関連ドキュメント

- [README](../README.md) — 3 ロール、利用者管理、F-02a/b/c、F-04、F-05、F-06、F-11 の機能定義
- [refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md) — 書誌/所蔵・一括貸出の確定設計
- [er-diagram.md](./er-diagram.md) — DB（書誌 `deleted` / 所蔵 / 利用者 `is_active`）
- [front-api-learning.md](./front-api-learning.md) — フロントの `toErrorMessage` 契約
- [future-considerations.md](./future-considerations.md) — バッチ・延滞などの将来案
