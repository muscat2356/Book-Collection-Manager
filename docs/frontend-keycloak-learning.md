# React + Keycloak 学習メモ（F-01）

Member B（フロント）向け。LibraShare の Keycloak ログイン連携（F-01）を進めるうえで、ここまで学習・実装した内容をまとめたものです。

設計の正は引き続き以下を参照してください。

- [screen-transition.md](./screen-transition.md) — 認証と認可の分離
- [sequence-diagram.md](./sequence-diagram.md) — OIDC フロー
- [frontend-gap-analysis.md](./frontend-gap-analysis.md) — 残タスク一覧

---

## 1. 全体像

LibraShare のフロントは **keycloak-js**（Keycloak JavaScript Adapter）で OIDC 認証を行い、取得した `access_token` を API 呼び出しに付与します。

```
[React SPA] ── OIDC (Authorization Code + PKCE) ──> [Keycloak]
      │
      └── Authorization: Bearer <JWT> ──> [Spring API]
```

### 認証と認可の分離

| 状態 | 挙動 |
|------|------|
| 未ログイン | 保護ルート → `/login` へ誘導（今後 `ProtectedRoute` で実装） |
| ログイン済み + 社員ロールあり | SPA 利用可（`general_employee` / `admin_employee`） |
| ログイン済み + `general_user` のみ | 認証は通るが SPA 利用不可 → アクセス拒否画面 |

ロールは JWT の `realm_access.roles` から判定します（次フェーズ）。

---

## 2. 前提・環境

### Node.js は必要か

| 領域 | Node.js |
|------|---------|
| Keycloak サーバー | 不要（Docker 上の Java アプリ） |
| Spring Boot API | 不要（Java 21） |
| React フロント（Vite） | **必要**（`npm install` / `npm run dev` / `keycloak-js` 導入） |

Docker の frontend コンテナ（`node:20-alpine`）だけ使う場合は、ホストへの Node.js インストールは省略可能です。ただし手元での開発では **Node.js 20+** を入れておくのが一般的です。

### 環境変数（`.env.development`）

```env
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=library-realm
VITE_KEYCLOAK_CLIENT_ID=librashare-frontend
```

`docker-compose.yml` のバックエンドは `KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/library-realm` を参照しています。Realm 名はこれと揃えます。

### Keycloak クライアント設定（Member A と要確認）

`onLoad: 'check-sso'` を使う場合、Keycloak 管理コンソールで最低限以下が必要です。

| 設定 | 例 |
|------|-----|
| Client type | Public（SPA） |
| Valid redirect URIs | `http://localhost:5173/*` |
| Web origins | `http://localhost:5173` |
| Realm roles | `general_user`, `general_employee`, `admin_employee` |

`docs/` には Keycloak Realm の export JSON はまだありません。設定は管理コンソールまたは Member A のメモを正とします。

---

## 3. パッケージ

```bash
npm install keycloak-js
```

`package.json` の dependencies に `keycloak-js` が追加されます。

---

## 4. 実装の骨格（ここまで完了）

実装ファイル: `frontend/src/auth/AuthContext.tsx`

### 4.1 Keycloak インスタンスの保持（シングルトン）

`Keycloak` インスタンスは **コンポーネント外（モジュールスコープ）** で1回だけ生成します。

```typescript
const keycloakInstance = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
})
```

**なぜコンポーネント外か**

- 再レンダーのたびに `new Keycloak()` されるのを防ぐ
- アプリ全体で同じインスタンスを共有する
- `init()` は同一インスタンスに対して1回にまとめる必要がある

### 4.2 初期化オプション

```typescript
keycloakInstance.init({
  onLoad: 'check-sso',
  pkceMethod: 'S256',
})
```

| オプション | 意味 |
|------------|------|
| `onLoad: 'check-sso'` | ページ読み込み時に既存セッションをサイレント確認。未ログインならそのまま表示 |
| `pkceMethod: 'S256'` | SPA 向け PKCE（Proof Key for Code Exchange） |

`onLoad: 'login-required'` にすると、未ログイン時に即 Keycloak ログイン画面へリダイレクトします。LibraShare ではまず `check-sso` でセッション復元を試みる方針です。

### 4.3 Context / Provider / Hook

```typescript
interface KeycloakContextType {
  keycloak: Keycloak | null
  isAuthenticated: boolean
  isInitialized: boolean
}

export const KeycloakProvider = ({ children }) => { ... }
export const useAuth = () => useContext(KeycloakContext)
```

| 値 | 意味 |
|----|------|
| `keycloak` | Keycloak インスタンス（login / logout / token 取得に使う） |
| `isAuthenticated` | ログイン済みか（`init()` 成功後の `boolean`） |
| `isInitialized` | Keycloak 初期化処理が完了したか（成功・失敗どちらでも `true` にする） |

**`isAuthenticated` と `isInitialized` の違い**

- `isInitialized === false` → まだ init の結果がわからない（ローディング表示に使う）
- `isInitialized === true` かつ `isAuthenticated === false` → init は終わったが未ログイン
- `isInitialized === true` かつ `isAuthenticated === true` → ログイン済み

---

## 5. init の二重実行防止（`initPromise` パターン）

### 問題: React StrictMode

`main.tsx` で `<StrictMode>` を使っていると、**開発時のみ** `useEffect` が2回実行されることがあります。

```
1回目: マウント → useEffect → init()
2回目: アンマウント → 再マウント → useEffect → init() 再度 ⚠️
```

`keycloak-js` の `init()` は同一インスタンスに複数回呼ぶと不安定になりうるため、**init 自体を1回に束ねる**必要があります。

### 解決: モジュールレベルで Promise を保持

```typescript
let initPromise: Promise<boolean> | null

function initKeycloak(): Promise<boolean> {
  if (!initPromise) {
    initPromise = keycloakInstance.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
    })
  }
  return initPromise
}
```

| 質問 | 答え |
|------|------|
| 実行済みフラグ？ | 近い。ただし `boolean` ではなく **init の Promise を保持**している |
| なぜ `let`？ | 最初は `null`、後から `Promise` を1回代入するため |
| `init()` の戻り値は `boolean`？ | **いいえ、`Promise<boolean>`**。`.then` の引数が `boolean` |

```
initPromise = null           → まだ init していない
initPromise = Promise<...>   → すでに init 開始済み（進行中 or 完了）
```

2回目以降の `initKeycloak()` は新しい `init()` を呼ばず、**保存済みの Promise を返す**だけです。

---

## 6. `useEffect` 内の Promise の動き

```typescript
useEffect(() => {
  initKeycloak()
    .then((authenticated) => {
      setIsAuthenticated(authenticated)
      setIsInitialized(true)
    })
    .catch((err) => {
      console.error('Keycloak init failed', err)
      setIsInitialized(true)
    })
}, [])
```

### 時間の流れ

```
t0: useEffect 開始
t1: initKeycloak() 呼び出し → Promise<boolean> が返る（まだ pending の可能性）
t2: .then / .catch を登録
t3: useEffect 終了（ここで init 完了は待たない）
...
t4: Keycloak init 完了
t5: 成功 → .then に boolean が渡る / 失敗 → .catch に error が渡る
t6: setState → React 再レンダー
```

### `.then` と `.catch` の役割

| 経路 | 条件 | 処理 |
|------|------|------|
| `.then` | init **成功** | `authenticated` は `true`（ログイン済み）または `false`（未ログイン） |
| `.catch` | init **失敗** | Keycloak 未起動・Realm 未作成・Client 設定ミスなど |

**注意:** `authenticated === false` はエラーではない。init は成功したがセッションがなかった、という意味です。

**`.catch` でも `setIsInitialized(true)` にする理由:** init が失敗しても「初期化処理は終わった」とマークし、画面が永遠にローディングのままになるのを防ぐためです。

### `async/await` との対応

```typescript
useEffect(() => {
  ;(async () => {
    try {
      const authenticated = await initKeycloak()
      setIsAuthenticated(authenticated)
      setIsInitialized(true)
    } catch (err) {
      console.error('Keycloak init failed', err)
      setIsInitialized(true)
    }
  })()
}, [])
```

動きは `.then` / `.catch` 版と同じです。

---

## 7. レビューで挙がった改善点（未対応・次フェーズ）

| 項目 | 状態 | メモ |
|------|------|------|
| `initPromise` による二重 init 防止 | 済 | 上記 §5 |
| `.catch` でのエラーハンドリング | 済 | 上記 §6 |
| `main.tsx` への `KeycloakProvider` 接続 | 未 | 次の作業 |
| 初期化中のローディング UI | 未 | `!isInitialized` の間表示 |
| `login()` / `logout()` の Context 提供 | 未 | `LoginPage` / `Layout` で使用 |
| ロール判定（`general_employee` 等） | 未 | JWT `realm_access.roles` |
| `ProtectedRoute` | 未 | 未ログイン → `/login` |
| `AccessDeniedPage` | 未 | `general_user` 拒否 UX |
| `api/client.ts` の Bearer 注入 | 未 | `localStorage` → `keycloak.token` |
| トークン更新（`updateToken`） | 未 | API 呼び出し前に実施 |

---

## 8. 次の実装ステップ（参考）

1. `main.tsx` に `<KeycloakProvider>` を追加
2. `!isInitialized` の間はローディング表示
3. Context に `login()` / `logout()` を追加
4. `ProtectedRoute` + `AccessDeniedPage`
5. `api/client.ts` で `keycloak.token` を注入
6. `Layout` のロール別ナビ出し分け

---

## 9. 関連ファイル

| ファイル | 役割 |
|----------|------|
| `frontend/src/auth/AuthContext.tsx` | Keycloak 保持・初期化・Context |
| `frontend/.env.development` | `VITE_KEYCLOAK_*` |
| `frontend/src/main.tsx` | Provider 接続予定 |
| `frontend/src/api/client.ts` | Bearer 注入予定 |
| `docker-compose.yml` | Keycloak / backend の issuer URI |

---

## 10. 用語クイックリファレンス

| 用語 | 説明 |
|------|------|
| OIDC | OpenID Connect。Keycloak が提供する認証プロトコル |
| PKCE | 公開クライアント（SPA）向けの認可コード傍受対策 |
| `check-sso` | サイレントに既存セッションを確認する init モード |
| `access_token` | API 呼び出しに使う JWT |
| `realm_access.roles` | JWT 内のロール一覧（社員判定に使用） |
| `initPromise` | init の二重実行を防ぐための共有 Promise |
