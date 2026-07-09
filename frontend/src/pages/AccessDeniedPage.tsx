import { useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { isEmployee } from '../auth/roles'

export function AccessDeniedPage() {
  const { keycloak, isAuthenticated, isInitialized } = useAuth()

  useEffect(() => {
    if (!isInitialized) return
    if (isAuthenticated) return
    keycloak?.login()
  }, [isInitialized, isAuthenticated, keycloak])

  if (!isInitialized) return <p className="page-status">認証状態を確認中...</p>
  if (!isAuthenticated) return <p className="page-status">ログイン画面へ移動中...</p>
  if (isEmployee(keycloak)) return <Navigate to="/books" replace />

  return (
    <section className="page">
      <h1>アクセス権限がありません</h1>
      <p className="page__lead">このアプリは社員アカウント専用です。</p>
      <button
        type="button"
        className="btn"
        onClick={() => keycloak?.logout({ redirectUri: window.location.origin })}
      >
        ログアウト
      </button>
    </section>
  )
}
