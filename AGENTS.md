# LibraShare — AI Agent 向けガイド

このファイルは Cursor 等の AI Agent がプロジェクトを理解するための共通ルールです。`.cursor/rules/` のルールと併用してください。

## 言語・トーン

- 常に**日本語**で回答する
- コードのコメントも日本語
- 明るく、ポジティブな口調

## プロジェクト概要

社内バックオフィス向け蔵書管理（貸出管理）Web アプリ。API + SPA 分離構成。

| 項目 | 内容 |
|------|------|
| バックエンド | Spring Boot 4.1.0 + Java 21（Member A） |
| フロントエンド | React + TypeScript + Vite（Member B） |
| 認証 | Keycloak（OIDC / JWT） |
| DB | PostgreSQL |

## チーム分担

| 担当 | 領域 |
|------|------|
| Member A | Keycloak, Spring Security, REST API, Docker |
| Member B | React 全画面, Keycloak JS, API クライアント |

## 開発フロー

**学習 → 共有 → 設計 → 実装** の順を守る。未理解のまま詳細設計や実装に進まない。

## スコープ

### MVP-A（必須）

- F-01  Keycloak ログイン + 3 ロール RBAC
- F-02  蔵書一覧・詳細
- F-02a/b/c  蔵書 CRUD（管理社員のみ）
- F-04  貸出/返却 + 在庫連動
- F-05  貸出中一覧
- F-06  利用者管理（`general_user` のみ）
- F-07  Docker 一括起動

### MVP-B（Stretch・余力時）

- F-03  書籍検索
- F-11  ページング
- F-10  UX polish

### 対象外

WebSocket、一般ユーザー向けマイページ、`GET /api/loans/me`、延滞罰金など

## ロール

| ロール | SPA 利用 |
|--------|----------|
| `general_user` | 不可（貸出対象として DB/API で管理） |
| `general_employee` | 蔵書参照、利用者管理、貸出/返却 |
| `admin_employee` | 上記 + 蔵書 CRUD |

認証（Keycloak ログイン）と認可（社員ロール必須）を分離する。

## 設計ドキュメント（正）

索引: [docs/README.md](docs/README.md)

- [docs/design/refactor-holdings-and-checkout.md](docs/design/refactor-holdings-and-checkout.md) — 書誌/所蔵分離・checkout 一括貸出（確定）
- [docs/design/screen-transition.md](docs/design/screen-transition.md) — 画面・ルート
- [docs/api/openapi-notes.md](docs/api/openapi-notes.md) — API 契約
- [docs/design/sequence-diagram.md](docs/design/sequence-diagram.md) — フロー
- [docs/database/er-diagram.md](docs/database/er-diagram.md) — DB
- [docs/design/frontend-gap-analysis.md](docs/design/frontend-gap-analysis.md) — フロント差分リスト
- [docs/refactoring/review-refactoring-diff.md](docs/refactoring/review-refactoring-diff.md) — レビュー指摘の設計差分

## AI Agent への依頼方針

### Member A（バック）

- **実装は本人が手で行う**。Agent は下書きの一括生成より、差分整理・設計確認・エラー原因の説明を優先する
- 依頼時は機能 ID（F-02 / F-04 等）か `docs/design/refactor-holdings-and-checkout.md` / `docs/api/openapi-notes.md` を添える
- レイヤ構成（controller / service / repository / domain / dto / Flyway）に合わせる
- 旧単件貸出（`bookId` + `stock_count`）は実装せず、所蔵分離後の契約で進める

### Member B（フロント）

- **実装は本人が手で行う**。Agent は下書きコードの一括生成より、差分整理・設計確認・エラー原因の説明を優先する
- 依頼時は機能 ID（F-01 等）か docs への参照を添える
- 既存のコンポーネント構成（pages / components / api / types）に合わせる
- セキュリティの正は API 側。フロントの権限制御は UI 出し分け + 403 ハンドリング

### 「実装」と送られた場合の進め方（共通）

- ① **現状のコードを確認**: 関連する `docs/` と担当領域の実装を読み、現在地（未実装・暫定・TODO）を特定する
- ② **次タスクを提示**: 設計と差分が最小になる順で、次にやるべき作業を具体化する
- ③ **写経コード + 解説**: 写経できるコード例 + 機能 / 依存 / データの流れ / 初出要素の解説（基本構文の説明は不要）

フロント詳細は [frontend/AGENTS.md](frontend/AGENTS.md) と `.cursor/rules/libra-share-frontend.mdc`。  
バック詳細は [backend/AGENTS.md](backend/AGENTS.md) と `.cursor/rules/libra-share-backend.mdc`。

## フロントエンド規約

- 関数コンポーネント + TypeScript
- データ取得: `useEffect` + loading / error / data の 3 状態
- API: axios + Bearer JWT（`api/client.ts`）
- ルーティング: React Router v7、`Layout` + `Outlet`
- 貸出中一覧は `/loans/active`（`/loans/me` は廃止）。貸出入口は `/loans/checkout`
- **CSS**: 開発中は `VITE_USE_FULL_THEME=false`（`.env.development`）。フルテーマは `frontend/src/styles/theme/`

## バックエンド規約

- Java 21 + Spring Boot（controller / service / repository / domain / dto）
- DB 変更は Flyway 追加のみ（既存 V* を書き換えない）
- エラー: `{ error, message }`。入力 400 / 業務衝突 409。認可は `@PreAuthorize` + JWT
- 書誌/所蔵分離・一括貸出は [docs/design/refactor-holdings-and-checkout.md](docs/design/refactor-holdings-and-checkout.md) が正

## 参照リポジトリ

- 学習用プロトタイプ: `react-handson/my-react-app`（本番は `frontend/` へ移行）
