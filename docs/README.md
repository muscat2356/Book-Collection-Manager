# LibraShare ドキュメント索引

設計の正は本ディレクトリ配下の **design / database / api** を併読してください。学習メモは `learning/` に分離しています。

## カテゴリ一覧

| カテゴリ | パス | 内容 |
|----------|------|------|
| 設計 | [design/](./design/) | 画面・フロー・所蔵分離・フロント差分 |
| DB | [database/](./database/) | ER 図・Flyway 参考 SQL |
| API | [api/](./api/) | OpenAPI 仕様・補足メモ |
| リファクタリング | [refactoring/](./refactoring/) | 変更差分・実装チェックリスト |
| 将来検討 | [future/](./future/) | MVP-B 以降の候補 |
| 学習 | [learning/](./learning/) | 学習メモ（設計の正ではない） |
| その他 | [misc/](./misc/) | コマンド例・サンプル設定 |

## 設計（design/）

| ファイル | 説明 |
|----------|------|
| [refactor-holdings-and-checkout.md](./design/refactor-holdings-and-checkout.md) | 書誌/所蔵分離・checkout 一括貸出（確定） |
| [screen-transition.md](./design/screen-transition.md) | 画面・ルート |
| [sequence-diagram.md](./design/sequence-diagram.md) | シーケンス図 |
| [frontend-gap-analysis.md](./design/frontend-gap-analysis.md) | フロント差分リスト |
| [refact-plane.md](./design/refact-plane.md) | 当初たたき台（参照用） |

## DB（database/）

| ファイル | 説明 |
|----------|------|
| [er-diagram.md](./database/er-diagram.md) | ER 図・制約・到達 SQL |
| [sql/seed-publisher-categories.sql](./database/sql/seed-publisher-categories.sql) | 出版社・カテゴリ追記用参考 SQL |

## API（api/）

| ファイル | 説明 |
|----------|------|
| [openapi.yaml](./api/openapi.yaml) | OpenAPI 3.1 仕様（MVP-A） |
| [openapi-notes.md](./api/openapi-notes.md) | API 補足メモ・JSON 例 |

## リファクタリング（refactoring/）

| ファイル | 説明 |
|----------|------|
| [review-refactoring-diff.md](./refactoring/review-refactoring-diff.md) | レビュー指摘の設計差分（出版社・カテゴリ・一人一冊）＋影響箇所・Back/Front 対応 |
| [book-copies-soft-delete-changes.md](./refactoring/book-copies-soft-delete-changes.md) | 所蔵論理削除の実装チェックリスト |

## 将来検討（future/）

| ファイル | 説明 |
|----------|------|
| [future-considerations.md](./future/future-considerations.md) | バッチ・延滞・外部 API 等 |

## 学習（learning/）

| ファイル | 説明 |
|----------|------|
| [front-f04-f05.md](./learning/front-f04-f05.md) | F-04/F-05 学習メモ |
| [front-api-learning.md](./learning/front-api-learning.md) | API クライアント学習 |
| [frontend-keycloak-learning.md](./learning/frontend-keycloak-learning.md) | Keycloak 学習 |

## プロジェクトルートとの関係

- 機能スコープ・起動手順の要約: [../README.md](../README.md)
- Agent 向けガイド: [../AGENTS.md](../AGENTS.md)
