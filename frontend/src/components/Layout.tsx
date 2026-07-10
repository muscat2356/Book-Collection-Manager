import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { isAdmin } from '../auth/roles';

export function Layout() {

  const {keycloak} = useAuth();

  function handleLogout() {
    keycloak?.logout({ redirectUri: window.location.origin })
  }

  return (
    <>
      <header className="app-header">
        <div className="app-header__inner">
          <h1 className="app-title">LibraShare</h1>
          <p className="app-subtitle">蔵書の登録・検索・貸出/返却を管理する図書館アプリ</p>
          <nav className="app-nav">
            <Link to="/books" className="nav-link">書籍一覧</Link>
            {isAdmin(keycloak) && (
              <Link to="/books/new" className="nav-link">新規書籍登録</Link>
            )}
            <Link to="/loans/active" className="nav-link">貸出中の書籍</Link>
            <Link to="/users" className="nav-link">利用者管理</Link>
            <button type='button'
                    className='nav-link'
                    onClick={handleLogout}>
            ログアウト
            </button>
          </nav>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </>
  )
}
