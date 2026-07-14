# フロントエンド差分リスト（確定設計との差分）

Member B 向け。`LibraShare/frontend/` の**現状**と、確定設計（[screen-transition.md](./screen-transition.md)、[openapi-notes.md](./openapi-notes.md)、[README.md](../README.md)）の差分を一覧化したものです。

最終更新: F-01（Keycloak 認証・Protected Route・3ロール RBAC）完了後。

---

## 1. ルーティング（App.tsx）

| 状態 | パス | 現状 | 確定設計 | 対応 |
|------|------|------|----------|------|
| 済 | `/` | `/books` へリダイレクト | 同左 | なし |
| 済 | `/books` | 実装済み（mock） | 蔵書一覧 | API 接続（F-02） |
| 済 | `/books/:id` | 実装済み（mock） | 蔵書詳細 | API 接続（F-02） |
| 済 | `/books/new` | 実装済み（mock） | 蔵書追加（admin のみ） | API 接続（F-02a） |
| 済 | `/books/:id/edit` | 実装済み（mock） | 蔵書編集（admin のみ） | API 接続（F-02b） |
| 済 | `/users` | 実装済み（mock） | 利用者一覧（F-06） | API 接続 |
| 済 | `/users/new` | 実装済み（mock） | 利用者登録 | API 接続 |
| 済 | `/users/:id/edit` | 実装済み（mock） | 利用者編集・論理削除 | API 接続 |
| 済 | `/loans/active` | 実装済み（mock） | 貸出中一覧（F-05） | API 接続 |
| 済 | `/access-denied` | 実装済み | `general_user` 拒否 UX | なし |
| 済 | 保護ルート | `ProtectedRoute` で保護 | 未ログイン → Keycloak ログイン、社員ロール必須 | なし |
| — | `/login` | **なし**（廃止） | Keycloak ログイン画面（外部） | なし |
| 未 | 404 | なし | 存在しないパス | 任意（推奨） |

---

## 2. ページ（pages/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `BookListPage.tsx` | 実装済み | mock のみ。`GET /api/books` 接続（F-02） |
| `BookDetailPage.tsx` | 実装済み | mock。API 連携・返却 UI（F-02/F-04） |
| `BookCreatePage.tsx` | 実装済み | mock。`POST /api/books` 接続（F-02a） |
| `BookEditPage.tsx` | 実装済み | mock。`PUT /api/books/{id}` + 削除（F-02b/c） |
| `UserListPage.tsx` | 実装済み | mock。`GET /api/users` 接続（F-06） |
| `UserCreatePage.tsx` | 実装済み | mock。氏名・メールのみ（パスワード欄削除）→ `POST /api/users` 接続（F-06） |
| `UserEditPage.tsx` | 実装済み | mock。`PUT` / `DELETE` + 409 表示（F-06） |
| `LoanBookListPage.tsx` | 実装済み | mock。`GET /api/loans/active` + 返却（F-05） |
| `AccessDeniedPage.tsx` | **済** | `general_user` 拒否 + ログアウト |

---

## 3. コンポーネント（components/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `Layout.tsx` | **済** | ロール別ナビ・ログアウト実装済み |
| `ProtectedRoute.tsx` | **済** | 認証・ロールチェック・`requireAdmin` |
| `BookDetail.tsx` | 実装済み | 利用者選択・貸出 mock。編集リンクは admin 出し分け済み |
| `BookCard.tsx` | 実装済み | 一覧表示のみ（大きな差分なし） |
| `StockBadge.tsx` | 実装済み | 大きな差分なし |
| 削除確認ダイアログ | **未作成** | 蔵書削除・利用者論理削除用（F-02c / F-06） |

### Layout ナビ

| 項目 | 現状 | 確定設計 |
|------|------|----------|
| 書籍一覧 | `/books` | 同左 |
| 貸出中 | `/loans/active` | 同左 |
| 利用者管理 | `/users` | 同左 |
| 蔵書追加 | admin のみ表示 | 同左 |
| ログアウト | `keycloak.logout()` | 同左 |

---

## 4. API 層（api/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `client.ts` | 雛形あり | `localStorage.getItem('token')` → Keycloak の `access_token` 注入に変更（F-02 前） |
| `books.ts` | mock 返却 | `GET/POST/PUT/DELETE /api/books` を apiClient 経由で実装 |
| `users.ts` | mock 返却 | `GET/POST/PUT/DELETE /api/users` を apiClient 経由で実装 |
| `loans.ts` | mock 返却 | `POST /api/loans`, `PUT /api/loans/{id}/return`, `GET /api/loans/active` |

---

## 5. 認証（auth/）

| 項目 | 現状 | 確定設計 |
|------|------|----------|
| `auth/AuthContext.tsx` | **済** | Keycloak 初期化・Context 提供 |
| `auth/roles.ts` | **済** | JWT `realm_access.roles` 判定 |
| Keycloak JS | **済** | `keycloak-js` 導入済み |
| ログイン | **済** | `ProtectedRoute` から `keycloak.login()`（SPA `/login` なし） |
| ログアウト | **済** | `Layout` / `AccessDeniedPage` から `keycloak.logout()` |
| トークン（API） | 未 | `api/client.ts` で `keycloak.token` 注入 |
| トークン更新 | 未 | `updateToken`（余力） |
| `general_user` 拒否 | **済** | `AccessDeniedPage` |

### 環境変数（`.env.development`）

```env
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=LibraShare
VITE_KEYCLOAK_CLIENT_ID=front-client
```

Keycloak realm import: `docker/keycloak/import/LibraShare-realm.json`

---

## 6. 型定義（types/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `Book.ts` | あり | `id` が `string` → API は `number`。要整合（API 接続時） |
| `User.ts` | あり | API 接続時にレスポンス型を確認 |
| `Loan.ts` | あり | API 接続時にレスポンス型を確認 |
| `page.ts` | **未作成** | MVP-B: Page\<T\> |

---

## 7. 機能 ID 別チェックリスト（Member B 担当）

| ID | 機能 | 現状 | 残タスク |
|----|------|------|----------|
| F-01 | Keycloak + Protected Route | **済**（SPA） | `api/client.ts` の JWT 注入（F-02 前） |
| F-02 | 蔵書一覧・詳細 UI | 部分 | API 接続 |
| F-02a | 蔵書追加 UI | 部分 | 画面済み・API 接続 |
| F-02b | 蔵書更新 UI | 部分 | 画面済み・API 接続 |
| F-02c | 蔵書削除 UI | 未 | 確認ダイアログ + API |
| F-04 | 貸出/返却 UI | 部分 | 利用者選択 UI 済み・loans API |
| F-05 | 貸出中一覧 UI | 部分 | 画面済み・API 接続 |
| F-06 | 利用者管理 UI | 部分 | 画面済み・API 接続 |
| F-07 | フロント dev 手順 | 未 | README 追記 |
| F-03 | 検索 UI | 未 | MVP-B |
| F-11 | ページング UI | 未 | MVP-B |
| F-10 | UX polish | 最小限 | MVP-B |

---

## 8. 実装順序の推奨（参考）

1. ~~Keycloak JS + ProtectedRoute + Layout ロール別ナビ（F-01）~~ **済**
2. `api/client.ts` の Keycloak トークン注入
3. 蔵書 API 接続（F-02）→ 蔵書 CRUD API（F-02a/b/c）
4. 利用者管理 API（F-06）
5. 貸出/返却 API（F-04）→ 貸出中一覧 API（F-05）
6. MVP-B（F-03, F-11, F-10）

---

## 関連ドキュメント

- [README.md](../README.md)
- [screen-transition.md](./screen-transition.md)
- [frontend-keycloak-learning.md](./frontend-keycloak-learning.md)
- [openapi-notes.md](./openapi-notes.md)
- [sequence-diagram.md](./sequence-diagram.md)
