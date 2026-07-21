# フロントエンド差分リスト（確定設計との差分）

Member B 向け。`LibraShare/frontend/` の**現状**と、確定設計（[screen-transition.md](./screen-transition.md)、[openapi-notes.md](../api/openapi-notes.md)、[README.md](../../README.md)、[refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md)）の差分を一覧化したものです。

最終更新: 書誌/所蔵分離・checkout 一括貸出の設計確定後。

---

## 1. ルーティング（App.tsx）

| 状態 | パス | 現状 | 確定設計 | 対応 |
|------|------|------|----------|------|
| 済 | `/` | `/books` へリダイレクト | 同左 | なし |
| 済 | `/books` | 実装済み（mock・旧 stockCount） | 書誌一覧（total/available） | API + 型・表示更新（F-02） |
| 済 | `/books/:id` | 貸出 UI あり（mock） | **所蔵一覧テーブル表示**（閲覧のみ・貸出なし） | 貸出 UI 削除 + `GET` holdings（F-02） |
| 済 | `/books/new` | 実装済み（mock） | `initialCopyCount` つき書誌追加 | API 接続（F-02a） |
| 済 | `/books/:id/edit` | 実装済み（mock） | 書誌更新・**所蔵追加/削除**・書誌論理削除 | `PUT` / `POST copies` / `DELETE copies/{copyId}` / `DELETE books` |
| 未 | `/loans/checkout` | **なし** | 利用者選択 | 新規（F-04） |
| 未 | `/loans/checkout/books` | **なし** | 書誌カード + 選択中サマリ | 新規（F-04） |
| 未 | `/loans/checkout/books/:id` | **なし** | 所蔵複数選択 | 新規（F-04） |
| 未 | `/loans/checkout/confirm` | **なし** | 確認 → POST | 新規（F-04） |
| 済 | `/users` | 実装済み（mock） | 利用者一覧（F-06） | API 接続 |
| 済 | `/users/new` | 実装済み（mock） | 利用者登録 | API 接続 |
| 済 | `/users/:id/edit` | 実装済み（mock） | 利用者編集・論理削除 | API 接続 |
| 済 | `/loans/active` | 実装済み（mock） | 貸出中一覧（F-05・bookCopyId） | API 接続・型更新 |
| 済 | `/access-denied` | 実装済み | `general_user` 拒否 UX | なし |
| 済 | 保護ルート | `ProtectedRoute` で保護 | 未ログイン → Keycloak、社員ロール必須 | なし |
| — | `/login` | **なし**（廃止） | Keycloak 外部ログイン | なし |
| 未 | 404 | なし | 存在しないパス | 任意（推奨） |

---

## 2. ページ（pages/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `BookListPage.tsx` | 実装済み | mock・`stockCount`。書誌集計表示へ |
| `BookDetailPage.tsx` | 実装済み | **貸出 UI 削除**。所蔵一覧表示。admin は編集へのみ誘導 |
| `BookCreatePage.tsx` | 実装済み | `initialCopyCount` 入力へ |
| `BookEditPage.tsx` | 実装済み | 冊数手入力廃止。所蔵一覧 + 追加/削除（LOANED は削除不可）+ 書誌削除 |
| checkout 系ページ | **未作成** | `LoanProvider` + サマリ + 確認（F-04） |
| `UserListPage.tsx` 等 | 実装済み | mock。API 接続（F-06） |
| `LoanBookListPage.tsx` | 実装済み | mock。`bookCopyId` 対応・返却（F-05） |
| `AccessDeniedPage.tsx` | **済** | 差分なし |

---

## 3. コンポーネント（components/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `Layout.tsx` | **済** | ナビに **貸出**（`/loans/checkout`）追加 |
| `ProtectedRoute.tsx` | **済** | 大きな差分なし |
| `BookDetail.tsx` | 実装済み | 利用者選択・貸出 mock を削除。所蔵一覧（閲覧） |
| `BookCard.tsx` | 実装済み | `availableCount` / `totalCount` 表示 |
| `StockBadge.tsx` | 実装済み | available 集計表示に合わせる |
| `LoanProvider` / 選択中サマリ | **未作成** | checkout 専用 Context（F-04） |
| 削除確認ダイアログ | **未作成** | 書誌削除・利用者論理削除用 |

### Layout ナビ（確定）

| 項目 | 確定設計 |
|------|----------|
| 書誌一覧 | `/books` |
| 貸出 | `/loans/checkout` |
| 貸出中 | `/loans/active` |
| 利用者管理 | `/users` |
| 書誌追加 | admin のみ |
| ログアウト | `keycloak.logout()` |

---

## 4. API 層（api/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `client.ts` | 雛形あり | Keycloak `access_token` 注入（F-02 前） |
| `books.ts` | mock | 書誌 CRUD + `POST/DELETE .../copies`。`stockCount` 廃止 |
| `users.ts` | mock | 利用者 CRUD |
| `loans.ts` | mock・単件 `bookId` | `createLoans(userId, bookCopyIds)`、active に `bookCopyId` |

---

## 5. 認証（auth/）

（F-01 済みの内容は変更なし。トークン注入のみ F-02 前の宿題）

| 項目 | 現状 | 確定設計 |
|------|------|----------|
| Auth / roles / ProtectedRoute | **済** | 同左 |
| トークン（API） | 未 | `api/client.ts` で `keycloak.token` 注入 |
| `general_user` 拒否 | **済** | `AccessDeniedPage` |

---

## 6. 型定義（types/）

| ファイル | 状態 | 確定設計との差分 |
|----------|------|------------------|
| `Book.ts` | `stockCount` あり | `totalCount` / `availableCount` / `holdings?` |
| `User.ts` | あり | API 接続時に確認 |
| `Loan.ts` | `bookId` 前提 | `bookCopyId` 中心に更新 |
| `page.ts` | **未作成** | MVP-B: Page\<T\> |

---

## 7. 機能 ID 別チェックリスト（Member B 担当）

| ID | 機能 | 現状 | 残タスク |
|----|------|------|----------|
| F-01 | Keycloak + Protected Route | **済**（SPA） | JWT 注入（F-02 前） |
| F-02 | 書誌一覧・詳細 UI | 部分 | API・所蔵一覧表示・貸出 UI 削除 |
| F-02a | 書誌追加 UI | 部分 | `initialCopyCount` + API |
| F-02b | 書誌更新 UI | 部分 | API（冊数なし）+ 編集画面の所蔵追加/削除 |
| F-02c | 書誌削除 UI | 未 | 確認ダイアログ + API + LOANED 409 |
| F-04 | checkout 一括貸出 | 旧詳細貸出のみ | LoanProvider・checkout 全画面・`bookCopyIds` |
| F-05 | 貸出中一覧 UI | 部分 | API・`bookCopyId`・返却 |
| F-06 | 利用者管理 UI | 部分 | API 接続 |
| F-07 | フロント dev 手順 | 未 | README 追記 |
| F-03 / F-11 / F-10 | MVP-B | 未 | Stretch |

---

## 8. 実装順序の推奨（参考）

1. ~~Keycloak JS + ProtectedRoute + Layout（F-01）~~ **済**
2. `api/client.ts` の Keycloak トークン注入
3. 書誌 API 接続（F-02）— 型・一覧・詳細（holdings）・詳細から貸出削除
4. 書誌 CRUD（F-02a/b/c）+ 編集画面での所蔵追加/削除
5. 利用者管理 API（F-06）
6. checkout + 一括貸出（F-04）→ 貸出中一覧（F-05）
7. MVP-B（F-03, F-11, F-10）

---

## 関連ドキュメント

- [README.md](../../README.md)
- [refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md)
- [screen-transition.md](./screen-transition.md)
- [openapi-notes.md](../api/openapi-notes.md)
- [frontend-keycloak-learning.md](../learning/frontend-keycloak-learning.md)
