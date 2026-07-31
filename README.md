# 蔵書管理システム
社内バックオフィス向けに、蔵書の登録・検索・貸出/返却、貸出対象ユーザーを管理する Web アプリケーション。  
認証基盤を Keycloak に外部化し、Spring Boot REST API と React SPA を分離構成で構築したポートフォリオ作品です。

管理画面はログイン済みの社員が利用し、貸出対象の一般ユーザーも Keycloak で管理します。

## 起動方法

```bash
# 1. リポジトリを clone
git clone git@github.com:muscat2356/Book-Collection-Manager.git
cd Book-Collection-Manager

# 2. バックエンド + DB + Keycloak を起動
docker compose up -d

# 3.ログイン
下記にデモアカウント用のID/パスワードを記載しています。
```

| サービス | URL|
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

### デモユーザーログイン方法
```
蔵書管理アプリ
ログイン名`admin_test@example.com`
パスワード`password`

keycloak
ログイン名`admin`
パスワード`admin`

```

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

---

## 主な機能

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

## DB スキーマ

| ファイル | 説明 |
|----------|------|
| [er-diagram.md](./docs/database/er-diagram.md) | ER 図・制約・到達 SQL |

## API（api/）

| ファイル | 説明 |
|----------|------|
| [openapi.yaml](./docs/api/openapi.yaml) | OpenAPI 3.1 仕様（MVP-A） |
| [openapi-notes.md](./docs/api/openapi-notes.md) | API 補足メモ・JSON 例 |

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

---


### 対象ユーザーとロール

| ロール | 想定ユーザー | 利用できる主な機能 |
|--------|--------------|--------------------|
| `general_user` | 貸出対象の利用者 | 貸出履歴の紐付け対象。バックオフィス画面の操作は対象外 |
| `general_employee` | 一般社員 | 書誌一覧・詳細、利用者管理、checkout 貸出/返却、貸出中一覧 |
| `admin_employee` | 管理社員 | 一般社員の機能 + 書誌追加・更新・削除・所蔵追加/削除 |

全ユーザーの認証・ロール管理は Keycloak を正とします。ログイン後のヘッダーはロールに応じて表示項目を切り替えます。

アプリから作成・編集する対象は利用者（`general_user`）のみです。社員・管理社員（`general_employee` / `admin_employee`）のアカウントは Keycloak 管理コンソールで管理し、アプリの API・画面からは作成・編集しません。
これにより、一般社員が社員・管理社員の情報を編集できてしまう権限昇格を構造的に防ぎます。アプリ DB の `users` テーブルにも利用者のみを保持します。

---
