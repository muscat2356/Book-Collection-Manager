# 将来検討事項

MVP-A / MVP-B のスコープ外とし、Phase 2 以降で検討する候補をまとめる。

---

## バッチ処理（延滞管理）

### MVP で不要とする理由

- 現行スコープに **延滞罰金** は含まれない
- MVP-A の貸出フローは **リクエスト駆動**（借りる/返す API）で完結する
- バッチ（`@Scheduled`、Spring Batch 等）は学習・実装コストが大きい

### 将来の候補機能

| 機能 | 概要 |
|------|------|
| 返却期限 | `loans` に `due_at` カラムを追加 |
| 延滞ステータス更新 | 日次ジョブで `status: BORROWED` → `OVERDUE` に更新 |
| 延滞通知 | メール送信（Keycloak 連携または外部サービス） |
| 貸出統計レポート | 月次・年次の貸出件数、人気書籍ランキング |

### DB 拡張案（参考）

MVP-A では不要。将来検討時に `loans` テーブルを拡張する想定。

```
loans に due_at, overdue_at を追加
```

| カラム | 用途 |
|--------|------|
| `due_at` | 返却期限日時 |
| `overdue_at` | 延滞と判定した日時（バッチ更新） |

### 実装の選択肢

| 方式 | メリット | デメリット |
|------|----------|------------|
| `@Scheduled` + JDBC | シンプル、学習コスト低 | 大規模データには不向き |
| Spring Batch | 大量データ・再実行に強い | 設定・学習コスト高 |
| 外部ジョブ（cron + API） | アプリと分離 | インフラ追加が必要 |

---

## 貸出一覧エンドポイントの設計（履歴全件出力への拡張）

### 現状（MVP-A）

貸出中の把握だけが目的のため、専用ビューとして `GET /api/loans/active`（`status=BORROWED` のみ）を提供している。

### 将来の課題

履歴全件出力・返却済み一覧・延滞一覧などを扱うようになると、状態ごとに専用エンドポイントが増殖する懸念がある。

```
GET /api/loans/active
GET /api/loans/returned
GET /api/loans/overdue
GET /api/loans/all
```

- 状態が増えるたびにエンドポイントが増える
- ページング・期間・ユーザーなどの横断的な絞り込みを各エンドポイントに重複実装することになる
- `/active` と `/all?status=BORROWED` のように意味が重複する

### 移行案（フィルタ方式へ寄せる）

履歴全件出力が正式にスコープへ入る段階で、コレクションに対するクエリパラメータ方式へ移行する。

```
GET /api/loans                     # 全件（ページング前提）
GET /api/loans?status=BORROWED     # 貸出中（現在の /active 相当）
GET /api/loans?status=RETURNED     # 返却済み
GET /api/loans?status=OVERDUE      # 延滞（延滞管理の導入後）
GET /api/loans?userId=10           # ユーザーで絞り込み
GET /api/loans?page=0&size=20      # ページング
```

- 状態が増えても `status` の値が増えるだけでエンドポイントは 1 本
- ページング（F-11）・期間・ユーザーなどの条件を 1 か所で組み合わせられる
- 延滞ステータス（`OVERDUE`）を扱う将来案とも整合する

### 移行時の注意

- `GET /api/loans/active` は後方互換のため一定期間残すか、フロントの参照箇所をまとめて切り替える
- ページング（F-11）と同じレスポンス形式（`content` / `page` / `totalElements` など）に揃えると一貫性が保てる

---

## その他の将来候補

- 延滞罰金の計算・請求
- 外部書籍 API（ISBN から書誌情報取得）
- プッシュ通知（返却リマインダー）
- モバイルアプリ（React Native 等）

---

## 関連ドキュメント

- [README](../README.md) — MVP-A/B のスコープ定義
- [openapi-notes.md](./openapi-notes.md) — F-11 ページング API 草案
