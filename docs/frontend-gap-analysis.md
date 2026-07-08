# フロントエンド差分リスト（my-react-app → 確定設計）

Member B 向け。現行プロトタイプ（[my-react-app](/home/spiritualmasa/workspace/react-handson/my-react-app)）と、打ち合わせで確定した設計（[screen-transition.md](./screen-transition.md)、[openapi-notes.md](./openapi-notes.md)、[README.md](../README.md)）の差分を一覧化したものです。実装は各自の手で行う前提のため、**やることのリストのみ**を記載しています。

---

## 1. ルーティング（App.tsx）

| 状態 | パス | 現状 | 確定設計 | 対応 |
|------|------|------|----------|------|
| 済 | `/` | `/books` へリダイレクト | 同左 | なし |
| 済 | `/books` | 実装済み | 蔵書一覧 | なし |
| 済 | `/books/:id` | 実装済み | 蔵書詳細 | なし |
| 要変更 | `/loans/me` | 仮ページ（MyLoransPage） | **廃止** | ルート削除 |
| 未 | `/loans/active` | なし | 貸出中一覧（F-05） | 新規 Route 追加 |
| 未 | `/books/new` | なし | 蔵書追加（admin のみ） | 新規 Route 追加 |
| 未 | `/books/:id/edit` | なし | 蔵書編集（admin のみ） | 新規 Route 追加 |
| 未 | `/users` | なし | 利用者一覧（F-06） | 新規 Route 追加 |
| 未 | `/users/new` | なし | 利用者登録 | 新規 Route 追加 |
| 未 | `/users/:id/edit` | なし | 利用者編集・論理削除 | 新規 Route 追加 |
| 要強化 | `/login` | 仮ページ（プレースホルダのみ） | Keycloak ログイン + 社員ロール判定 | 実装 |
| 未 | 保護ルート | なし（全 Route 公開） | 未ログイン → `/login`、社員ロール必須 | ProtectedRoute 導入 |
| 未 | 権限エラー画面 | なし | `general_user` ログイン時の拒否 UX | 新規 Route または画面 |
| 未 | 404 | なし | 存在しないパス | 任意（推奨） |

---

## 2. ページ（pages/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `BookListPage.tsx` | 実装済み | mock データ取得のみ。本 API（`GET /api/books`）接続、管理社員向け「蔵書追加」導線、MVP-B では検索・ページング UI |
| `BookDetailPage.tsx` | 実装済み | 貸出/返却は BookCard 内のローカル state。API 連携・利用者選択 UI・管理社員の編集/削除導線が未 |
| `LoginPage.tsx` | 仮 | Keycloak JS Adapter によるログイン/ログアウト、コールバック処理 |
| `MyLoransPage.tsx` | 仮・**廃止対象** | `/loans/active` 用の新ページに置き換え（ファイル名も変更推奨） |
| `BookNewPage.tsx` | **未作成** | `POST /api/books` フォーム（admin のみ） |
| `BookEditPage.tsx` | **未作成** | `PUT /api/books/{id}` フォーム + 削除（admin のみ） |
| `UserListPage.tsx` | **未作成** | `GET /api/users` 一覧 |
| `UserNewPage.tsx` | **未作成** | `POST /api/users`（displayName, email, temporaryPassword） |
| `UserEditPage.tsx` | **未作成** | `PUT /api/users/{id}` + `DELETE`（409 エラー表示） |
| `ActiveLoansPage.tsx` | **未作成** | `GET /api/loans/active` + 返却ボタン（`PUT /api/loans/{id}/return`） |
| `AccessDeniedPage.tsx` | **未作成** | 社員ロールなし（`general_user`）の拒否画面 |

---

## 3. コンポーネント（components/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `Layout.tsx` | 実装済み | ナビが固定（全員同じ）。JWT ロールで出し分け、ログアウト、管理社員のみ「蔵書追加」 |
| `BookCard.tsx` | 実装済み | 借りる/返却が `useState` のみ。API 呼び出し・利用者選択・ロール別操作表示に変更 |
| `StockBadge.tsx` | 実装済み | 大きな差分なし（在庫表示は維持） |
| `MemberCard.tsx` | 学習用 | 確定設計に不在。**削除または学習用として残すか判断** |
| `ProtectedRoute.tsx` | **未作成** | 認証・ロールチェック |
| 削除確認ダイアログ | **未作成** | 蔵書削除・利用者論理削除用 |
| 利用者選択 UI | **未作成** | 貸出時に `userId` を選ぶコンポーネント（select 等） |

### Layout ナビの差分

| 項目 | 現状（Layout.tsx） | 確定設計 |
|------|---------------------|----------|
| 書籍一覧 | `/books` | 同左 |
| 貸出中 | `/loans/me` | **`/loans/active`**（ラベル: 貸出中一覧） |
| 利用者管理 | なし | **`/users`** |
| 蔵書追加 | なし | **`/books/new`**（admin のみ） |
| ログイン | `/login`（常時表示） | 未ログイン時のみ / ログイン後はログアウト |

---

## 4. API 層（api/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `client.ts` | 雛形あり | `localStorage.getItem('token')` → Keycloak の `access_token` 注入に変更 |
| `books.ts` | mock 返却 | `GET/POST/PUT/DELETE /api/books` を apiClient 経由で実装 |
| `users.ts` | **未作成** | `GET/POST/PUT/DELETE /api/users` |
| `loans.ts` | **未作成** | `POST /api/loans`, `PUT /api/loans/{id}/return`, `GET /api/loans/active` |

### books.ts の具体的差分

- `fetchBooks()`: mockBooks + 1秒 delay → `GET /api/books`
- `fetchBookById()`: mockBooks.find → `GET /api/books/{id}`
- 未実装: `createBook`, `updateBook`, `deleteBook`
- MVP-B: `fetchBooks(page, size, q?)` でページング・検索

---

## 5. 認証（auth/）

| 項目 | 現状 | 確定設計 |
|------|------|----------|
| `auth/` フォルダ | なし | `keycloak.ts`, `AuthContext.tsx` 等 |
| Keycloak JS | 未導入（package.json になし） | `keycloak-js` 追加 |
| ログイン | プレースホルダ | OIDC Authorization Code + PKCE |
| トークン | localStorage `'token'` 仮 | Keycloak `access_token` |
| ロール判定 | なし | JWT の `general_employee` / `admin_employee` |
| general_user | 未考慮 | ログイン可だが SPA 利用不可 → 拒否画面 |

---

## 6. 型定義（types/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `book.ts` | あり | `id` が `string` → API は `number`（bigint）。要整合 |
| `user.ts` | **未作成** | id, keycloakSub, displayName, email, isActive |
| `loan.ts` | **未作成** | id, bookId, userId, borrowedAt, returnedAt, status |
| `loan-active.ts` 等 | **未作成** | 貸出中一覧用ネスト型（book + user） |
| `page.ts` | **未作成** | MVP-B: Page\<T\>（content, page, size, totalElements, totalPages） |
| `member.ts` | 学習用 | 確定設計に不在 |

---

## 7. データ・環境

| 項目 | 現状 | 確定設計 |
|------|------|----------|
| `data/mockBooks.ts` | 使用中 | API 接続後は dev フォールバック用に残すか削除するか判断 |
| `data/mockMembers.ts` | 学習用 | 確定設計に不在 |
| `.env` / `VITE_*` | `VITE_API_BASE_URL` のみ（client.ts） | Keycloak URL, realm, clientId 等を追加 |
| 配置先 | `my-react-app/` | 本番は `LibraShare/frontend/`（現時点フォルダ未作成） |

---

## 8. ドキュメント（my-react-app/docs/）

| ファイル | 状態 | 差分 |
|----------|------|------|
| `screen-flow.md` | 旧設計 | `/loans/me` 記載 → [screen-transition.md](./screen-transition.md) に合わせて更新 |
| `presentation.md` | 旧設計 | 利用者管理・3 ロール・`/loans/active` を反映 |
| `component-structure.md` | 旧設計 | 未作成ファイル（auth/, users.ts, loans.ts 等）を追記 |

---

## 9. 依存パッケージ（package.json）

| パッケージ | 現状 | 必要 |
|------------|------|------|
| react, react-router-dom, axios | あり | 維持 |
| keycloak-js | **なし** | F-01 で追加 |
| @types/keycloak-js 等 | **なし** | 必要に応じて |

---

## 10. 機能 ID 別チェックリスト（Member B 担当）

| ID | 機能 | 現状 | 残タスク |
|----|------|------|----------|
| F-01 | Keycloak + Protected Route | 未 | 全 Phase 4-A |
| F-02 | 蔵書一覧・詳細 UI | 部分 | API 接続 |
| F-02a | 蔵書追加 UI | 未 | `/books/new` |
| F-02b | 蔵書更新 UI | 未 | `/books/:id/edit` |
| F-02c | 蔵書削除 UI | 未 | 確認ダイアログ + API |
| F-04 | 貸出/返却 UI | UI のみ（local state） | 利用者選択 + loans API |
| F-05 | 貸出中一覧 UI | 未（/loans/me 仮） | `/loans/active` |
| F-06 | 利用者管理 UI | 未 | 3 画面 + CRUD |
| F-07 | フロント dev 手順 | 未 | README 追記 |
| F-03 | 検索 UI | 未 | MVP-B |
| F-11 | ページング UI | 未 | MVP-B |
| F-10 | UX polish | 最小限 | MVP-B |

---

## 11. 本番移行時（my-react-app → LibraShare/frontend/）

| # | 作業 |
|---|------|
| 1 | `LibraShare/frontend/` を Vite + React + TS で新規作成（または my-react-app をコピー） |
| 2 | docker-compose.yml の frontend サービスとポート（3000 vs 5173）を整合 |
| 3 | 環境変数を `.env_example` / docker の `REACT_APP_*` or `VITE_*` に統一 |
| 4 | `.cursor/rules/` と `AGENTS.md` を frontend から参照できるよう LibraShare ルートに配置（済） |
| 5 | 学習用ファイル（MemberCard, mockMembers, member.ts）を本番から除外 |

---

## 12. 実装順序の推奨（参考）

設計ドキュメント・README の依存関係に沿った順序です。

1. `LibraShare/frontend/` 作成・プロトタイプ移行
2. ルート整理（`/loans/active` 追加、`/loans/me` 削除、未実装 Route の枠）
3. 型定義（User, Loan, Page）
4. Keycloak JS + ProtectedRoute + Layout ロール別ナビ（EX-3）
5. 蔵書 API 接続（F-02）
6. 利用者管理（F-06）
7. 蔵書 CRUD（F-02a/b/c）
8. 貸出/返却（F-04）→ 貸出中一覧（F-05）
9. MVP-B（F-03, F-11, F-10）

---

## 関連ドキュメント

- [README.md](../README.md)
- [screen-transition.md](./screen-transition.md)
- [openapi-notes.md](./openapi-notes.md)
- [sequence-diagram.md](./sequence-diagram.md)
