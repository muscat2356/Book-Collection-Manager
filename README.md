# LibraShare

社内バックオフィス向けに、蔵書の登録・検索・貸出/返却、貸出対象ユーザーを管理する Web アプリケーション。  
認証基盤を Keycloak に外部化し、Spring Boot REST API と React SPA を分離構成で構築したポートフォリオ作品です。管理画面はログイン済みの社員が利用し、貸出対象の一般ユーザーも Keycloak で管理します。

## 概要

| 項目 | 内容 |
|------|------|
| テーマ | 社内バックオフィス向け蔵書管理（貸出管理） |
| 想定利用者 | 一般ユーザー / 一般社員 / 管理社員 |
| 開発フロー | **学習 → 共有 → 設計 → 実装** |
| 構成 | API + SPA 分離 |
| バックエンド | Spring Boot **4.1.0** + **Java 21** |
| フロントエンド | React + TypeScript + Vite |
| 認証 | Keycloak（Docker / OIDC / JWT） |
| インフラ | Docker Compose |
| チーム | 2名。**Member A = セキュリティ/バック中心、Member B = フロント中心** |
| 対象外 | WebSocket / チャット / リアルタイム通信 |

**コミットライン**: MVP-A（必須）のみ。**Stretch**: MVP-B（フロント強化・検索・ページング）。

### 対象ユーザーとロール

| ロール | 想定ユーザー | 利用できる主な機能 |
|--------|--------------|--------------------|
| `general_user` | 貸出対象の利用者 | 貸出履歴の紐付け対象。バックオフィス画面の操作は対象外 |
| `general_employee` | 一般社員 | 蔵書一覧・詳細、利用者管理、貸出/返却、貸出中一覧 |
| `admin_employee` | 管理社員 | 一般社員の機能 + 蔵書追加・更新・削除 |

全ユーザーの認証・ロール管理は Keycloak を正とします。ログイン後のヘッダーはロールに応じて表示項目を切り替えます。

アプリから作成・編集する対象は利用者（`general_user`）のみです。社員・管理社員（`general_employee` / `admin_employee`）のアカウントは Keycloak 管理コンソールで管理し、アプリの API・画面からは作成・編集しません。これにより、一般社員が社員・管理社員の情報を編集できてしまう権限昇格を構造的に防ぎます。アプリ DB の `users` テーブルにも利用者のみを保持します。

---

## 主な機能

### MVP-A（必須）

| ID | 機能 | Member A | Member B |
|----|------|----------|----------|
| F-01 | Keycloak ログイン + 3 ロール RBAC | Security, Keycloak, Docker | Keycloak JS, Protected Route |
| F-02 | 蔵書一覧・詳細 | 参照 API, Flyway | 一覧・詳細画面 |
| F-02a | 蔵書追加（管理社員のみ） | 登録 API | 追加画面 / ヘッダー導線 |
| F-02b | 蔵書更新（管理社員のみ） | 更新 API | 編集画面 / 管理操作 |
| F-02c | 蔵書削除（管理社員のみ） | 削除 API | 削除操作 / 確認 UI |
| F-04 | 貸出 / 返却 + 在庫連動 | 貸出 API（`loans` テーブル連携） | 貸出/返却操作 → API 呼び出し |
| F-05 | 貸出中一覧（利用者情報つき） | `GET /api/loans/active` | 貸出中一覧 UI |
| F-06 | 利用者登録・更新・削除 | Keycloak Admin API 連携, `users` テーブル連携 | 利用者管理画面 |
| F-07 | `docker compose up` | Compose 全体 | フロント dev 手順 |

- [ ] F-01 Keycloak ログイン（ロール: `general_user` / `general_employee` / `admin_employee`）
- [ ] F-02 蔵書一覧・詳細表示
- [ ] F-02a 蔵書追加（管理社員のみ）
- [ ] F-02b 蔵書更新（管理社員のみ）
- [ ] F-02c 蔵書削除（管理社員のみ）
- [ ] F-04 書籍の貸出・返却（`loans` レコード作成 + `books.stock_count` 減算/加算）
- [ ] F-05 貸出中一覧（利用者情報つき）
- [ ] F-06 利用者登録・更新・削除（Keycloak 管理。社員は対象外）
- [ ] F-07 デモ環境の Docker 一括起動

#### F-04 貸出フロー（MVP-A）

一般社員以上のユーザーがアプリ上で貸出対象ユーザーと書籍を選択して「貸出」を操作すると、次の処理を行う。

1. React SPA が `POST /api/loans`（body: `{ bookId, userId }`）を呼び出す
2. API が貸出対象ユーザーと書籍の存在、在庫数を確認する
3. API が `loans` テーブルにレコードを作成（`status=BORROWED`, `borrowed_at` 記録）
4. API が `books.stock_count` を 1 減算（在庫連動）
5. 返却時は `PUT /api/loans/{id}/return` で `returned_at` 更新 + `stock_count` 加算

| データ | 役割 |
|--------|------|
| `books.stock_count` | 貸出可能な冊数（在庫） |
| `loans` | 誰がいつどの本を借りたか（貸出履歴・状態管理） |
| `users.keycloak_sub` | Keycloak ユーザーと貸出履歴の紐付け |

> 学習用ミニアプリの借りるボタン（フロントのみの `useState`）は Day 14〜18 で本 API に置き換える。

### MVP-B（Stretch・Day 18 Go 後）

| ID | 機能 | 担当 | 条件 |
|----|------|------|------|
| F-03 | 書籍検索 | A: API / B: UI | MVP-A 完了 |
| F-11 | 書籍一覧ページング | A: API / B: UI | MVP-A 完了（F-03 と同時実装推奨） |
| F-10 | UX polish（ローディング、エラー表示、レスポンシブ） | B | 余力 |

- [ ] F-03 書籍検索（タイトル・著者）
- [ ] F-11 書籍一覧ページング（`?page=` `?size=`、前へ/次へ UI）
- [ ] F-10 ローディング / エラー表示など UX 改善

### スコープ外

- WebSocket / STOMP / リアルタイムチャット
- REST コメント・書籍ディスカッション
- 一般ユーザー自身が操作するマイページ / マイ貸出
- 延滞罰金、外部書籍 API、決済、モバイルアプリ
- バッチ処理（延滞ステータス更新・通知）— [将来検討](docs/future-considerations.md)

### 時間不足時の削る順番

| 優先 | あきらめる機能 | 代替案 |
|------|----------------|--------|
| 1位 | F-03 サーバー側検索 | フロント側フィルタのみ |
| 2位 | F-11 サーバー側ページング | 全件表示（蔵書数が少ない間は可） |
| 3位 | F-10 UX polish | 最低限のエラー表示のみ |
| 4位 | F-06 利用者更新・削除 | 利用者登録と一覧を優先 |

---

## 技術スタック

### Backend（Spring Boot 3.x + Java 21）

- `spring-boot-starter-web` — REST API
- `spring-boot-starter-jpa` + `JdbcTemplate`
- `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`
- Flyway — DB マイグレーション
- springdoc-openapi — Swagger UI / OpenAPI 3

### Frontend（React）

- React + TypeScript + Vite
- Keycloak JS アダプタ（OIDC Authorization Code + PKCE）
- axios / fetch

### Infra

- PostgreSQL 15+
- Keycloak
- Docker Compose（`postgres` / `keycloak` / `api`）

| レイヤ | 技術 |
|--------|------|
| Backend | Java 21, Spring Boot 3.x, Spring JPA, Spring Security, OAuth2 Resource Server |
| Frontend | React, TypeScript, Vite, Keycloak JS Adapter |
| DB | PostgreSQL 15+ |
| Auth | Keycloak |
| API 仕様 | OpenAPI 3（springdoc-openapi） |
| Migration | Flyway |
| Infra | Docker Compose |

---

## アーキテクチャ

```
[React SPA] ── OIDC ──> [Keycloak]
     |
     | REST (Bearer JWT)
     v
[Spring Boot API] ──> [PostgreSQL]
     |
     | Keycloak Admin API
     v
[Keycloak]
```

- **フロントエンド**: Keycloak から `access_token` を取得し、`Authorization: Bearer` で API を呼び出す
- **バックエンド**: OAuth2 Resource Server により JWT を検証し、`@PreAuthorize` で RBAC を適用。利用者管理では Keycloak Admin API とアプリ DB を連携（社員・管理社員は Keycloak コンソールで管理）
- **CSRF**: Bearer JWT による stateless API のため CSRF は無効（[セキュリティ](#セキュリティ) 参照）

---

## リポジトリ構成（予定）

```
.
├── backend/          # Spring Boot API
├── frontend/         # React SPA
├── docker/           # Docker Compose, Keycloak 設定
├── docs/             # ER 図, OpenAPI, 画面設計
└── README.md
```

---

## DB スキーマ（MVP-A）

| テーブル | カラム |
|----------|--------|
| `books` | id, title, author, isbn, stock_count, created_at |
| `users` | id, keycloak_sub, display_name, email, is_active, updated_at |
| `loans` | id, book_id, user_id, borrowed_at, returned_at, status |

`users` は貸出対象の利用者（`general_user`）のみを保持する参照テーブルで、認証・ロール管理の正は Keycloak とする。社員・管理社員は `users` に登録せず、Keycloak 管理コンソールで管理する。ロールは Keycloak（JWT）で判定するため `users` に `role` カラムは持たない。`loans` は貸出履歴・状態管理用で、`loans.user_id` は利用者を指す。F-04 の貸出操作でレコードが作成される。`books.stock_count` は在庫数（冊数）を表し、役割が異なる。

利用者削除は **論理削除** とする。物理削除すると `loans.user_id` から辿る過去の貸出履歴が壊れるため、`users` は残したまま `is_active=false` を設定し、あわせて Keycloak 側のユーザーを無効化（`enabled=false`）する。利用者一覧の通常表示は `is_active=true` のみとし、貸出履歴では無効化済み利用者も表示できるようにする。

## API エンドポイント

| Method | Path | 説明 | 権限 |
|--------|------|------|------|
| GET | `/api/books` | 蔵書一覧（MVP-A: 全件 / MVP-B: `?page=` `?size=` でページング、`?q=` で検索） | 一般社員・管理社員 |
| GET | `/api/books/{id}` | 蔵書詳細 | 一般社員・管理社員 |
| POST | `/api/books` | 蔵書追加 | 管理社員のみ |
| PUT | `/api/books/{id}` | 蔵書更新 | 管理社員のみ |
| DELETE | `/api/books/{id}` | 蔵書削除 | 管理社員のみ |
| GET | `/api/users` | 利用者一覧（`general_user` のみ） | 一般社員・管理社員 |
| GET | `/api/users/{id}` | 利用者詳細 | 一般社員・管理社員 |
| POST | `/api/users` | 利用者登録（Keycloak + アプリ DB。ロールは `general_user` 固定） | 一般社員・管理社員 |
| PUT | `/api/users/{id}` | 利用者更新（Keycloak + アプリ DB） | 一般社員・管理社員 |
| DELETE | `/api/users/{id}` | 利用者削除（論理削除 + Keycloak 無効化。貸出中は削除不可） | 一般社員・管理社員 |
| POST | `/api/loans` | 貸出（body: `{ bookId, userId }`） | 一般社員・管理社員 |
| PUT | `/api/loans/{id}/return` | 返却 | 一般社員・管理社員 |
| GET | `/api/loans/active` | 貸出中一覧（ユーザー情報つき） | 一般社員・管理社員 |

ページング API の詳細は [docs/openapi-notes.md](docs/openapi-notes.md) を参照。

---

## ログイン後ヘッダー表示

| ロール | ヘッダー表示 |
|--------|--------------|
| 利用者 | バックオフィス画面は対象外 |
| 一般社員 | 蔵書一覧、利用者管理、貸出中一覧、ログアウト |
| 管理社員 | 蔵書一覧、利用者管理、貸出中一覧、蔵書追加、管理メニュー、ログアウト |

管理社員向けの蔵書更新・削除は、蔵書一覧または詳細画面の管理社員専用操作として表示する。

---

## セキュリティ

- **認証**: Keycloak（外部 IdP）
- **認可**: Spring Security + JWT + ロールベースアクセス制御（RBAC）
- **ロール**: `general_user`（貸出対象の一般ユーザー） / `general_employee`（一般社員） / `admin_employee`（管理社員）
- **CSRF**: SPA + Bearer JWT は stateless のため CSRF 保護は無効化。Cookie セッションを導入する場合は CSRF トークン検証を有効化する
- **CORS**: フロントオリジンのみ許可

---

## 開発体制

| 領域 | Member A（セキュリティ/バック） | Member B（フロント） |
|------|--------------------------------|----------------------|
| 主担当 | Keycloak, Spring Security, JWT, RBAC, REST API, Docker | React 全画面, Keycloak JS, API クライアント |
| MVP-B stretch | F-03 検索 API, F-11 ページング API | F-11 ページング UI, F-10 UX |
| Phase 1 学習 | Day 2〜4: セキュリティ専念 | Day 2〜4: フロント専念 |
| 相手分野の習得 | Day 5 Q&A + EX-3/4 ペア実装 | 同上 |

---

## 開発フロー（1ヶ月）

本プロジェクトは **学習 → 共有 → 設計 → 実装** の 4 フェーズで進めます。未理解のまま詳細設計は行いません。

| Phase | 期間 | 目的 | やること |
|-------|------|------|----------|
| **1 学習** | Day 1 + Day 2〜4 | 主担当分野を深掘り | A=セキュリティ、B=フロント（並行学習） |
| **2 共有** | Day 5 | 学習成果を揃える | 発表、認証×UI 接点整理 |
| **3 設計** | Day 6〜8 | 理解に基づいて設計 | ER, OpenAPI, 画面一覧, 認証シーケンス図 |
| **4 実装** | Day 9〜28 | MVP-A → MVP-B | 認証 → CRUD → 貸出 →（余力）stretch |

### Phase 1 — 並行学習（Day 1〜4）

| Day | Member A | Member B | 共同 |
|-----|----------|----------|------|
| 1 | キックオフ | キックオフ | テーマ、MVP-A/B 合意 |
| 2 | OIDC/JWT、Spring Security | Vite + React、コンポーネント | 15分進捗共有 |
| 3 | Keycloak Docker、Realm/Roles | Router、レイアウト、ワイヤー | 15分進捗共有 |
| 4 | Resource Server、RBAC、CORS | axios 雛形、画面遷移図 | 発表資料提出 |

**Phase 1 成果物**

| 担当 | 提出物 |
|------|--------|
| A | 認証アーキテクチャ図、Keycloak 設定メモ、実装方針 |
| B | React ミニアプリ、画面構成案、コンポーネント構成メモ |

### Phase 2 — 共有・発表（Day 5）

| 時間 | 内容 |
|------|------|
| 午前 | SHARE-1: A 発表（セキュリティ） / SHARE-2: B 発表（フロント） |
| 午後 | SHARE-3: 認証×UI 接点整理、設計論点、MVP-A/B 再確認 |

### Phase 3 — 設計（Day 6〜8）

| Day | 内容 |
|-----|------|
| 6 | ER 図、認証シーケンス図 |
| 7 | OpenAPI（books, loans）、画面一覧・遷移図 |
| 8 | 設計レビュー、Docker 構成図、MVP-B 優先順位確定 |

**画面（7〜8枚）**: ログイン、蔵書一覧、蔵書詳細、利用者一覧、利用者登録/編集、貸出中一覧、蔵書追加、蔵書編集、管理社員向け操作導線

### Phase 4 — 実装（Day 9〜28）

| 期間 | 内容 | 担当 |
|------|------|------|
| Day 9〜13 | Docker + Keycloak + Spring Security + React ログイン（EX-3 ペア） | A 主 / B 主 |
| Day 14〜18 | MVP-A: 蔵書参照 API+UI、管理社員 CRUD、利用者管理 API+UI、貸出 API+UI、貸出中一覧、Seed データ | A API / B UI |
| Day 18 | Go/No-Go 判定 | 共同 |
| Day 19〜22 | MVP-B stretch: 検索 → ページング → UX polish | B 主 / A サポート |
| Day 23〜28 | E2E テスト、README、相互デモ、スクリーンショット | 共同 |

**Day 18 Go/No-Go**

| 判定 | 条件 | 次のアクション |
|------|------|----------------|
| Go | ログイン→利用者登録→蔵書追加/更新/削除→貸出→返却が E2E で動く | F-03 → F-11 → F-10 |
| No-Go | 管理社員 CRUD または貸出が未完了 | MVP-B 捨て、Day 23 仕上げへ |

### 情報交換セッション

| # | Phase | 内容 |
|---|-------|------|
| — | 1（Day 2〜4） | A=セキュリティ、B=フロント（並行学習） |
| SHARE-1〜3 | 2（Day 5） | 発表 + 接点整理 |
| — | 3（Day 6〜8） | 共同設計 |
| EX-3 | 4（Day 9〜13） | ペア: 認証通し |
| EX-4 | 4（Day 14〜18） | ペア: API 連携 |
| SYNC | 毎週 | 1h 進捗確認 |

---

## 学習目標との対応

| 技術 | 実装 |
|------|------|
| DB / CRUD | 蔵書追加・更新・削除、貸出（MVP-A） |
| Security / CSRF | Keycloak + Spring Security RBAC（A 主担当） |
| Keycloak | 外部認証基盤（A 設定、B JS 連携） |
| Docker | Compose 一括起動（A） |
| フロント | React SPA 全画面（B） |
| WebSocket | 計画外（除外） |

---

## 必要条件

- Java 21+
- Node.js 20+（フロント開発時）
- Docker / Docker Compose
- Maven 3.9+

## 起動方法

> 実装完了後に具体コマンドを追記してください。

```bash
# 1. リポジトリを clone
git clone https://github.com/<your-org>/LibraShare.git
cd LibraShare

# 2. バックエンド + DB + Keycloak を起動
docker compose up -d

# 3. フロントエンド（開発時）
cd frontend
npm install
npm run dev
```

| サービス | URL（例） |
|----------|-----------|
| Frontend | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Keycloak | http://localhost:8080/auth （要設定確認） |

## デモアカウント（予定）

| ロール | ユーザー名 | 用途 |
|--------|------------|------|
| `admin_employee` | `admin_employee` | 蔵書追加・更新・削除、利用者管理、貸出・返却 |
| `general_employee` | `general_employee` | 蔵書一覧・詳細、利用者管理、貸出・返却 |
| `general_user` | `general_user` | 貸出対象の利用者サンプル |

---

## スクリーンショット

<!-- 実装後に画像を追加 -->
<!-- ![書籍一覧](./docs/screenshots/book-list.png) -->

---

## 作者

- Backend / Security（Member A）: [古山]
- Frontend（Member B）: [市倉]

## ライセンス

MIT License（またはプロジェクトに合わせて変更）
