# 同一書誌は一人一冊（F-04 拡張）学習メモ

レビュー指摘リファクタのフロント実装（Issue A / §7.2）について、**変数・引数を日本語で言い換え**、**`if` 分岐の条件と結果**、データの流れと確認結果をまとめたメモ。

設計の正:

- [review-refactoring-diff.md](../refactoring/review-refactoring-diff.md) §4.3 / §7.2
- [openapi-notes.md](../api/openapi-notes.md)（`BOOK_ALREADY_LOANED_BY_USER`）

関連学習: [front-f04-f05.md](./front-f04-f05.md)（checkout の土台）

---

## 用語

| 用語 | 意味 |
|------|------|
| 書誌（`books`） | タイトル単位の作品。同じ本が何冊あっても書誌は 1 件 |
| 所蔵（`book_copies` / holdings） | 物理的な 1 冊。貸出の正は所蔵 ID（`bookCopyId`） |
| 同一書誌は一人一冊 | 同じ利用者が、同じ書誌を同時に 2 冊以上借りられない。選択段階でも同じ書誌の所蔵を 2 件選べない |
| 異なる書誌の一括貸出 | 書誌 A の所蔵 + 書誌 B の所蔵をまとめて POST するのは可（現状維持） |

---

## 全体の流れ

```text
所蔵選択画面（CheckoutBookDetailPage）
  → LoanContext（選択リストを保持・同一 bookId を拒否）
  → 確認画面（CheckoutConfirmPage）
  → POST /api/loans
  → 成功: clearAll → /loans/active
  → 失敗 409:
       BOOK_ALREADY_LOANED_BY_USER → 選択は残す + メッセージ
       failedBookCopyIds あり     → 失敗した所蔵だけ外す
```

ガードは **2 段**:

1. **UI / Context**（今すぐ効く）… 同じ書誌をカゴに 2 冊入れない  
2. **API 409**（Back マージ後に検証）… 既存 `BORROWED` との衝突など

---

## 1. `LoanContext.tsx` — 選択の正本

対象: `frontend/src/loans/LoanContext.tsx`

### 型

| 名前 | 言語化 |
|------|--------|
| `CheckoutUser` | 貸出先の利用者。`id` = DB の利用者 ID、`displayName` = 画面表示名 |
| `SelectedBookCopy` | カゴに入れた 1 所蔵。`bookCopyId` = 所蔵 ID、`bookId` = 書誌 ID、`title` = 書名 |
| `LoanContextValue` | Provider が子に渡す API 一式（利用者・選択配列・追加/削除/クリア） |

### state

| 名前 | 言語化 |
|------|--------|
| `user` | 今選んでいる利用者。未選択なら `null` |
| `setUser` | 利用者をセット／クリアする関数 |
| `selectedBookCopies` | 選択中の所蔵の配列（カゴの中身） |
| `setSelectedBookCopies` | カゴを更新する React の setter |

### `addCopy(copy)`（今回の追加ガード）

| 名前 | 言語化 |
|------|--------|
| `copy` | 今追加しようとしている所蔵 1 件 |
| `prev` | 更新直前のカゴ（`setSelectedBookCopies` のコールバック引数） |
| `c` | `prev` を走査するときの「すでに入っている 1 件」 |

処理の言語化:

1. `prev` のどこかに、`copy` と同じ **所蔵 ID**（`bookCopyId`）がある → 何もせず `prev` を返す（同じ冊を二重登録しない）  
2. `prev` のどこかに、`copy` と同じ **書誌 ID**（`bookId`）がある → 何もせず `prev` を返す（**同じタイトルは 1 冊まで**）  
3. どちらでもなければ `[...prev, copy]`（末尾に追加）

```tsx
function addCopy(copy: SelectedBookCopy) {
  setSelectedBookCopies((prev) => {
    if (prev.some((c) => c.bookCopyId === copy.bookCopyId)) {
      return prev
    }
    if (prev.some((c) => c.bookId === copy.bookId)) {
      return prev
    }
    return [...prev, copy]
  })
}
```

#### `if` 分岐の解説（`addCopy`）

上から順に評価する。**先にマッチした分岐で return したら、下の分岐は見ない**。

| # | 条件（日本語） | 条件（コード） | 真のとき | 偽のとき |
|---|----------------|----------------|----------|----------|
| 1 | カゴに、今追加したいのと同じ所蔵 ID が既にあるか | `prev.some((c) => c.bookCopyId === copy.bookCopyId)` | カゴを変えず `prev` を返す（二重追加防止） | 次の `if` へ |
| 2 | カゴに、今追加したいのと同じ書誌 ID が既にあるか | `prev.some((c) => c.bookId === copy.bookId)` | カゴを変えず `prev` を返す（同一書誌 2 冊目拒否） | 次へ |
| 3 | （どちらにも当てはまらない） | — | `[...prev, copy]` で末尾に追加 | — |

```text
addCopy(copy)
  │
  ├─ if 所蔵IDが既にある？ ──真──► return prev（何もしない）
  │
  ├─ if 書誌IDが既にある？ ──真──► return prev（何もしない）
  │
  └─ 偽・偽 ──► return [...prev, copy]（追加）
```

例:

- カゴが空で所蔵 #12（書誌 1）を追加 → #1 偽・#2 偽 → 追加される  
- すでに #12 が入っているのに再度 #12 を追加 → #1 真 → 無視  
- すでに #12（書誌 1）があり、所蔵 #13（同じ書誌 1）を追加 → #1 偽・#2 真 → 無視  
- 所蔵 #12（書誌 1）と所蔵 #25（書誌 2）→ 書誌が違うので #2 は偽 → 両方入る  

### その他の関数

| 名前 | 言語化 |
|------|--------|
| `removeCopy(bookCopyId)` | 引数の所蔵 ID と一致する行だけカゴから外す |
| `clearSelection` | カゴだけ空にする（利用者は残す） |
| `clearAll` | 利用者もカゴもリセット（貸出成功後など） |
| `value` | 上記をまとめたオブジェクト。`useMemo` で `user` / `selectedBookCopies` が変わったときだけ作り直す |
| `children` | Provider の内側に来る画面ツリー |
| `ctx` | `useLoanCheckout` が Context から取り出した値。Provider 外ならエラー |

---

## 2. `CheckoutBookDetailPage.tsx` — 選択 UI

対象: `frontend/src/pages/CheckoutBookDetailPage.tsx`

### 画面まわりの変数

| 名前 | 言語化 |
|------|--------|
| `id` | URL の書誌 ID（文字列。`/loans/checkout/books/:id`） |
| `apiClient` | Bearer JWT 付き axios |
| `selectedBookCopies` | Context のカゴ（読み取り） |
| `addCopy` / `removeCopy` | カゴへ追加／削除 |
| `book` | API から取った書誌。未取得・見つからないときは `null` |
| `loading` | 取得中なら `true` |
| `error` | 取得失敗などのメッセージ。なければ `null` |
| `ignore` | 画面を離れたあと古いレスポンスで state を更新しないためのフラグ |
| `data` | `fetchBookById` の戻り（書誌 or `null`） |
| `err` | catch したエラー |

#### `if` 分岐の解説（描画前の early return）

コンポーネント本体の先頭で、**描画できる状態になるまで段階的に止める**。

| # | 条件 | 真のとき | 偽のとき |
|---|------|----------|----------|
| 1 | `if (loading)` | 「読み込み中…」だけ出して終了 | 次へ |
| 2 | `if (error)` | エラー文言だけ出して終了 | 次へ |
| 3 | `if (!book)` | 「書籍が見つかりません」で終了 | 次へ（ここ以降 `book` は描画上ある前提） |

```text
描画開始
  ├─ loading？ ──真──► 「読み込み中」return
  ├─ error？   ──真──► エラー表示 return
  ├─ book なし？──真──► 見つからない return
  └─ すべて偽 ──► 所蔵リストを描画
```

### ガード用の変数

| 名前 | 言語化 |
|------|--------|
| `holdings` | この書誌の所蔵一覧。`book.holdings` が無ければ空配列 `[]`（`??`） |
| `alreadySelectedForThisBook` | 「カゴの中に、**今見ている書誌と同じ `bookId` の所蔵が 1 件でもあるか**」。あれば `true` |

`some((c) => c.bookId === book.id)` の言語化:  
「カゴの各要素 `c` について、その書誌 ID が今の書誌 ID と等しいものが一つでもあるか」

### `onToggle(holding, checked)`

| 名前 | 言語化 |
|------|--------|
| `holding` | クリックした行の所蔵（`id` = 所蔵 ID、`status` = AVAILABLE / LOANED） |
| `checked` | checkbox がオンになったら `true`、オフなら `false` |

処理の言語化:

1. `book` が無い、または所蔵が貸出中 → 何もしない  
2. オンにするとき:  
   - カゴに「同じ書誌で、**別の所蔵 ID**」がある → return（2 冊目拒否）  
   - なければ `addCopy`（所蔵 ID・書誌 ID・書名を渡す）  
3. オフにするとき: `removeCopy(holding.id)` でその所蔵だけ外す  

#### `if` 分岐の解説（`onToggle`）

```tsx
function onToggle(holding: BookHolding, checked: boolean) {
  if (!book || holding.status !== "AVAILABLE") return

  if (checked) {
    if (
      selectedBookCopies.some(
        (c) => c.bookId === book.id && c.bookCopyId !== holding.id
      )
    ) {
      return
    }
    addCopy({ bookCopyId: holding.id, bookId: book.id, title: book.title })
  } else {
    removeCopy(holding.id)
  }
}
```

| # | 条件（日本語） | 条件（コード） | 真のとき | 偽のとき |
|---|----------------|----------------|----------|----------|
| 1a | 書誌データが無い | `!book` | 何もしないで return | 1b も見る（`\|\|`） |
| 1b | この所蔵は貸出中 | `holding.status !== "AVAILABLE"` | 何もしないで return | 次へ |
| 2 | checkbox をオンにした | `if (checked)` | 内側の同一書誌チェックへ | `else`（オフ）へ |
| 3 | カゴに「同じ書誌・別所蔵」がある | `some(c => c.bookId === book.id && c.bookCopyId !== holding.id)` | return（2 冊目拒否） | `addCopy` する |
| else | checkbox をオフにした | `else`（`checked` が偽） | `removeCopy(holding.id)` | — |

条件 #1 の `||`（または）の言語化:

- `!book` が真 **または** 貸出中が真 → まとめて return  
- 両方とも偽（書誌あり **かつ** 貸出可）だけ先へ進む  

条件 #3 の `&&`（かつ）の言語化:

- カゴの要素 `c` が「今の書誌と同じ」**かつ**「今クリックした所蔵とは別 ID」  
- これが一つでもあれば「すでに別の冊を選んでいる」＝ 2 冊目になるので拒否  

```text
onToggle(holding, checked)
  │
  ├─ if (!book || 貸出中) ──真──► return（無視）
  │
  ├─ if (checked) ──真──►
  │       │
  │       ├─ if 同じ書誌の別所蔵がカゴにある？ ──真──► return（拒否）
  │       │
  │       └─ 偽 ──► addCopy(...)
  │
  └─ else（オフ）──► removeCopy(holding.id)
```

例:

| 操作 | 分岐の辿り | 結果 |
|------|------------|------|
| 貸出中の所蔵をオン | #1 真 | 何も起きない |
| 空カゴで所蔵 #12 をオン | #1 偽 → #2 真 → #3 偽 | `addCopy` |
| #12 選択済みで #13（同書誌）をオン | #1 偽 → #2 真 → #3 真 | return（拒否） |
| #12 をオフ | #1 偽 → #2 偽 → else | `removeCopy` |

#### なぜ `if (!book)` を関数内でも書くか

外側で `if (!book) return ...` していても、**ネストした `function onToggle` の中では TypeScript が `book` を `Book | null` のまま**扱うことがある（呼び出し時点で state が変わり得るため）。

- 型: 再ガード後は `book.id` / `book.title` を `Book` として扱える  
- 実行時: 「書誌が消えたあと古いハンドラが動く」可能性への保険  

別案: early return の直後に `const currentBook = book` と固定すると、ネスト関数でも `Book` のまま残る。

### リスト内（`map`）

| 名前 | 言語化 |
|------|--------|
| `h` | 所蔵 1 行 |
| `checked` | この所蔵がカゴに入っているか（`bookCopyId === h.id`） |
| `disabled` | 操作不可か。次のどちらかなら true: (1) 貸出中（`LOANED`）(2) **この書誌はすでに別の所蔵を選んでいて、かつ今の行は未選択**（`alreadySelectedForThisBook && !checked`） |

(2) の言語化:  
「同じタイトルの別冊はもう選べない。ただし **今チェック中の行** は外せるように、`!checked` の行だけ無効化する」

```tsx
const disabled =
  h.status === "LOANED" || (alreadySelectedForThisBook && !checked)
```

#### `if` / 条件式の解説（`disabled`）

`disabled` は代入式だが、中身は `if` と同じ論理。真なら checkbox 操作不可。

| 部分 | 条件（日本語） | 真の意味 |
|------|----------------|----------|
| A | `h.status === "LOANED"` | この所蔵は貸出中 → 選べない |
| B | `alreadySelectedForThisBook` | この書誌の所蔵をカゴに既に 1 件入れている |
| C | `!checked` | **今の行はまだチェックされていない**（別の行が選ばれている側） |
| A \|\| (B && C) | A が真、または（B かつ C）が真 | 無効化 |

真理値の例（同じ書誌に所蔵 #12 / #13）:

| カゴの状態 | 行 | A (LOANED) | B | C (!checked) | disabled |
|------------|-----|------------|---|--------------|----------|
| 空 | #12 | 偽 | 偽 | 真 | **偽**（選べる） |
| #12 選択中 | #12 | 偽 | 真 | 偽（自分は checked） | **偽**（外せる） |
| #12 選択中 | #13 | 偽 | 真 | 真 | **真**（押せない） |
| #12 が貸出中 | #12 | 真 | （不問） | （不問） | **真** |

`onChange={(e) => onToggle(h, e.target.checked)}`  
→ イベント `e` の「今チェックされたか」を `onToggle` に渡す。

説明文「同じ書誌は 1 冊まで選択できます。」はユーザー向けのルール表示（Context のガードと二重）。

JSX の三項 `holdings.length === 0 ? … : …`:

| 条件 | 真 | 偽 |
|------|----|----|
| 所蔵が 0 件か | 「所蔵がありません」 | `<ul>` で一覧を描く |

---

## 3. `loans.ts` — API エラーの正規化

対象: `frontend/src/api/loans.ts`

### リクエスト / レスポンス型

| 名前 | 言語化 |
|------|--------|
| `CreateLoanRequest.userId` | 貸出先利用者 ID |
| `CreateLoanRequest.bookCopyIds` | 貸す所蔵 ID の配列 |
| `CreateLoanResponse.loans` | 作成された貸出レコードの配列 |
| `ActiveLoan` | 貸出中一覧 1 行用の型 |

### エラーボディ・クラス

| 名前 | 言語化 |
|------|--------|
| `LoanApiErrorBody.message` | 人間向け文言 |
| `LoanApiErrorBody.error` | 機械向けコード（例: `BOOK_ALREADY_LOANED_BY_USER`） |
| `LoanApiErrorBody.failedBookCopyIds` | 貸出できなかった所蔵 ID の配列（在庫衝突など） |
| `LoanError` | 画面が扱う例外。`message` + 追加情報 |
| `LoanError.status` | HTTP ステータス（403 / 409 など） |
| `LoanError.errorCode` | API の `error` をそのまま載せたもの（画面の分岐用） |
| `LoanError.failedBookCopyIds` | 失敗した所蔵 ID（確認画面で `removeCopy` に使う） |

### `toLoanError(err)`

| 名前 | 言語化 |
|------|--------|
| `err` | catch した未知のエラー |
| `status` | レスポンスの HTTP ステータス |
| `data` | レスポンス JSON（上の ErrorBody） |
| `errorCode` | `data.error`（例: 一人一冊コード） |
| `message` | `data.message`、無ければ axios の `err.message` |
| `ids` | `failedBookCopyIds` |
| `text` | 画面表示用に ID を足した文 |

#### `if` 分岐の解説（`toLoanError`）

外側から内側へ、**エラーの種類を段階的に振り分ける**。

| # | 条件 | 真のとき | 偽のとき |
|---|------|----------|----------|
| 1 | `if (isAxiosError(err))` | HTTP レスポンス付きの axios エラーとして処理 | #5 へ（普通の Error か不明） |
| 2 | `if (status === 403)` | 「権限がありません」の `LoanError` を return | 次へ |
| 3 | `if (status === 409)` | 業務衝突。内側の #3a / #3b へ | #4 へ（その他ステータス） |
| 3a | `if (errorCode === "BOOK_ALREADY_LOANED_BY_USER")` | 一人一冊用メッセージ。**`failedBookCopyIds` なし**で return | #3b へ |
| 3b | （409 のその他） | 在庫不可など。`ids` があれば文言に「所蔵 #…」を付け、`failedBookCopyIds` 付きで return | — |
| 4 | （axios だが 403/409 以外） | `message` / `errorCode` だけ載せた `LoanError` | — |
| 5 | `if (err instanceof Error)` | 一般 Error の `message` を載せた `LoanError` | #6 へ |
| 6 | （どれでもない） | 「不明なエラーが発生しました」 | — |

```text
toLoanError(err)
  │
  ├─ axios エラー？ ──偽──► Error？ ──真──► LoanError(message)
  │                      └─偽──► LoanError(不明…)
  │
  └─真
      ├─ status === 403？ ──真──► LoanError(権限がありません)
      │
      ├─ status === 409？ ──真──►
      │       │
      │       ├─ errorCode が BOOK_ALREADY_LOANED_BY_USER？
      │       │     真──► LoanError(一人一冊…)  ※ failedBookCopyIds なし
      │       │     偽──► LoanError(在庫不可…)  ※ failedBookCopyIds あり得る
      │
      └─ その他ステータス ──► LoanError(message, errorCode)
```

メッセージ組み立て（#3b 内）の三項演算子:

| 条件 | 真 | 偽 |
|------|----|----|
| `ids && ids.length > 0` | `` `${message}（所蔵 #${ids.join(", #")}）` `` | `message` が空なら固定文言「貸出できない所蔵が含まれています」 |

ポイント: **同じ 409 でも `errorCode` で分岐する**。画面側は「一人一冊」と「所蔵 ID 付き在庫衝突」を別扱いできる。

`createLoan` / `returnLoans` / `fetchActiveLoans` は失敗時に必ず `toLoanError` 経由で投げる。

---

## 4. `CheckoutConfirmPage.tsx` — 確定送信

対象: `frontend/src/pages/CheckoutConfirmPage.tsx`

| 名前 | 言語化 |
|------|--------|
| `apiClient` | axios |
| `navigate` | 画面遷移用 |
| `user` | 貸出先利用者（無ければ利用者選択へリダイレクト） |
| `selectedBookCopies` | 確認リストの元データ |
| `removeCopy` | 失敗した所蔵だけ外す用 |
| `clearAll` | 成功後にカゴ＋利用者をリセット |
| `submittingRef` | 連打で二重 POST しない同期ロック（`true` = 送信中） |
| `submitting` | ボタン文言・`disabled` 用の state（再描画を起こす） |
| `error` | 画面に出す失敗メッセージ |

### 描画前の早期分岐

| # | 条件 | 真のとき |
|---|------|----------|
| 1 | `if (!user)` | `/loans/checkout`（利用者選択）へリダイレクト |
| 2 | `if (selectedBookCopies.length === 0)` | `/loans/checkout/books`（書誌一覧）へリダイレクト |

### `handleSubmit` の言語化

1. 利用者なし／カゴ空 → return  
2. すでに `submittingRef.current === true` → return（二重送信防止）  
3. ロック ON → `createLoan({ userId, bookCopyIds })`  
   - `bookCopyIds` = カゴの各要素 `c` から `bookCopyId` だけ抜き出した配列  
4. 成功 → `clearAll` → `/loans/active`  
5. 失敗（`e`）:  
   - `LoanError` かつ `errorCode === "BOOK_ALREADY_LOANED_BY_USER"`  
     → メッセージ＋「選び直す」案内。**選択は消さない**。`return`（その後の共通 `setError` は走らない）  
   - `failedBookCopyIds` がある  
     → 各 `id` を `removeCopy`（失敗分だけカゴから除去）  
   - それ以外も含め、最後に `setError`（一人一冊のときだけ先に return 済み）  
6. `finally` でロック解除・`submitting` を `false`  

#### `if` 分岐の解説（`handleSubmit`）

```text
handleSubmit()
  │
  ├─ if (!user || カゴが空) ──真──► return（送信しない）
  │
  ├─ if (submittingRef.current) ──真──► return（二重送信防止）
  │
  ├─ ロック ON → createLoan(...)
  │
  ├─ try 成功 ──► clearAll → /loans/active
  │
  └─ catch (e)
        │
        ├─ if (e は LoanError) ──偽──► 下の setError へ落ちる
        │       │
        │       ├─ if errorCode が BOOK_ALREADY_LOANED_BY_USER
        │       │     真──► setError(案内文) → return
        │       │           ※ カゴは触らない / 下の setError はスキップ
        │       │
        │       └─ if failedBookCopyIds に 1 件以上ある
        │             真──► 各 id を removeCopy
        │             偽──► カゴはそのまま
        │
        └─ setError(メッセージ)   ← 一人一冊で return したときはここまで来ない
  finally
        └─ ロック OFF（catch で return しても必ず実行）
```

| # | 条件 | 真のとき | 偽のとき |
|---|------|----------|----------|
| 1 | `!user \|\| selectedBookCopies.length === 0` | 送信せず return | 次へ |
| 2 | `submittingRef.current`（すでに true） | 送信せず return（連打ガード） | ロックを true にして POST |
| 3 | `e instanceof LoanError` | エラーコード／失敗 ID を見る | 一般メッセージで `setError` |
| 3a | `e.errorCode === "BOOK_ALREADY_LOANED_BY_USER"` | 案内メッセージを出して **return**（カゴ維持） | 次へ |
| 3b | `e.failedBookCopyIds?.length`（1 以上） | 失敗した所蔵だけ `removeCopy` | カゴはそのまま |
| 最後 | （#3a で return していない場合） | `setError(...)` | — |

`?`（オプショナルチェーン）の言語化:  
`e.failedBookCopyIds?.length` … `failedBookCopyIds` が `undefined` なら式全体が `undefined`（偽扱い）。中身があるときだけ件数を見る。

`catch` で `return` しても **`finally` は必ず実行**されるので、ロックは外れる。

画面の `c` / `index`: カゴの 1 件と通し番号。表示は「書名 所蔵 #所蔵ID」。

JSX: `{error && <p>…</p>}` … `error` が文字列なら段落を出す。`null` なら何も出さない。

---

## 確認結果（合否）

| チェック | 結果 |
|----------|------|
| `addCopy` で同一 `bookId` 拒否 | OK |
| 詳細で 2 冊目 `disabled` + `onToggle` 拒否 | OK |
| `book` null を `onToggle` 内で再ガード | OK |
| `errorCode` で一人一冊と在庫衝突を分岐 | OK |
| 一人一冊時は選択を残す | OK |
| `submittingRef` 維持 | OK |

### 注意点

1. 文言は docs 上「書誌」が正。UI は「書籍」と揺れていてもよいが、揃えるなら「書誌」に。  
2. Back がまだ `BOOK_ALREADY_LOANED_BY_USER` を返さない間は、409 分岐のうち一人一冊側は **実 API では未検証**。UI / Context ガードは今すぐ効く。

---

## 手動確認の目安

1. 同一書誌の所蔵を 2 つ → 2 件目が押せない  
2. チェック解除 → また選べる  
3. 別書誌は同時選択できる  
4. （Back マージ後）既存貸出と衝突 → メッセージ、カゴは残る  
5. 連打しても POST は 1 回  

---

## 対象ファイル一覧

| ファイル | 役割 |
|----------|------|
| `frontend/src/loans/LoanContext.tsx` | 同一 `bookId` の追加拒否 |
| `frontend/src/pages/CheckoutBookDetailPage.tsx` | checkbox UI ガード + 説明文 |
| `frontend/src/api/loans.ts` | `LoanError.errorCode` + 409 分岐 |
| `frontend/src/pages/CheckoutConfirmPage.tsx` | 一人一冊時は選択維持、在庫衝突は ID 除去 |

---

## `if` 分岐クイック索引

| 場所 | 主な分岐 | 一言 |
|------|----------|------|
| `addCopy` | 所蔵 ID 重複 → 書誌 ID 重複 → 追加 | 上から拒否、どちらも偽なら追加 |
| 詳細ページ early return | loading → error → !book | 描画できるまで止める |
| `onToggle` | !book/貸出中 → checked → 同書誌別所蔵 | オン時だけ 2 冊目拒否 |
| `disabled` | LOANED \|\| (同書誌選択済み && 未チェック行) | 選んだ行は外せる |
| `toLoanError` | axios → 403 → 409 → 一人一冊 / その他 | 同じ 409 をコードで分ける |
| `handleSubmit` | 前提不足 → 連打 → catch で一人一冊 / ID 除去 | 一人一冊は return でカゴ維持 |
