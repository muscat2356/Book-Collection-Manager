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
| `general_employee` | 一般社員 | 書誌一覧・詳細、利用者管理、checkout 貸出/返却、貸出中一覧 |
| `admin_employee` | 管理社員 | 一般社員の機能 + 書誌追加・更新・削除・所蔵追加/削除 |

全ユーザーの認証・ロール管理は Keycloak を正とします。ログイン後のヘッダーはロールに応じて表示項目を切り替えます。

アプリから作成・編集する対象は利用者（`general_user`）のみです。社員・管理社員（`general_employee` / `admin_employee`）のアカウントは Keycloak 管理コンソールで管理し、アプリの API・画面からは作成・編集しません。これにより、一般社員が社員・管理社員の情報を編集できてしまう権限昇格を構造的に防ぎます。アプリ DB の `users` テーブルにも利用者のみを保持します。

---

## 主な機能

### MVP-A（必須）

| ID | 機能 | Member A | Member B |
|----|------|----------|----------|
| F-01 | Keycloak ログイン + 3 ロール RBAC | Security, Keycloak, Docker | Keycloak JS, Protected Route |
| F-02 | 書誌一覧・詳細（所蔵状態つき） | 参照 API, Flyway | 一覧・詳細画面 |
| F-02a | 書誌追加（管理社員のみ） | 登録 API（初回所蔵作成） | 追加画面 / ヘッダー導線 |
| F-02b | 書誌更新・所蔵追加/削除（管理社員のみ） | 更新 API・copies API | 編集画面（所蔵管理つき） |
| F-02c | 書誌削除（管理社員のみ） | 削除 API | 削除操作 / 確認 UI |
| F-04 | 貸出 / 返却 + 所蔵 status 連動 | 一括貸出 API（`bookCopyIds`） | checkout UI / 返却 |
| F-05 | 貸出中一覧（利用者情報つき） | `GET /api/loans/active` | 貸出中一覧 UI |
| F-06 | 利用者登録・更新・削除 | Keycloak Admin API 連携, `users` テーブル連携 | 利用者管理画面 |
| F-07 | `docker compose up` | Compose 全体 | フロント dev 手順 |

#### 達成状況（2026-07 時点・`develop`）

MVP-A の実装・マージは一通り完了。書誌/所蔵分離・checkout 一括貸出・利用者管理まで API + SPA が揃っている。残りは結合確認・デモ仕上げと、余力があれば MVP-B（F-03 / F-11 / F-10）。

- [x] F-01 Keycloak ログイン（ロール: `general_user` / `general_employee` / `admin_employee`）— SPA 認証・認可 + API Bearer JWT 注入済み
- [x] F-02 書誌一覧・詳細表示（所蔵状態つき・`totalCount` / `availableCount` / `holdings`）
- [x] F-02a 書誌追加（管理社員のみ・`initialCopyCount` で初回所蔵作成）
- [x] F-02b 書誌更新 + 編集画面での所蔵 1 冊追加/論理削除（AVAILABLE のみ削除可。LOANED は 409）
- [x] F-02c 書誌削除（管理社員のみ・貸出中所蔵があれば 409）
- [x] F-04 一括貸出・返却（`bookCopyIds` + 所蔵 status 連動・`/loans/checkout`）
- [x] F-05 貸出中一覧（利用者情報つき・`/loans/active`）
- [x] F-06 利用者登録・更新・削除（対象は `general_user` のみ。社員は Keycloak コンソール）
- [x] F-07 デモ環境の Docker 一括起動（`docker compose up` + フロント `npm run dev`）

#### F-04 貸出フロー（MVP-A）

一般社員以上のユーザーが `/loans/checkout` で利用者と所蔵を選び「貸出」すると、次の処理を行う。詳細は [docs/design/refactor-holdings-and-checkout.md](docs/design/refactor-holdings-and-checkout.md)。

1. React SPA が `POST /api/loans`（body: `{ userId, bookCopyIds }`）を呼び出す
2. API が利用者と各所蔵（`book_copies`）の存在・`AVAILABLE` を確認する（行ロック）
3. 同一 TX で各所蔵を `LOANED`、`loans` を件数分作成（`status=BORROWED`）
4. 1 件でも不可なら 409 で全体ロールバック
5. 返却時は `PUT /api/loans/{id}/return` で `RETURNED` 更新 + 所蔵を `AVAILABLE` に戻す

| データ | 役割 |
|--------|------|
| `books` | 書誌（タイトル単位） |
| `book_copies` | 所蔵 1 冊（`AVAILABLE` / `LOANED`、論理削除 `deleted`）。貸出可能冊数は未削除分から集計 |
| `loans` | 誰がいつどの所蔵を借りたか（`book_copy_id`） |
| `users.keycloak_sub` | Keycloak ユーザーと貸出履歴の紐付け |

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
- バッチ処理（延滞ステータス更新・通知）— [将来検討](docs/future/future-considerations.md)

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

## リポジトリ構成

```
.
├── backend/          # Spring Boot API
├── frontend/         # React SPA
├── docker/           # Keycloak realm 設定など
├── docker-compose.yml
├── docs/             # 設計ドキュメント（[索引](docs/README.md): design / database / api / refactoring 等）
└── README.md
```

---

## DB スキーマ（MVP-A）

| テーブル | カラム |
|----------|--------|
| `books` | id, title, author, isbn, publisher, category_small_id, deleted, created_at |
| `category_large` / `category_medium` / `category_small` | 大中小カテゴリ（書誌は小のみ紐付け） |
| `book_copies` | id, book_id, status（AVAILABLE / LOANED）, deleted |
| `users` | id, keycloak_sub, display_name, email, is_active, updated_at |
| `loans` | id, book_copy_id, user_id, borrowed_at, returned_at, status（BORROWED / RETURNED） |

`users` は貸出対象の利用者（`general_user`）のみを保持する参照テーブルで、認証・ロール管理の正は Keycloak とする。社員・管理社員は `users` に登録せず、Keycloak 管理コンソールで管理する。ロールは Keycloak（JWT）で判定するため `users` に `role` カラムは持たない。`books` は書誌、`book_copies` は所蔵 1 冊である。`books.publisher` は必須。カテゴリは大→中→小で、書誌は `category_small_id` のみ（任意）。貸出可能冊数は `stock_count` カラムではなく所蔵の集計で表す。`loans` は所蔵（`book_copy_id`）と利用者を紐づける。**同一書誌は同一利用者につき同時に 1 冊まで**。ER の詳細は [docs/database/er-diagram.md](docs/database/er-diagram.md)。シード例は [docs/database/sql/seed-publisher-categories.sql](docs/database/sql/seed-publisher-categories.sql)。

書誌削除は **論理削除**（`books.deleted=true`）とする。貸出中の未削除所蔵（`LOANED`）がある書誌は削除不可（`409`）。所蔵削除も **論理削除**（`book_copies.deleted=true`）とする。`status=LOANED` の所蔵は削除不可（`409`）。`AVAILABLE` なら過去の貸出履歴があっても論理削除可。集計・holdings・貸出対象は `deleted=false` のみ。利用者削除は **論理削除**（`users.is_active=false`）＋ Keycloak 無効化とする。利用者一覧の通常表示は `is_active=true` のみ、書誌一覧の通常表示は `deleted=false` のみとする。貸出中の利用者は削除不可（`409`）。

## API エンドポイント

| Method | Path | 説明 | 権限 |
|--------|------|------|------|
| GET | `/api/books` | 書誌一覧（`totalCount` / `availableCount`。MVP-B: ページング・検索） | 一般社員・管理社員 |
| GET | `/api/books/{id}` | 書誌詳細（`holdings` つき） | 一般社員・管理社員 |
| POST | `/api/books` | 書誌追加（`initialCopyCount` で所蔵作成） | 管理社員のみ |
| PUT | `/api/books/{id}` | 書誌更新（冊数フィールドなし） | 管理社員のみ |
| POST | `/api/books/{id}/copies` | 所蔵 1 冊追加（編集画面） | 管理社員のみ |
| DELETE | `/api/books/{id}/copies/{copyId}` | 所蔵 1 冊論理削除（AVAILABLE のみ。LOANED は 409。編集画面） | 管理社員のみ |
| DELETE | `/api/books/{id}` | 書誌削除（論理削除。貸出中所蔵があれば 409） | 管理社員のみ |
| GET | `/api/categories/tree` | 大中小カテゴリツリー | 一般社員・管理社員 |
| GET | `/api/users` | 利用者一覧（`general_user` のみ） | 一般社員・管理社員 |
| GET | `/api/users/{id}` | 利用者詳細 | 一般社員・管理社員 |
| POST | `/api/users` | 利用者登録（氏名・メールのみ。初回パスワードは API 生成 + Keycloak temporary。ロールは `general_user` 固定） | 一般社員・管理社員 |
| PUT | `/api/users/{id}` | 利用者更新（Keycloak + アプリ DB） | 一般社員・管理社員 |
| DELETE | `/api/users/{id}` | 利用者削除（論理削除 + Keycloak 無効化。貸出中は削除不可） | 一般社員・管理社員 |
| POST | `/api/loans` | 一括貸出（body: `{ userId, bookCopyIds }`。同一書誌は一人一冊まで） | 一般社員・管理社員 |
| PUT | `/api/loans/{id}/return` | 返却 | 一般社員・管理社員 |
| GET | `/api/loans/active` | 貸出中一覧（ユーザー・書誌・bookCopyId つき） | 一般社員・管理社員 |

ページング API の詳細は [docs/api/openapi-notes.md](docs/api/openapi-notes.md) を参照。

---

## ログイン後ヘッダー表示

| ロール | ヘッダー表示 |
|--------|--------------|
| 利用者 | バックオフィス画面は対象外 |
| 一般社員 | 書誌一覧、貸出、貸出中一覧、利用者管理、ログアウト |
| 管理社員 | 書誌一覧、貸出、貸出中一覧、利用者管理、書誌追加、管理メニュー、ログアウト |

管理社員向けの書誌更新・削除・所蔵追加/削除は、書誌編集画面（`/books/:id/edit`）の管理社員専用操作として表示する。貸出は `/loans/checkout` から行う（通常の書誌詳細からは貸出しない）。

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

**画面**: ログイン、書誌一覧、書誌詳細、書誌追加/編集、利用者一覧、利用者登録/編集、貸出 checkout、貸出中一覧、管理社員向け操作導線

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

```bash
# 1. リポジトリを clone
git clone https://github.com/spiritualmasa/LibraShare.git
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
| API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Keycloak | http://localhost:8080 |

## デモアカウント（Keycloak `LibraShare` realm）

テストユーザーは `docker/keycloak/import/LibraShare-realm.json` で import されます。パスワードは Keycloak 管理コンソール（`admin` / `admin`）で確認・再設定してください。

| ロール | ユーザー名 | 用途 |
|--------|------------|------|
| `admin_employee` | `admin_test@example.com` | 蔵書追加・更新・削除、利用者管理、貸出・返却 |
| `general_employee` | `employee_test@example.com` | 書誌一覧・詳細、利用者管理、貸出・返却 |
| `general_user` | `user_test@example.com` | 貸出対象の利用者サンプル（SPA 利用不可） |

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
