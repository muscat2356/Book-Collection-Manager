# 蔵書管理・貸出機能 設計変更方針

> **確定設計（正）**: [refactor-holdings-and-checkout.md](./refactor-holdings-and-checkout.md)（設計 docs 反映済み）  
> 本ファイルは当初のたたき台。barcode / returnDate / 横断 copy 一覧などは確定設計で見送りまたは変更あり。

## 背景

現在のアプリケーションでは、本詳細画面から貸出を開始し、利用者を選択して1冊ずつ貸出APIを実行する設計になっている。

現在のフロー：

```
本一覧
 ↓
本詳細
 ↓
利用者選択
 ↓
貸出実行
```

しかし、実際の図書館司書が利用するバックオフィスを想定すると、複数冊をまとめて貸し出す業務フローが必要。

また、同じタイトルの本を複数冊所蔵するケースを考慮し、書誌情報と実際の蔵書を分離する設計へ変更する。

---

# 蔵書管理モデル変更

## 現状

現在は本情報を1テーブルで管理している。

例：

```
books
-----
id
title
author
isbn
```

この設計では、同じタイトルの本を複数冊所蔵した場合に区別できない。

例：

```
Java入門
Java入門
Java入門
```

を管理できない。

---

# 変更後のテーブル設計

## books（書誌情報）

タイトル単位の情報を管理する。

例：

```
books
-----
id
title
author
isbn
```

管理対象：

* タイトル
* 著者
* ISBN
* 出版情報

---

## book_copies（蔵書）

実際の1冊単位の情報を管理する。

例：

```
book_copies
------------
id
book_id
barcode
status
location
```

管理対象：

* 蔵書管理番号
* 貸出状態
* バーコード
* 配置場所

例：

| id  | book_id | title  | status    |
| --- | ------- | ------ | --------- |
| 101 | 1       | Java入門 | AVAILABLE |
| 102 | 1       | Java入門 | LOANED    |
| 103 | 1       | Java入門 | AVAILABLE |

同じタイトルでも1冊ずつ管理できるようにする。

---

# 在庫管理方針

在庫表示や検索は `book_id` 単位で集計する。

例：

Java入門の貸出可能冊数

```sql
SELECT COUNT(*)
FROM book_copies
WHERE book_id = ?
AND status = 'AVAILABLE';
```

表示例：

```
Java入門
貸出可能：2冊
所蔵：3冊
```

---

# 貸出対象について

貸出処理対象は `book_id` ではなく `book_copy.id` とする。

理由：

同じタイトルを複数冊借りる場合に区別する必要があるため。

例：

```json
{
  "userId": 1,
  "bookCopyIds": [
    101,
    103
  ]
}
```

---

# 貸出画面フロー変更

## 変更前

```
本詳細
 ↓
利用者選択
 ↓
貸出
```

## 変更後

貸出処理の入口は貸出管理画面に統一する。

```
貸出管理
 ↓
利用者選択
 ↓
貸出本選択（複数）
 ↓
貸出内容確認
 ↓
貸出完了
```

---

# 本詳細画面

本詳細画面から直接貸出するボタンは削除する。

理由：

* 1冊貸出と複数冊貸出の2フローを作らない
* 貸出業務フローを統一する
* 状態管理を単純化する
* API呼び出しを一括化する

将来的に必要なら「貸出対象に追加」機能として拡張する。

---

# 貸出本選択画面

仕様：

* 蔵書一覧（book_copies）を表示
* 複数選択可能
* チェックボックス形式
* 貸出中(status=LOANED)は選択不可
* 選択した蔵書IDを保持する

例：

```
□ Java入門 No.101
□ Java入門 No.103
□ React実践 No.201
```

---

# React状態管理

貸出フロー中の状態はContext APIで管理する。

LoanProviderを作成する。

保持する情報：

```ts
{
  user,
  selectedBookCopies,
  returnDate
}
```

画面間でpropsやnavigate stateによる受け渡しは行わない。

構成：

```tsx
<LoanProvider>
  <LoanUserSelect />
  <LoanBookSelect />
  <LoanConfirm />
</LoanProvider>
```

---

# API設計

貸出APIは複数冊一括登録に変更する。

例：

```
POST /api/loans
```

Request:

```json
{
  "userId": 1,
  "bookCopyIds": [
    101,
    103
  ],
  "returnDate": "2026-07-28"
}
```

バックエンドでは：

1. book_copiesの貸出可能確認
2. status更新
3. loan登録

を1トランザクションで実行する。

途中失敗時はロールバックする。

---

# 設計目的

今回の変更目的：

* 司書向けバックオフィスとして自然な業務フローにする
* 複数冊一括貸出に対応する
* 同じタイトルの複数冊管理に対応する
* 書誌情報と蔵書情報を分離する
* API呼び出し回数を削減する
* Reactの状態管理をContext APIで整理する

既存実装を確認しながら、影響範囲を調査した上で段階的にリファクタリングすること。
