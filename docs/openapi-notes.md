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
  "email": "yamada@example.com",
  "temporaryPassword": "change-me"
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

## 蔵書 CRUD API（F-02a / F-02b / F-02c）

管理社員のみが利用できる API。ログイン後ヘッダーの管理メニューまたは蔵書一覧・詳細画面の管理操作から呼び出す。

### POST /api/books

**権限**: `admin_employee`

```json
// Request
{
  "title": "リーダブルコード",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "stockCount": 3
}

// Response 201
{
  "id": 1,
  "title": "リーダブルコード",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "stockCount": 3
}
```

### PUT /api/books/{id}

**権限**: `admin_employee`

```json
// Request
{
  "title": "リーダブルコード 改訂版",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "stockCount": 5
}

// Response 200
{
  "id": 1,
  "title": "リーダブルコード 改訂版",
  "author": "Boswell",
  "isbn": "978-4-87311-565-8",
  "stockCount": 5
}
```

### DELETE /api/books/{id}

**権限**: `admin_employee`

論理削除。物理削除は行わない。アプリ DB の `books.deleted` を立てる（カラムはマージ前ブランチで追加済み）。

```json
// Response 204
{}

// Response 409（例: 貸出中があり削除不可など業務衝突）
{
  "error": "BOOK_HAS_ACTIVE_LOANS",
  "message": "貸出中のため削除できません"
}
```

一覧・詳細の通常取得は `deleted=false`（未削除）のみを対象とする想定。

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
      "stockCount": 3
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
| `content` | Book[] | 当該ページの書籍一覧 |
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

## 貸出 API（F-04 / F-05 参考）

MVP-A で実装。OpenAPI 設計時の参考。

**権限**: `general_employee` / `admin_employee`

### POST /api/loans

```json
// Request
{
  "bookId": 1,
  "userId": 10
}

// Response 201
{
  "id": 42,
  "bookId": 1,
  "userId": 10,
  "borrowedAt": "2026-06-30T10:00:00Z",
  "returnedAt": null,
  "status": "BORROWED"
}
```

一般社員以上が、貸出対象ユーザーと書籍を選択して貸出登録する。API は `users` と `books` の存在、`books.stockCount` を確認し、`loans` 作成と在庫減算を行う。

在庫不足など業務衝突は **409** + `{ error, message }` で返す（例: `INSUFFICIENT_STOCK`）。

### PUT /api/loans/{id}/return

```json
// Response 200
{
  "id": 42,
  "bookId": 1,
  "userId": 10,
  "borrowedAt": "2026-06-30T10:00:00Z",
  "returnedAt": "2026-07-07T15:30:00Z",
  "status": "RETURNED"
}
```

二重返却など状態衝突は **409** + `{ error, message }` で返す（例: `LOAN_ALREADY_RETURNED`）。

### GET /api/loans/active

貸出中の利用者を把握するため、`status=BORROWED` の貸出を利用者情報つきで返す。

```json
// Response 200
[
  {
    "id": 42,
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
- [er-diagram.md](./er-diagram.md) — DB（蔵書 `deleted` / 利用者 `is_active`）
- [front-api-learning.md](./front-api-learning.md) — フロントの `toErrorMessage` 契約
- [future-considerations.md](./future-considerations.md) — バッチ・延滞の将来案
