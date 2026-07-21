# 所蔵論理削除 — バック / フロント変更箇所

契約の正は更新済み docs（[er-diagram.md](./er-diagram.md) / [openapi-notes.md](./openapi-notes.md) / [refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md) / [README.md](../README.md)）。  
本ファイルは **実装時の変更チェックリスト**（Member A = バック主、Member B = フロント確認）。

## 方針（再掲）

| 項目 | 内容 |
|------|------|
| 方式 | `book_copies.deleted BOOLEAN NOT NULL DEFAULT false`（`books.deleted` と同型） |
| 成功 | `204`（行は残し `deleted=true`） |
| 409 | **`status=LOANED` のみ**（メッセージ: `貸出中の所蔵のため削除できません`） |
| 履歴 | 過去 `loans` があっても `AVAILABLE` なら論理削除可 |
| 集計・表示・貸出 | `deleted=false` のみ対象 |

---

## Backend（Member A）

### 1. Flyway（新規のみ。既存 V* は書き換えない）

| ファイル | 内容 |
|----------|------|
| `backend/src/main/resources/db/migration/V5__add_deleted_to_book_copies.sql`（新規） | `ALTER TABLE book_copies ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;` |

### 2. Domain

| ファイル | 変更 |
|----------|------|
| `backend/.../domain/BookCopy.java` | `deleted` フィールド（初期値 `false`）+ getter / setter。コンストラクタでは `deleted=false` |

### 3. Repository

| ファイル | 変更 |
|----------|------|
| `backend/.../repository/BookCopyRepository.java` | `findByBookIdAndDeletedFalse(Long bookId)` を追加（集計・holdings 用） |
| 同上 | `findByIdsForUpdate` の JPQL に **`c.deleted = false`**（必要なら AVAILABLE 条件も）を追加し、削除済みを貸出せないようにする |
| 同上 | 既存 `findByBookId` は、未削除限定に寄せるか、呼び出し側でフィルタ |

### 4. Service

| ファイル | メソッド | 変更 |
|----------|----------|------|
| `backend/.../service/BookService.java` | `deleteCopies` | 物理 `delete` をやめ論理削除。シグネチャを `deleteCopies(Long bookId, Long copyId)` に |
| 同上 | `deleteCopies` | ① 所蔵なし → 404 ② 書誌 `bookId` と所属不一致 → 404 ③ 既に `deleted=true` → 404 ④ `LOANED` → `BusinessException("COPY_NOT_DELETABLE", "貸出中の所蔵のため削除できません")` ⑤ `setDeleted(true)` → `save` |
| 同上 | `toResponse` | `findByBookIdAndDeletedFalse` で `totalCount` / `availableCount` / `holdings` を算出 |
| 同上 | `deleteBook` | LOANED 判定は **未削除所蔵のみ**（`deleted=false`） |
| 同上 | （任意）メッセージ | 書誌削除の 409 文言が「履歴」を含むなら、所蔵側と揃えて見直す |

### 5. Controller

| ファイル | 変更 |
|----------|------|
| `backend/.../controller/BookController.java` | `deleteCopies(@PathVariable Long id, @PathVariable Long copyId)` から `bookService.deleteCopies(id, copyId)` を呼ぶ（いまは `copyId` のみ渡している） |

### 6. 貸出（削除済みを拾わない）

| ファイル | 変更 |
|----------|------|
| `backend/.../service/LoansService.java` | `findByIdsForUpdate` 経由で削除済みが来ないこと。来ても `AVAILABLE` かつ未削除以外は既存の失敗扱いに乗せる |

### Backend 受け入れ確認

- [ ] AVAILABLE 削除 → 204、DB 行残存 + `deleted=true`
- [ ] LOANED 削除（API 直叩き）→ 409 + 上記メッセージ
- [ ] 既削除 / 書誌不一致 → 404
- [ ] `GET /api/books/{id}` の holdings・冊数から消える
- [ ] 削除済み copyId で `POST /api/loans` できない
- [ ] 返却済み履歴つき AVAILABLE も論理削除できる

---

## Frontend（Member B）

契約（パス・204 / 409）は維持のため、**必須のコード変更はほぼなし**。結合確認が主。

### 変更不要（契約維持）

| ファイル | 理由 |
|----------|------|
| `frontend/src/api/books.ts` の `deleteBookCopy` | 引き続き `DELETE /api/books/{bookId}/copies/{copyId}` |
| `frontend/src/types/Book.ts` | holdings は API が未削除のみ返す前提なら `deleted` 必須ではない |

### 現状維持でよい UI

| ファイル | 内容 |
|----------|------|
| `frontend/src/pages/BookEditPage.tsx` | `canDelete = h.status === "AVAILABLE"` のまま |
| 同上 | 409 時は既存の `copyError` + `toErrorMessage`（API の `message`）で表示 |
| 同上 | 成功後 `reloadBook()` で一覧・冊数が減ることを確認 |

### 任意（やりたければ）

| ファイル | 内容 |
|----------|------|
| `frontend/src/types/Book.ts` | `BookHolding` に `deleted?: boolean`（通常レスポンスには出ない想定） |
| 詳細 / checkout の holdings 表示 | API が未削除のみなら変更不要。もし全件返す実装なら `deleted` でフィルタ |

### Frontend 受け入れ確認

- [ ] 編集画面で AVAILABLE 削除 → 一覧から消え、冊数更新
- [ ] LOANED は削除ボタン disabled
- [ ] （すり抜け時）409 文言が所蔵セクションの `copyError` に出る
- [ ] 詳細・checkout に削除済み所蔵が出ない

---

## docs（済）

既に更新済み。実装中に文言ずれがあれば再度揃える。

- `docs/er-diagram.md`
- `docs/openapi-notes.md`
- `docs/refactor-holdings-and-checkout.md`
- `docs/screen-transition.md`
- `docs/sequence-diagram.md`
- `README.md`

---

## 実装順（推奨）

1. Flyway V5 → Entity → Repository  
2. `BookService.deleteCopies` + `toResponse` / `deleteBook` / 貸出クエリ  
3. Controller で `bookId` を渡す  
4. フロント結合確認  

完了後、本ファイルは削除してよい（一時チェックリスト）。
