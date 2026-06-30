# OpenAPI 補足メモ

設計フェーズ（Phase 3 Day 7）で OpenAPI 仕様書に反映する草案。

---

## 社員ロール方針

LibraShare は社内バックオフィス向けの蔵書管理として扱う。一般公開ユーザーは想定しない。

| ロール | 説明 |
|--------|------|
| `general_employee` | 一般社員。蔵書参照、貸出/返却、マイ貸出を利用できる |
| `admin_employee` | 管理社員。一般社員の機能に加えて、蔵書追加・更新・削除を利用できる |

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

```json
// Response 204
{}
```

削除時は、貸出中の本を削除できないなどの業務ルールを設計フェーズで決める。

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

## 貸出 API（F-04 参考）

MVP-A で実装。OpenAPI 設計時の参考。

**権限**: `general_employee` / `admin_employee`

### POST /api/loans

```json
// Request
{ "bookId": 1 }

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

---

## 関連ドキュメント

- [README](../README.md) — 社員ロール、F-02a/b/c、F-04、F-11 の機能定義
- [future-considerations.md](./future-considerations.md) — バッチ・延滞の将来案
